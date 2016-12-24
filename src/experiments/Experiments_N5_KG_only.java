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

public class Experiments_N5_KG_only {

	public static void main(String[] args) throws Exception {

		int MAX_EXPERIM_TIME = 80;
		
		BufferedReader br = new BufferedReader(new FileReader("test/parametersN10.txt.seed.txt"));
		
		while (br.ready()) {
			long seed = Long.parseLong(br.readLine());		
			ExperimentSimulator kg = new ExperimentSimulator(seed, 5, null, new ConstDistribution(1), 1, 1, new KG_RSalgorithm(), 1000);
			kg.run(MAX_EXPERIM_TIME, false);
		}
		br.close();
	
	
	}

}
