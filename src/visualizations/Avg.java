package visualizations;
/**
 java -server -Xmx1500M -cp app/lib/*:app/bin visualizations.Avg results.csv > fileout.txt
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Avg {

	private static final int [] avgGroup = new int[] {1,2,3,4,5,6,7,8,9};
	private static final int [] avgAvg = new int[] {11,12,14,15,16};

	
	private static String getKey(String line[]) {
		StringBuilder key = new StringBuilder();
		for (int i =0;i<avgGroup.length;i++) {
			key.append(line[avgGroup[i]]);
			key.append("\t");
		}
		return key.toString();
	}
	
	public static void main(String[] args) throws Exception {
		File inputFile;
		if (args.length > 0) {
			inputFile = new File(args[0]);
		} else {
			System.out.println("Input file:");
			inputFile =  new File(new BufferedReader(new InputStreamReader(System.in)).readLine());
		}
		
			
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String[] header = br.readLine().split("\t");
		String[] line;
		String key;
		
		double avg[];
		ArrayList<String> keys = new ArrayList<>();
		HashMap<String, double[]> data = new HashMap<>();
		HashMap<String, double[]> stdDevs = new HashMap<>();
		int lineNo = 0;
		while (br.ready()) {
			line = br.readLine().split("\t");
			if (line.length > 14) {
				key = getKey(line);
				if (data.containsKey(key)) {
					avg = data.get(key);
				} else {
					keys.add(key);
					avg = new double[avgAvg.length+1];
					data.put(key, avg);
					stdDevs.put(key, new double[avgAvg.length+1]);
				}
				for (int i=0;i<avgAvg.length;i++)
					avg[i] += Double.parseDouble(line[avgAvg[i]]);
				avg[avgAvg.length] += 1;
			}
			lineNo++;
			if (lineNo % 1000000 == 0) {
				System.err.print(".");
				System.err.flush();
			}
			
		}
		br.close();
		
		for (String key0 : keys) {
			avg = data.get(key0);			
			for (int i = 0;i<avg.length-1;i++) {
				avg[i] = avg[i]/avg[avg.length-1];
			}			
		}
		
		double stdDev[];
		//double mySum = 0;
		//int myCount = 0;
		br = new BufferedReader(new FileReader(inputFile));
		br.readLine();
		System.err.println("");
		while (br.ready()) {
			line = br.readLine().split("\t");
			if (line.length > 14) {
				key = getKey(line);
				avg = data.get(key);
				stdDev = stdDevs.get(key);
				for (int i=0;i<avgAvg.length;i++) {
					//if (i==avgAvg.length-1) {
						//System.out.println(Math.pow((Double.parseDouble(line[avgAvg[i]])-avg[i]),2));
						//myCount++;
						//mySum += Math.pow((Double.parseDouble(line[avgAvg[i]])-avg[i]),2);
					//}
					stdDev[i] += Math.pow((Double.parseDouble(line[avgAvg[i]])-avg[i]),2) ;
				}
				stdDev[avgAvg.length] += 1;
			}
			lineNo++;
			if (lineNo % 1000000 == 0) {
				System.err.print(".");
				System.err.flush();
			}
		}
		System.err.println("");
		
		for (String key0: keys) {
			stdDev = stdDevs.get(key0);						
			for (int i = 0;i<stdDev.length-1;i++) {
				stdDev[i] = Math.sqrt(stdDev[i]/(stdDev[stdDev.length-1]-1));
			}			
		}
		br.close();
		
		for (int i=0;i<avgGroup.length;i++) System.out.print(header[avgGroup[i]]+"\t");
		for (int i=0;i< avgAvg.length;i++) System.out.print("avg_"+header[avgAvg[i]]+"\t"+"std_"+header[avgAvg[i]]+"\t");
		System.out.println("COUNT");
		for (String key0 : keys) {
			avg = data.get(key0);
			stdDev = stdDevs.get(key0);
			System.out.print(key0);
			for (int i=0;i< avgAvg.length;i++) {
				System.out.print(avg[i]+"\t");
				System.out.print(stdDev[i]+"\t");
			}
			
			System.out.println(avg[avgAvg.length]);
		}
	}

}
