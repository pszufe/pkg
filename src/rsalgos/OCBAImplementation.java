package rsalgos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

import simulator.RSpoint;

public class OCBAImplementation {


    /**
     * Determines the best design based on current simulation
	 * results
     * @param mean temporary array for sample mean of design i, i=0,1,...,ND-1
     */
	private static int best(double[] mean) {
		int min_index = 0;
		for (int i = 1; i < mean.length; i++) {
			if (mean[i] < mean[min_index]) {
				min_index = i;
			}
		}
		return min_index;
	}

	/**
	 * Determines the second best design based on current
	 * simulation results
	 * 
	 * @param t_s_mean[i]: temporary array for sample mean of design i, i=0,1,...,ND-1
	 * @param b current best design determined by function
	 * @return
	 */
	private static int second_best(double[] t_s_mean, int b)	{
		int i, second_index;
		if (b == 0)
			second_index = 1;
		else
			second_index = 0;
		for (i = 0; i < t_s_mean.length; i++) {
			if (t_s_mean[i] < t_s_mean[second_index] && i != b) {
				second_index = i;
			}
		}

		return second_index;
	}	
	

	/**
	 * 
	 * @param mean sample mean of design i, i=0,1,...,ND-1
	 * @param var sample variance of design i, i=0,1,...,ND-1
	 * @param m number of simulation replications of design i, i=0,1,...,ND-1
	 * @param M the additional simulation budget 
	 * @return additional number of simulation replications assigned to design i
	 */
	public static int[] ocba(double[] mean, double[] var, int[] m, final int M) {
		//System.out.println(mean.length+","+var.length+","+m.length+","+M);
		//for (int i=0;i<mean.length;i++) 
		//	System.out.println("mean["+i+"]="+mean[i]);
		
		int nd = mean.length;
		
		int i, s, budget, remainingBudget;
		int b;

		double[] ratio = new double[nd]; /* Ni/Ns */
		double totalRatio;
		double temp = 0;
		boolean[] moreRun = new boolean[nd];
		

		/* MAX problem */
		for (i = 0; i < nd; i++)
			mean[i] = -mean[i];

		b = best(mean);
		s = second_best(mean, b);
		ratio[s] = 1;

		for (i = 0; i < nd; i++) {
			if (i != s && i != b) {
				if (mean[b] == mean[i]) {
					ratio[i] = 1; //avoid division by 0
				} else {
					ratio[i] = Math.pow((mean[b] - mean[s]) / (mean[b] - mean[i]), 2) * var[i] / var[s];
				}
			} /* calculate ratio of Ni/Ns */
			if (i != b)
				temp += ratio[i] * ratio[i] / var[i];
		}
		ratio[b] = Math.sqrt(var[b] * temp);

		//all ratios
		/* calculate NB */
		budget = M;
		for (i = 0; i < nd; i++) {
			budget += m[i];
			moreRun[i] = true;
		}
		remainingBudget = budget;

		boolean moreAllocation = true;
		int[] z = new int[nd];
		double zdouble[]= new double[nd];
		while (moreAllocation) {
			moreAllocation = false;
			totalRatio = 0;
			for (i = 0; i < nd; i++) {
				if (moreRun[i])
					totalRatio += ratio[i];
			}
			//System.out.println("totalratio="+totalRatio);
			
			for (i = 0; i < nd; i++) {
				
				if (moreRun[i]) {
					zdouble[i] = (remainingBudget * ratio[i] / totalRatio);
					z[i] = (int) zdouble[i];
				}
				// disable those design which have been run too much 
				if (z[i] < m[i]) {
					z[i] = m[i];
					moreRun[i] = false;
					moreAllocation = true;// && originalRecursiveAllocations;
				}
			}
			if (moreAllocation) {
				remainingBudget = budget;
				for (i = 0; i < nd; i++) {
					if (moreRun[i] == false)
						remainingBudget -= z[i];
				}
			}
		} 
		
		moreAllocation = true;
		int sumD = 0;
		while (sumD != M) {
			sumD = 0;
			for (i = 0; i < nd; i++) {				
				sumD += z[i]-m[i];				
			}
			if (sumD < M) {
				double bestD = -1E99;
				int bestIx = -1;
				for (i = 0; i < nd; i++) {
					//System.out.println("i="+i+" "+zdouble[i]+","+z[i]+" "+(zdouble[i]-z[i]));
					if (zdouble[i]-z[i] > bestD) {
						bestD = zdouble[i]-z[i];
						bestIx = i;
					}		
				}				
				z[bestIx]++;
			} else if (sumD > M) {
				double bestD = 1E99;
				int bestIx = -1;
				for (i = 0; i < nd; i++) {
					if (zdouble[i]-z[i] < bestD) {
						bestD = zdouble[i]-z[i];
						bestIx = i;
					}					
				}
				z[bestIx]--;
			}
			
		}
		
		
		
		//remainingBudget = z[0];
		//for (i = 1; i < nd; i++)
		//	remainingBudget += z[i];
		//z[b] += (budget
		//		- remainingBudget); /* give the difference to design b */
		
		
		int d[] = new int[nd];
		
		for (i = 0; i < nd; i++)
			d[i] = z[i] - m[i];
		return d;
	}
	   


	
	 private static class Point {
	    public double u; 
	    public final double sig;
		
