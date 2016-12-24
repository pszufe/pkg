package visualizations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;

public class DistinctMasterSeeds {
	public static void main (String args[]) throws Exception {
		String fileName = "C:\\!BIBLIOTEKA\\WinterSim\\AKG\\test\\parametersN10.txt";
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line[];
		HashSet<Long> vals = new HashSet<>();  
		while (br.ready()) {
			line = br.readLine().split("\\t");
			long val = Long.parseLong(line[0]);
			if (! vals.contains(val)) {
				vals.add(val);
				System.out.println(val);
			}
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName+".seed.txt"));
		for (Long val : vals) {
			bw.write(val.toString());
			bw.newLine();
		}
	}
}
