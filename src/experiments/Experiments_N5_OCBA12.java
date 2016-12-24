package experiments;

import rsalgos.AKG_RSalgorithm;
import rsalgos.AOCBA_RSalgorithm;
import rsalgos.KG_RSalgorithm;
import rsalgos.OCBA_RSalgorithm;
import rsalgos.SKG_RSalgorithm;
import simulator.ConstDistribution;
import simulator.ExpDistribution;
import simulator.ExperimentSimulator;

public class Experiments_N5_OCBA12 {

	public static void main(String[] args) throws Exception {
		long masterSeed = args.length==0?0:Long.parseLong(args[0]);
		long start = System.currentTimeMillis();
		int NUMBER_OF_SIMs = 1;
		int MAX_EXPERIM_TIME = 80;
				
		for (int s=0;s<NUMBER_OF_SIMs;s++) {
			long seed = (masterSeed+1)*10000+s;
		
			for (int N : new int[] {5}) { //{5,10,20})
				ExperimentSimulator ocba = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, 10, new OCBA_RSalgorithm(2), 1000);
				ocba.run(MAX_EXPERIM_TIME, false);
				
				ExperimentSimulator aocba = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, 10, new AOCBA_RSalgorithm(2), 1000);
				aocba.run(MAX_EXPERIM_TIME, false);
				
				ExperimentSimulator akg = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 10, 1, new AKG_RSalgorithm(), 1000);
				akg.run(MAX_EXPERIM_TIME, false);
				
				ExperimentSimulator skg = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, 10, new SKG_RSalgorithm(), 1000);
				skg.run(MAX_EXPERIM_TIME, false);
				
				
			}
		
		}
		System.out.println(System.currentTimeMillis()-start);
		 
	
	
	}

}