		public final ArrayList<Double> ys;
		NormalDistribution normalD;
		public double ys_sum;

		public double getMeanY() {		
			if (ys.size()==0) return 0.0;
			return ys_sum/ys.size();
		}
		
		public double getVarianceY() {
			if (ys.size()<=1) return 1.0;
			double mean = getMeanY();
			double temp = 0;
	        for(double y:ys)
	            temp += (y-mean)*(y-mean);
	        return temp/(ys.size()-1);
		}
		Random r;
		Point (Random r, double u) {		
			this.r=r;
			this.u = u;
			sig = 1;
			normalD = new NormalDistribution(u, sig);
			ys = new ArrayList<Double>();
			ys_sum = 0.0;
		
		}
		
		private double y;
		public double get_y() {
			y = normalD.inverseCumulativeProbability(r.nextDouble());
			ys.add(y);
			ys_sum +=y;
			return y;
		}
		
		public int getNoMeasures () {
			return ys.size();
		}
	}
	 

	public static RSpoint[] getOcba(RSpoint[] points, int m ) {
		RSpoint[] res = new RSpoint[m];
		double mean[] = new double[points.length];
		double var[] = new double[points.length];			
		int replications[] = new int[points.length];
		for (int i=0;i<points.length;i++) {
			mean[i] = points[i].getMeanY();
			replications[i]=points[i].getNoMeasures();
			var[i] = points[i].sig*points[i].sig; //points[i].getVarianceY();
		}

		int d[] = ocba(mean, var, replications, m);		
		
		int aix = 0, dix=0;
		while (aix < m) {
			while (d[dix] ==0) {
				dix++;
			}
			d[dix]--;
			res[aix]=points[dix];
			aix++;
		}
		return res;
	}
 

	public static RSpoint[] getAOcba(RSpoint[] points, int m ) {
		RSpoint[] res = new RSpoint[m];
		double mean[] = new double[points.length];
		double var[] = new double[points.length];			
		int replications[] = new int[points.length];
		
		for (int i=0;i<points.length;i++) {			
			mean[i] = points[i].getMeanY();
			//System.out.println("OCBA getAOcba mean["+i+"]="+mean[i]+"  "+points[i].getNoMeasures()+ " "+points[i].getS());
			replications[i]=points[i].getNoMeasures()+points[i].getS();			
			var[i] = points[i].sig*points[i].sig; //points[i].getVarianceY();
		}

		int d[] = ocba(mean, var, replications, m);		
		
		int aix = 0, dix=0;
		while (aix < m) {
			while (d[dix] ==0) {
				dix++;
			}
			d[dix]--;
			res[aix]=points[dix];
			aix++;
		}
		return res;
	}
	
	
	public static void main (String args[]) {
		Random r = new Random(0);
		Point points[] = new Point[] {
				new Point(r, 0.2),
				new Point(r, 2.7),
				new Point(r, -0.2),
				new Point(r, -2.0),
				new Point(r, -0.03) };
		int N = 5;

		for (int i=0;i<N;i++) {	
			System.out.println("i="+i+" realval "+points[i].u);
		}
		
		
		for (int i=0;i<N;i++) {
			System.out.print("i="+i+" First 5:");
			for (int s=0;s<5;s++) {
				System.out.print("\t"+points[i].get_y());
			}
			System.out.println("\tMean:\t"+points[i].getMeanY());
		}
		
		for (int step=26;step<=1000;step++) {
			double mean[] = new double[N];
			double var[] = new double[N];
			Arrays.fill(var, 1);
			int replications[] = new int[N];
			for (int i=0;i<N;i++) {
				mean[i] = points[i].getMeanY();
				replications[i]=points[i].getNoMeasures();
				var[i] = 1; //points[i].getVarianceY();
				if (step % 1==0)	System.out.println("step="+step+" i=\t"+i+"\treps=\t"+replications[i]+"\tavg=\t"+mean[i]+"\tvar=\t"+points[i].getVarianceY());
			}

			int an[] = ocba(mean, var, replications, 1);
			if (step % 1==0) {
				for (int k : an) {
					System.out.print(" "+k);				
				}
				System.out.println();
			}
			for (int i=0;i<N;i++) {
				for (int a=0;a<an[i];a++) 
					points[i].get_y();
			}
		}		
	}
}
