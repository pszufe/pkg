package experiments;
/*
bash KissCluster/kissc-start.sh --region us-east-2 --command "java -server -Xmx1500M -cp lib/*:bin experiments.Experiments_N20_W1_10_AKG_OCBA_AOCBA" --folder ./app --s3_bucket s3://szufel-sim-ohio PKG3

*/


import rsalgos.AKG_RSalgorithm;
import rsalgos.AOCBA_RSalgorithm;
import rsalgos.KG_RSalgorithm;
import rsalgos.OCBA_RSalgorithm;
import rsalgos.SKG_RSalgorithm;
import simulator.ConstDistribution;
import simulator.ExpDistribution;
import simulator.ExperimentSimulator;

public class Experiments_N20_W1_10_AKG_OCBA_AOCBA {

	public static void main(String[] args) throws Exception {
		long masterSeed = Long.parseLong(args[0]);
		long time = System.currentTimeMillis();
		int NUMBER_OF_SIMs = 5;
		int N  = 20;
		int MAX_EXPERIM_TIME = 200;
		for (int W =1; W<=10;W++) {
			for (int s=0;s<NUMBER_OF_SIMs;s++) {
				long seed = (masterSeed+1)*10000+s;
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
		}
		System.out.println(System.currentTimeMillis()-time);
	
	}

}
