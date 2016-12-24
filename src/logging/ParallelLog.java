package logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


/**
 * The represents a parallelized storage mechanism for simulation logs.
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */
public class ParallelLog {
	
	private final int LOG_BUFFER_SIZE;	
	private final long MIN_ELEMS;  
	private final long MIN_TIME_MS;  
	private final String[] logBuffer;
	private final long[]   logTimes;
	private final AtomicLong currIx = new AtomicLong(-1);
	private final AtomicBoolean checkpointCheck = new AtomicBoolean(false);
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private final String hostname;
	private final String processStart;
	private final StoreLog storeLog;
	private final String fileNameBase;
	private final boolean ignore;
	public ParallelLog (String fileNameBase, StoreLog storeLog, int logBufferSize, long minElems, long minTimeMs, String hostname) {
		this.ignore = (storeLog instanceof StoreLogIgnore);
		this.LOG_BUFFER_SIZE = logBufferSize;
		this.MIN_ELEMS = minElems;
		this.MIN_TIME_MS = minTimeMs;
		logBuffer = new String[LOG_BUFFER_SIZE];
		logTimes  = new long[LOG_BUFFER_SIZE];
		processStart = sdf.format(new Date());
		this.storeLog = storeLog;
		this.fileNameBase = fileNameBase;		
		this.hostname = hostname;	
	}
	
	private long lastCheckpointIx = 0;
	private long lastCheckpointTime = System.currentTimeMillis();
	public void log(String str) {
		if (ignore) return;
		long elemId = currIx.incrementAndGet();
		int arrId = (int) (elemId % LOG_BUFFER_SIZE);
		logBuffer[arrId] = str;	
		logTimes[arrId]  = System.currentTimeMillis();
		if (elemId - lastCheckpointIx >= LOG_BUFFER_SIZE*9/10) {
			throw new Error("Buffer underrun - increase the buffer size!");
		}
		boolean otherCheck = checkpointCheck.getAndSet(true);
		if (!otherCheck) {			 
			if ((elemId - lastCheckpointIx) + 1 >= MIN_ELEMS && logTimes[arrId] - lastCheckpointTime >= MIN_TIME_MS ) {
				long maxId;
				int ix;
				for (maxId = lastCheckpointIx;maxId<elemId;maxId++) {
					ix = (int) (maxId % LOG_BUFFER_SIZE);
					if (logTimes[ix] >= logTimes[arrId]) {
						break;
					}					 
				}
				if (maxId > lastCheckpointIx)
					checkpoint(lastCheckpointIx,maxId-1);
				lastCheckpointIx = maxId;
			}			
			checkpointCheck.set(false);
		}
		
	}
	
	private int part = 0;
	
	private void checkpoint (long start, long end) {
		if (ignore) return;
		part++;
		String partName = String.format("%08d", part);
		StringBuilder log = new StringBuilder();
		int ix;
		String tName = Thread.currentThread().getName();
		for (long id =start; id <= end;id++) {
			ix = (int) (id % LOG_BUFFER_SIZE);
			log.append(hostname);
			log.append("\t");
			log.append(processStart);
			log.append("\t");
			log.append(partName);
			log.append("\t");
			log.append(id);
			log.append("\t");
			log.append(sdf.format(new Date(logTimes[ix])));
			log.append("\t");
			log.append(tName);
			log.append("\t");
			log.append(logBuffer[ix]);
			log.append("\r\n");			
			logBuffer[ix] = null;
		}
		//storeLog.store(fileNameBase+partName+".txt", log.toString().getBytes());
		storeLog.store(fileNameBase+".txt", log.toString().getBytes());
	}
	
	public void close() {
		if (ignore) return;
		checkpoint(lastCheckpointIx,currIx.get());
		storeLog.close();
	}
	
}
