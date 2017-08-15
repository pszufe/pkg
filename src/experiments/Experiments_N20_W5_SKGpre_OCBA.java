package experiments;

import rsalgos.OCBA_RSalgorithm;
import rsalgos.SKG_RSalgorithm;
import simulator.ConstDistribution;
import simulator.ExperimentSimulator;
/*
  bash KissCluster/create_cluster.sh Experiments_N20_W5_SKGpre_OCBA us-east-2 "java -server -Xmx1500M -cp lib/*:bin experiments.Experiments_N20_W5_SKGpre_OCBA" app/ s3://szufel-sim-ohio
 */
public class Experiments_N20_W5_SKGpre_OCBA {

	public static void main(String[] args) throws Exception {
		long masterSeed = Long.parseLong(args[0]);
		
		int NUMBER_OF_SIMs = 100;
		int MAX_EXPERIM_BUDGET = 250;
		int W = 5;
		int N  = 20;
		for (int s=0;s<NUMBER_OF_SIMs;s++) {
			long seed = (masterSeed+1)*10000+s;
			

			ExperimentSimulator ocba99 = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, 1, new OCBA_RSalgorithm(99), 1000);
			ocba99.run(MAX_EXPERIM_BUDGET, false);
			
			ExperimentSimulator ocba = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, W, new OCBA_RSalgorithm(2), 1000);
			ocba.run(MAX_EXPERIM_BUDGET, false);
			
			ExperimentSimulator skg = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, W, new SKG_RSalgorithm(2), 1000);
			skg.run(MAX_EXPERIM_BUDGET, false);
			
			
		
		}
		 
	
	
	}

}
