package experiments;

import rsalgos.OCBA_RSalgorithm;
import rsalgos.SKG_RSalgorithm;
import simulator.ConstDistribution;
import simulator.ExperimentSimulator;

public class Experiments_N20_W5_SKGpre_OCBA2 {

	public static void main(String[] args) throws Exception {
		long masterSeed = 0;
		
		int NUMBER_OF_SIMs = 1;
		int MAX_EXPERIM_BUDGET = 50;
		int W = 5;
		int N  = 20;
		for (int s=0;s<NUMBER_OF_SIMs;s++) {
			long seed = (masterSeed+1)*10000+s;			
			
			ExperimentSimulator skg = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, W, new SKG_RSalgorithm(2), 1000);
			skg.run(MAX_EXPERIM_BUDGET, false);
			
			
		
		}
		 
	
	
	}

}
