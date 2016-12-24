package logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
/**
 * Outputs logs to a log file.
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */
public class StoreLogFile implements StoreLog {

	private File folder;
	HashMap<String, FileOutputStream> openStreams = new HashMap<String, FileOutputStream>();
	public StoreLogFile (File folder) {
		this.folder = folder;		
	}
	public StoreLogFile (String folderName) {
		this.folder = new File(folderName);		
	}
	@Override
	public void store(String fileName, byte[] log) {
		try {
			if (!openStreams.containsKey(fileName)) 
				openStreams.put(fileName, new FileOutputStream(new File(folder,fileName)));
			FileOutputStream fos = openStreams.get(fileName);
			fos.write(log);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	@Override public void close() {
		for (String k : openStreams.keySet())
			try {
				openStreams.get(k).close();
			} catch (IOException e) {
				e.printStackTrace();
			}

	}
}
