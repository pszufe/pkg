package experiments;

import java.io.BufferedReader;
import java.io.FileReader;

import rsalgos.AKG_RSalgorithm;
import rsalgos.AOCBA_RSalgorithm;
import rsalgos.KG_RSalgorithm;
import rsalgos.OCBA_RSalgorithm;
import rsalgos.SKG_RSalgorithm;
import simulator.ConstDistribution;
import simulator.ExpDistribution;
import simulator.ExperimentSimulator;

public class Experiments_N5_v2 {

	public static void main(String[] args) throws Exception {
		long masterSeed = args.length==0?0:Long.parseLong(args[0]);
		
		int NUMBER_OF_SIMs = 40;
		int MAX_EXPERIM_TIME = 80;
		
		BufferedReader br = new BufferedReader(new FileReader("parameters2.txt.seed.txt"));
		
		while (br.ready()) {
			long seed = Long.parseLong(br.readLine());		
			for (int N : new int[] {5}) { //{5,10,20})
				for (int m : new int[] {1}) { //{1,2,3,4,5}				
					for (int ocbaPrecom : new int[]{2}) {
						ExperimentSimulator ocba = new ExperimentSimulator(seed, N, null, new ConstDistribution(1), 1, m, new OCBA_RSalgorithm(ocbaPrecom), 1000);
						ocba.run(MAX_EXPERIM_TIME, false);
					}					
				}
			}		
		}
		br.close();
	
	
	}

}
