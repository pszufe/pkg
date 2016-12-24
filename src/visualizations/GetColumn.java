package visualizations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class GetColumn {


	public static void main(String[] args) throws Exception {
		String colName = "bestBinary";
		File inputFile;
		if (args.length > 0) {
			inputFile = new File(args[0]);
		} else {
			System.out.println("Input file:");
			inputFile =  new File(new BufferedReader(new InputStreamReader(System.in)).readLine());
		}
		
			
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(inputFile.getAbsolutePath()+"_"+colName+".txt"));
		bw.write(colName);
		bw.newLine();
		String[] header = br.readLine().split("\t");
		
		int ix = -1;
		for (int i=0;i<header.length;i++) {
			if (header[i].equals(colName))
				ix = i;
		}
		while (br.ready()) {
			bw.write(br.readLine().split("\\t")[ix]);
			bw.newLine();
		}
		
		br.close();
		bw.close();
	}

}
