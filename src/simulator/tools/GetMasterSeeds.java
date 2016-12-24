package simulator.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class GetMasterSeeds {

	public static void main(String[] args) throws Exception {
		String fileIn = "C:\\!BIBLIOTEKA\\WinterSim\\AKG\\OCBA_1_prec\\parameters.csv";
		String linia;
		
		BufferedReader br = new BufferedReader(new FileReader(fileIn));
		HashSet<Long> seeds = new HashSet<>();
		Long value;
		while (br.ready()) {
			linia = br.readLine();
			if (linia.length()>2) {
				value = new Long(linia.substring(0, linia.indexOf('\t')));
				if (!seeds.contains(value)) seeds.add(value);
			}			
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileIn+".seed.txt"));
		for (Long val : seeds) {
			bw.write(val.toString());
			bw.newLine();
		}
		bw.close();
	}

}
