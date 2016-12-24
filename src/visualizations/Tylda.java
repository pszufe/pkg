package visualizations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

public class Tylda {

	/**
	 * This code extracts data from logged output streams from simulations. 
	 * 
	 * @param args
	 * @throws Exception
	 * @author PSZ
	 */
	public static void main(String[] args) throws Exception {
		
		
		String resultsFolder;
		if (args.length > 0) {
			resultsFolder = args[0];
		} else {
			System.out.println("Folder:");
			resultsFolder =  new BufferedReader(new InputStreamReader(System.in)).readLine();
		}
		
		
		File fs[] = new File(resultsFolder).listFiles();
		String[] prefixes = {"~","`"};
		String[] names = {"parameters","results"};
		HashSet<String> parametersLines; 
		BufferedWriter bws[] = new BufferedWriter[names.length];
		for (int ix=0;ix<prefixes.length;ix++) {
			bws[ix] = new BufferedWriter(new FileWriter(new File(resultsFolder,names[ix]+".csv")));
		}
		int ix;
		boolean header0[] = new boolean[prefixes.length];
		Arrays.fill(header0, true);
		for (File f : fs) {
			if (f.isFile() && (f.getName().endsWith(".txt") || f.getName().endsWith(".gz")) ) {
				System.out.println(f);
				FileInputStream fis = new FileInputStream(f);
				InputStream is = fis;
				if (f.getName().endsWith(".gz") )
					is = new GZIPInputStream(fis);
				
				BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("cp1250")));
				boolean header[] = new boolean[prefixes.length];
				Arrays.fill(header, true);
				String linia;
				parametersLines = new HashSet<>();
				int liniaNo = 0;
				while (br.ready()) {					
					linia = br.readLine();
					liniaNo++;
					if (linia == null) {
						System.out.println("NULL!!! ["+liniaNo+"] "+f.getName());
					} else {					
					    ix = -1;
					    for (int i=0;i<prefixes.length;i++) {
					    	if (linia.startsWith(prefixes[i])) {
					    		ix = i;
					    		break;
					    	}
					    }
					    
						if (ix>=0) {
							if (header[ix] && header0[ix]) {								
								bws[ix].write(linia.substring(2));
								bws[ix].newLine();
								header0[ix] = false;
							}
							if (!header[ix]) {
								if (!names[ix].equals("parameters") || !parametersLines.contains(linia)) {
									bws[ix].write(linia.substring(2));
									bws[ix].newLine();
									if (names[ix].equals("parameters")) parametersLines.add(linia);
								}
								
							}
							header[ix] = false;
						}
					}
				}
				br.close();
			}
		}
		
		for (ix=0;ix<prefixes.length;ix++) {
			bws[ix].close();
		}
		
		
	}
	
	
	static HashMap<String,HashMap<String,String>> readCsv(File f, int pkColumns[]) throws Exception {
		HashMap<String,HashMap<String,String>> res = new HashMap<String,HashMap<String,String>>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String[] header = br.readLine().split(";");
		String[] line;
		StringBuilder key;
		while (br.ready()) {
			line = br.readLine().split(";");
			if (line.length!=header.length) {
				throw new Exception("Bad column count "+line.length+"!="+header.length);
			}
			key = new StringBuilder();
			for (int i=0;i<pkColumns.length;i++) {
				key.append(line[i]);
				if (i<pkColumns.length-1) key.append("_");
			}
			HashMap<String, String> row = new HashMap<>();
			for (int c=0;c<header.length;c++) {
				row.put(header[c], line[c]);
			}
			res.put(key.toString(), row);
		}
		br.close();
		return res;
		
	}

}
