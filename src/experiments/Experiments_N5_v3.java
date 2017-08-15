package experiments;

import java.io.BufferedReader;
import java.io.FileReader;

import rsalgos.AKG_RSalgorithm;
import rsalgos.AOCBA_RSalgorithm;
import rsalgos.KG_RSalgorithm;
import rsalgos.OCBA_RSalgorithm;
import rsalgos.SKG_RSalgorithm;
import simulator.ConstDistribution;
import simulator.Distribution;
import simulator.ExpDistribution;
import simulator.ExperimentSimulator;

public class Experiments_N5_v3 {

	public static void main(String[] args) throws Exception {	
		int MAX_EXPERIM_TIME = 80;
		
		BufferedReader br = new BufferedReader(new FileReader("parameters2_147880.txt.seed.txt"));
		
		while (br.ready()) {
			long seed = Long.parseLong(br.readLine());		
			for (int N : new int[] {5}) { //{5,10,20})
				for (Distribution experimentAvailability : new Distribution [] {null,new ExpDistribution(0.9)}) {
					int initialQueue = experimentAvailability==null?1000:0;
					ExperimentSimulator ocba1 = new ExperimentSimulator(seed, N, experimentAvailability, new ExpDistribution(1), 1, 1, new OCBA_RSalgorithm(2), initialQueue);
					ocba1.run(MAX_EXPERIM_TIME, false);
					ExperimentSimulator aocba = new ExperimentSimulator(seed, N, experimentAvailability, new ExpDistribution(1.0), 5, 1, new AOCBA_RSalgorithm(2), initialQueue);
					aocba.run(MAX_EXPERIM_TIME, false);
					ExperimentSimulator ocba = new ExperimentSimulator(seed, N, experimentAvailability, new ExpDistribution(1), 1, 5, new OCBA_RSalgorithm(2), initialQueue);
					ocba.run(MAX_EXPERIM_TIME, false);
				}
			}		
		}
		br.close();
	
	
	}

}
