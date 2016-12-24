package rsalgos;

import java.util.Arrays;
/**
 * The tools used by the AKG algorithm
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */

public class Tools {
	public static double[] maxOfOtherElems (double d[]) {		
		double max = -1e99;
		double secondMax = -1e99;		
		int maxId = -1;
		for (int i=0;i<d.length;i++) {
			if (d[i] > max) {
				max = d[i];
				maxId = i;
			}
		}		
		for (int i=0;i<d.length;i++) {
			if (d[i] > secondMax && i!=maxId) secondMax = d[i];
		}
		double res[] = new double[d.length];
		Arrays.fill(res, max);
		res[maxId] = secondMax;
		return res;
	}
	
	public static double[] maxOfOtherElems (double d[], int s_list[]) {
		double max = -1e99;
		double secondMax = -1e99;		
		int maxId = -1;
		for (int i=0;i<d.length;i++) {
			if (d[i] > max && s_list[i]==0) {
				max = d[i];
				maxId = i;
			}
		}		
		for (int i=0;i<d.length;i++) {
			if (d[i] > secondMax && i!=maxId && s_list[i]==0) secondMax = d[i];
		}
		double res[] = new double[d.length];
		Arrays.fill(res, max);
		if (maxId >= 0) res[maxId] = secondMax;
		return res;
	}
	
	
	public static double min (double[] list, double ... list2) {
		double res = list[0];
		for (int i=1;i<list.length;i++) 
			if (list[i] < res) res = list[i];
		for (int i=0;i<list2.length;i++) 
			if (list2[i] < res) res = list2[i];
		return res;
	}
	
	public static String str (double[] list, double ... list2) {
		StringBuilder s = new StringBuilder();
		int len = list.length+list2.length;
		for (int i=0;i<list.length;i++) 
			s.append(list[i]==-1e99?-1e99: ((long)(list[i]*1e5))/1e5+((i<len-1)?",":""));
		for (int i=0;i<list2.length;i++) 
			s.append(list[i]==-1e99?-1e99:((long)(list2[i]*1e5))/1e5+((i<list2.length-1)?",":""));
		return s.toString();
	}
	public static String str (int[] list, int ... list2) {
		StringBuilder s = new StringBuilder();
		int len = list.length+list2.length;
		for (int i=0;i<list.length;i++) 
			s.append(list[i]+((i<len-1)?",":""));
		for (int i=0;i<list2.length;i++) 
			s.append(list2[i]+((i<list2.length-1)?",":""));
		return s.toString();
	}

	
}
