package experiments;
/*
bash KissCluster/create_cluster.sh Experiments_N100_W10_AKG_OCBA_AOCBA us-east-2 "java -server -Xmx1500M -cp lib/*:bin experiments.Experiments_N100_W10_AKG_OCBA_AOCBA" app/ s3://szufel-sim-ohio
*/


import rsalgos.AKG_RSalgorithm;
import rsalgos.AOCBA_RSalgorithm;
import rsalgos.KG_RSalgorithm;
import rsalgos.OCBA_RSalgorithm;
import rsalgos.SKG_RSalgorithm;
import simulator.ConstDistribution;
import simulator.ExpDistribution;
import simulator.ExperimentSimulator;

public class Experiments_N20_W5_AKG_OCBA_AOCBA {

	public static void main(String[] args) throws Exception {
		long masterSeed =Long.parseLong(args[0]);
		long time = System.currentTimeMillis();
		int NUMBER_OF_SIMs = 50;
		int MAX_EXPERIM_TIME = 150;
		int W = 5;
		for (int s=0;s<NUMBER_OF_SIMs;s++) {
			long seed = (masterSeed+1)*10000+s;
			int N  = 20;

			ExperimentSimulator ocba99 = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, 1, new OCBA_RSalgorithm(99), 1000);
			ocba99.run(MAX_EXPERIM_TIME, false);
			
			ExperimentSimulator aocba = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), W, 1, new AOCBA_RSalgorithm(2), 1000);
			aocba.run(MAX_EXPERIM_TIME, false);

			ExperimentSimulator ocba = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, W, new OCBA_RSalgorithm(2), 1000);
			ocba.run(MAX_EXPERIM_TIME, false);
			
			ExperimentSimulator akg = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), W, 1, new AKG_RSalgorithm(), 1000);
			akg.run(MAX_EXPERIM_TIME, false);
						
			ExperimentSimulator kg = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, 1, new KG_RSalgorithm(), 1000);
			kg.run(MAX_EXPERIM_TIME, false);
			
		
		}
		System.out.println(System.currentTimeMillis()-time);
		 
	
	
	}

}
