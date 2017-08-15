package rsalgos;

import java.util.Arrays;

public class Combi {

	public static void main(String[] args) {
		
		
		int comb1[][] = allDSKG(3, 5);		
		
		int comb2[][] = allDSKG(5, 3);
		
		System.out.println("W1:");
		for (int[] line : comb1) {
			print(line);
		}
		
		System.out.println("W2:");
		for (int[] line : comb2) {
			print(line);
		}
		
		System.out.println();
		System.out.print("      W/N");
		for (int N=1;N<11;N++) {
			System.out.format("%11d", N);
		}
		
		System.out.println();
		for (int W=1;W<=15;W++) {
			System.out.format("%11d", W);
			for (int N=1;N<11;N++) {
				int combinationCount = allDSKG (N, W).length;
				System.out.format("%11d", combinationCount);				
			}
			System.out.println();
		}
	
	}
	

	
	/**
	 * 
	 * @param N alternatives
	 * @param a observations
	 * @param resA
	 * @return
	 */
	public static int[][] allDSKG (int N, int a) {  //,boolean resA
		int[][] res = new int[binomial(a-1+N, N-1)][];
		int A[]=new int[a];	//workers	
			
		//print(w);
		int s[] = new int[N];
		s[0] = A.length;
		//print(s);
		int found = 1;
		res[found-1] = s.clone();
	    //res[found-1] = (resA?A:s).clone();			
		boolean ok = N>1;	
		while (ok) {
			found++;
			for (int ix = A.length-1;ix>=0;ix--) {
				A[ix]++;
				if (A[ix] <= N-1) {
					break;
				} else {
					if (ix > 0) {
						A[ix] = 0;
					}
				}				
			}
			Arrays.fill(s, 0);
			for (int ix=0;ix<A.length;ix++) {
				if (ix > 0 && A[ix]<A[ix-1]) A[ix]=A[ix-1];
				s[A[ix]]++;
			}
			ok = (A[0] < N-1);
			res[found-1] = s.clone();
			//res[found-1] = (resA?A:s).clone();
		}		
		return res;
	}
	
	private static int binomial(int n, int k)
    {
        if (k>n-k)
            k=n-k;
 
        long b=1;
        for (int i=1, m=n; i<=k; i++, m--)
            b=b*m/i;
        if (b > Integer.MAX_VALUE) throw new UnsupportedOperationException("The value for binomial("+n+","+k+") = "+b+" is too large");
        return (int) b;
    }
	
	private static void print(int line[]) {
		for (int t : line) {
			System.out.print(t+"\t");
		}
		System.out.println();
	}

}
