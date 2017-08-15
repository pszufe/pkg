package simulator;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ec.util.MersenneTwisterFast;
import rsalgos.HasMinMeasuresPerPoint;
import rsalgos.HasPreselection;
import rsalgos.PRSalgorithm;
import sim.engine.SimState;
import sim.engine.Steppable;
import simulator.tools.RandomStream;
import simulator.tools.RandomStreamFactory;

public class ExperimentSimulator extends SimState {

	private static final long serialVersionUID = 2920281553255093031L;
	static {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		String className = "";

		File f = null;
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];            
            if (!ste.getClassName().equals(ExperimentSimulator.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
            	className = ste.getClassName();                
                try {
                	Class<?> currentClass = Class.forName(className);
	                URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
                    if (resource.getProtocol().equals("file")) {
                        f = new File(resource.toURI());
                    } else if (resource.getProtocol().equals("jar")) {
                        String path = resource.getPath();
                        f = new File(path.substring(5, path.indexOf("!")));    
                    } 
                } catch (Exception ignored) { } 
            	break;
            }
        }
        String hostname = System.getProperty("os.name").startsWith("Windows")?System.getenv("COMPUTERNAME"):System.getenv("HOSTNAME");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("#\t"+sdf.format(new Date())+"\t"+"v1.0.2"+"\t"+className+"\t"+(f==null?"null":f.getAbsolutePath())+"\t"+(f==null?"null":sdf.format(new Date(f.lastModified())))+"\t"+hostname);
		System.out.println("`\t"+"seed"+"\t"+"alg"+"\t"+"N"+"\t"+"a"+"\t"+"m"+"\t"+"obsAvailDist"+"\t"+"obsAvailDistParam"+"\t"+"exmperimDist"+"\t"+"experimTime"+"\t"+"noExperiments"+"\t"+"queue_consumed"+"\t"+"time"+"\t"+"queue"+"\t"+"selection"+"\t"+"ocba"+"\t"+"bestBinary"+"\t"+"bestDistance");
	}
	public static String toStr(RSpoint[] selected) {
		StringBuilder res = new StringBuilder();
		for (int i=0;i<selected.length;i++) {
			res.append(selected[i].i);
			if (i<selected.length-1) res.append(" ");
		}
		return res.toString();
	}
	
	public final String algName;
	private final RSpoint[] points; 
	private final RandomStream[] rsExperimentTimes;
	private final RandomStream rsExperimentAvalability;
	final ObservationWorker[] workers;
	final ObservationQueue queue;
	final Steppable stepAllocator;
	final PRSalgorithm alg;
	final long seed;
	final Distribution experimentAvailability;
	final Distribution experimentTime;
	/**
	 * number of available experimenters
	 */
	final int a;
	/**
	 * number of parallel slots per experimenter
	 */
	final int m;
	/**
	 * number of points to choose from
	 */
	final int N;
	
	/**
	 * index of the real best point
	 */
	final int bestN;
	
	
	private int experimentCalculationCount;
	/**
     * 	
     * @param seed random number generator seed
     * @param N number of variables
     * @param experimentAvailability experiment availability distribution 
     * @param experimentTime time of an experiment distribution
     * @param a number of available experimenters
     * @param m number of parallel slots per experimenter
     * @param alg Parallel R&S algorithm
     * @param initQueueSize initial size of the queue 
     */
	public ExperimentSimulator(long seed, int N, Distribution experimentAvailability, Distribution experimentTime, int a, int m, PRSalgorithm alg, int initQueueSize) {
		super(new MersenneTwisterFast(seed));
		this.seed = seed;
		this.N = N;
		this.alg = alg;
		this.a = a;
		this.m = m;
		this.experimentTime = experimentTime;
		this.experimentAvailability = experimentAvailability;
		this.algName = alg.getClass().getName().substring(alg.getClass().getName().indexOf(".")+1, alg.getClass().getName().indexOf("_"))+
						(alg instanceof HasMinMeasuresPerPoint?((HasMinMeasuresPerPoint)alg).getMinMeasuresPerPoint():"")+
						(alg instanceof HasPreselection && ((HasPreselection)alg).getPreselectionDistance()>0?("p"+((HasPreselection)alg).getPreselectionDistance()):"");
		
		this.experimentCalculationCount=0;
		RandomStreamFactory rsf = new RandomStreamFactory(N*3, seed);
		points = new RSpoint[N];
		rsExperimentTimes =  new RandomStream[N];
		int rsExperimAvailaIds[] = new int[N];
		int bestIx = 0;
		for (int i=0;i<N;i++) {
			points[i] = new RSpoint(rsf.getStream(i), i);
			if (i>0 && points[i].u > points[bestIx].u) bestIx = i;
			rsExperimentTimes[i] = rsf.getStream(N+i);		
			rsExperimAvailaIds[i] = 2*N+i;
		}
		bestN = bestIx;
		rsExperimentAvalability = rsf.getStream(rsExperimAvailaIds);		
		workers = new ObservationWorker[a];
		for (int j=0;j<a;j++) {
			workers[j] = new ObservationWorker(m);
		}
		queue = new ObservationQueue(experimentAvailability, rsExperimentAvalability,initQueueSize);		
		stepAllocator = new StepAllocator();
		log0();
	}
	
	public int getExperimentCalculationCount() {
		return experimentCalculationCount;
	}
	
	
	private void log0() {
		System.out.print("~\t"+seed+"\t"+algName+"\trealval");
		for (int i=0;i<points.length;i++) {
			System.out.print("\t"+points[i].u);
			//System.out.printf("%.7f", points[i].u);								
		}
		System.out.println();
		//System.out.println("AT STEP #0# "+algName+"="+toStr(selected));
		
	}
	
	
	/**
	 * Method for conveniently running this model
	 */
	public void run (int experimentCalculationsBudget,boolean echo) {
		this.start();
		schedule.scheduleOnce(0,queue);
		
		long lastStepTime = System.currentTimeMillis();
		long startTime = System.currentTimeMillis();
		long reportEvery = 1000;
		boolean moreSteps;
		long step = 0;
		double simTime = 0;
		while (moreSteps = this.schedule.step(this)) {
			simTime = this.schedule.getTime();
			step = this.schedule.getSteps();
			
			if (this.getExperimentCalculationCount() >= experimentCalculationsBudget)
				break;
			if (step % reportEvery == 0) {
				if (echo)
					System.err.println("Step="+step+" sche.getTime()="+((long)(simTime*10))/10.0+" speed steps/s="+(reportEvery*1000.0/(System.currentTimeMillis()-lastStepTime)));
				lastStepTime = System.currentTimeMillis();
			}
		}
		this.finish();
		this.kill();
		if (echo) {
			System.out.flush();
			if (!moreSteps) 
				System.err.print("The simulation has been exhausted!");
			else 
				System.err.print("Simulation completed!");
			System.err.println(" step="+step+" schedule.getTime()="+((long)(simTime*10))/10.0+" speed steps/s="+(reportEvery*1000.0/(System.currentTimeMillis()-startTime)));
		}
		
	}
	
	
	private class StepAllocator implements Steppable {
		private static final long serialVersionUID = -2219657694981055120L;
		DecimalFormat df = new DecimalFormat("#.#######",DecimalFormatSymbols.getInstance(Locale.US));
		@Override
		public void step(SimState state) {
			//System.out.println(this.getClass().getName()+" "+state.schedule.getTime());
			for (int wNo=0;wNo<workers.length;wNo++) {
				ObservationWorker worker = workers[wNo];
				if (worker.isAvailable() 
						&& worker.parallelCapacitySlots <= queue.getQueueSize()) {
					queue.popFromQueue(worker.parallelCapacitySlots);
					//Double diff = null;
					if (worker.selected == null) {
						worker.selected = alg.getPointsInit_k_0(points,wNo,a,m);
					} else {
						double x[] = new double[worker.selected.length];
						for (int i=0;i<worker.selected.length;i++) {
							x[i] = worker.selected[i].get_y();
							worker.selected[i].decS();
						}	
						experimentCalculationCount += worker.selected.length;
						worker.selected = alg.getNextPoints(x, worker.selected, points, wNo, false);
						//diff= max(alg.getVkg())-min(alg.getVkg());
					}
					//System.out.println("AT TIME "+state.schedule.getTime() + " "+algName+"=\t"+toStr(worker.selected)+"\t"+alg.getBestVkg()+"\tqueue\t"+queue.getQueueSize()+" diff="+diff);
					
					int bestPointIx = 0;
					
					for (int i=1;i<points.length;i++) {
						if (points[i].getMeanY() > points[bestPointIx].getMeanY()) bestPointIx=i;
					}
					System.out.println("`\t"+seed+"\t"+algName+"\t"+N+"\t"+a+"\t"+m+
							"\t"+(experimentAvailability==null?"none":experimentAvailability.getName())+
							"\t"+(experimentAvailability==null?"0":experimentAvailability.getParam())+
							"\t"+experimentTime.getName()+"\t"+experimentTime.getParam()+
							"\t"+experimentCalculationCount+"\t"+queue.getProcessed()+
							"\t"+df.format(schedule.getTime())+"\t"+queue.getQueueSize()+
							"\t"+toStr(worker.selected)+
							"\t"+(alg.isOcbaFallback()?1:0)+
							"\t"+(bestPointIx == bestN?"1":"0")+
							"\t"+df.format(points[bestN].u-points[bestPointIx].u));
					
					double time = 0;
					for (int j=0;j < worker.selected.length;j++) {
						time = Math.max(time, experimentTime.next(rsExperimentTimes[worker.selected[j].i]));
						worker.selected[j].incS();
					}
					worker.schedule(time, state);
					
				}
			}
		}
	}

	public static double min(double[] arg) {
		double res = arg[0];
		for (int i=1;i<arg.length;i++) if (res < arg[i]) res = arg[i];
		return res;
	}
	public static double max(double[] arg) {
		double res = arg[0];
		for (int i=1;i<arg.length;i++) if (res > arg[i]) res = arg[i];
		return res;
	}		
}
