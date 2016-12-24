package experiments;

import rsalgos.AKG_RSalgorithm;
import rsalgos.AOCBA_RSalgorithm;
import rsalgos.KG_RSalgorithm;
import rsalgos.OCBA_RSalgorithm;
import rsalgos.SKG_RSalgorithm;
import simulator.ConstDistribution;
import simulator.ExpDistribution;
import simulator.ExperimentSimulator;

public class Experiments_N5_times_v1 {

	public static void main(String[] args) throws Exception {
		long masterSeed = args.length==0?0:Long.parseLong(args[0]);
		
		int NUMBER_OF_SIMs = 40;
		int MAX_EXPERIM_TIME = 80;
		
		
		for (int s=0;s<NUMBER_OF_SIMs;s++) {
			long seed = (masterSeed+1)*10000+s;
		
			for (int N : new int[] {5}) { //{5,10,20})
			    ExperimentSimulator es1 = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, 1, new KG_RSalgorithm(), 1000);
				es1.run(MAX_EXPERIM_TIME, false);
				
				for (int m : new int[] {1,2,5}) { //{1,2,3,4,5}							
					for (double arrivalTime : new double [] {0,1./m}) {
						for (double jobTime : new double [] {0.8,0.9,1,1.1,1.2}) {
							ExpDistribution arrDist = arrivalTime==0?null:new ExpDistribution(arrivalTime);
							ExpDistribution jobDist = new ExpDistribution(jobTime);
							int startQ = arrivalTime==0?1000:0;
							ExperimentSimulator ocba5T = new ExperimentSimulator(seed, N,arrDist,jobDist , 1, m, new OCBA_RSalgorithm(5), startQ);
							ocba5T.run(MAX_EXPERIM_TIME, false);
							ExperimentSimulator akg = new ExperimentSimulator(seed, N, arrDist,jobDist, m, 1, new AKG_RSalgorithm(), startQ);
							akg.run(MAX_EXPERIM_TIME, false);
						}
					}
				}
			}
		
		}
		 
	
	
	}

}
