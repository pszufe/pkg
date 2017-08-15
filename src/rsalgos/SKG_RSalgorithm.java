package rsalgos;
/**
 * Implementation of the AKG algorithm
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;

import simulator.RSpoint;
import simulator.tools.CalculateEv;
import simulator.tools.RandomStreamFactory;

public class SKG_RSalgorithm implements PRSalgorithm, HasPreselection {

	private int preselectionDistance  = -1;
	public SKG_RSalgorithm() {
		super();
	}
	public SKG_RSalgorithm(int preselectionDistance) {
		super();
		this.preselectionDistance = preselectionDistance;
	}
	
	
	
	protected double sigmas2[] = null; //priors  
	protected double mu[] = null; //priors
	protected int N;
	protected double vkg[];
	protected double best_vkg;
	private UnivariateFunction f = new UnivariateFunction() {
		private NormalDistribution normal = new NormalDistribution();
		@Override
		public double value(double x) {			
			return x*normal.cumulativeProbability(x)+normal.density(x);
		}
	};
	private final NormalDistribution normal = new NormalDistribution();
	
	protected boolean outOfNumericalAccuracy;
	protected final double numericalAccuracyDistTreshold = 1E-13;
	protected boolean ocbaFallback;
	
	private int dspaceFull[][];
	
    protected int roundNo;
	@Override
	public RSpoint[] getPointsInit_k_0(RSpoint[] points, int wNo, int a, int m) {
		if (wNo!=0 || a >1) {
			throw new IllegalArgumentException("SKG has exactly one worker (with many pallarel slots)");
		}
		roundNo = 0;
		outOfNumericalAccuracy = false;
		ocbaFallback=false;
		RSpoint[] res = new RSpoint[m];
		if (sigmas2==null) {
			N = points.length;
			sigmas2 = new double[N];
			Arrays.fill(sigmas2, 1); //initial beliefs equal to real values
			mu =  new double[N];
			Arrays.fill(mu, 0); //initial beliefs equal to 0
			this.dspaceFull = Combi.allDSKG(N, m); //all possible decisions with N points and m slots
		}
		//in initialization just evenly split points across available workers, since all priors are equal and no other info is available
		for (int w=0;w<m;w++) {
			res[w]=points[(w+wNo*m) % points.length];   
		}
		
		return res;		
	}
	
	
	
	BufferedWriter log=null;
	//ParallelLog log = null;
	
	/**
	 * Makes a OCBA preselection
	 * @param preselectionDistance - distance from OCBA-optimal point
	 * @param m number of OCBA workers
	 * @param points
	 * @param dspaceFull
	 * @return
	 */
	private static int[][] makePreselection(int preselectionDistance, int m, RSpoint[] points, int dspaceFull[][] ) {
		ArrayList<int[]> res_ = new ArrayList<>();
		
		
		int [] ocbarec = new int[points.length];
		
		boolean ocbaMinData = true;
		
		for (RSpoint p : points) ocbaMinData = ocbaMinData && p.getNoMeasures()>1;
		
		if (ocbaMinData) {
			RSpoint[] alloc = OCBAImplementation.getOcba(points, m);	
			for (RSpoint point : alloc) {
				ocbarec[point.i]++;
			}
		} else {
			int m_found = 0;
			for (int i=0;i<points.length && m_found < m;i++) {
				if (points[i].getNoMeasures() == 0) {
					m_found++;
					ocbarec[i]++;
				}
			}			
			for (int i=0;i<points.length && m_found < m;i++) {
				if (points[i].getNoMeasures() <= 1) {
					m_found++;
					ocbarec[i]++;
				}
			}
			for (int k=0;k<m-m_found;k++) {
				ocbarec[k % points.length]++;
			}
			
		}
		
		
		for (int[] row : dspaceFull ) {
			int distance = 0;
			for (int i=0;i<row.length;i++) {
				distance += Math.abs(row[i]-ocbarec[i]);
			}
			if (distance <= preselectionDistance) 
				res_.add(row);
		}		
		
		
		
		int[][] res = res_.toArray(new int[res_.size()][]);
		
		/**
		System.out.println("ocbarec with ocbaMinData="+ocbaMinData);
		printNums(ocbarec);
		println(ocbarec);
		System.out.println();**/
		/**System.out.println("Recommendations ");
		for (int [] row : res) {
			printNums(row);
			println(row);
		}
		**/
		
		return (res);
	}
	
	private static void printNums(int[] vals) {		
		for (int i=0;i<vals.length;i++) {
			for (int j=0;j<vals[i];j++) {
				System.out.print(i+",");
			}
		}
		System.out.print("\t");	
	}
	
	
	private static void println(int line[]) {		
		for (int t : line) {
			System.out.print(t+"\t");
		}
		System.out.println();
	}
	
	@Override
	public RSpoint[] getNextPoints(double[] x, RSpoint[] pointsEval, RSpoint[] points, int wNo, boolean debug) {
					
			
		
		int m = pointsEval.length; //number of parallel slots for this worker  
		roundNo++;
		RSpoint[] res;
		
		final int N = points.length;

		
		if (!outOfNumericalAccuracy) {
			res = new RSpoint[m];
			final int m_[] = new int[N];
			for (int aix=0;aix<m;aix++) {
				m_[pointsEval[aix].i]++;
			}		
			final double sigmas2mi[] = new double[N]; //sigma^2 w at time m
			final double mu_m[] = new double[N]; //mi at time m
			double sigma2e = 1;
			for (int i=0;i<N;i++) {
				double x_j_i_sum = 0;
				for (int j=0;j<m;j++) {
					if (pointsEval[j].i==i) x_j_i_sum += x[j]; 
				}
				
				sigmas2mi[i] = 1/(1/sigmas2[i] + m_[i]/sigma2e);
				mu_m[i] = (mu[i]/sigmas2[i]+x_j_i_sum/sigma2e)*sigmas2mi[i];
			}
			
			
			int xks = -1;
			double bestArg = -1e99;
			double worstArg = 1e99;
			double arg;
			
			int dspace[][] = (preselectionDistance < 0)?dspaceFull:makePreselection(preselectionDistance,m,points,dspaceFull);
			
			double max_mumi = max(mu_m);
			vkg = new double[dspace.length];
			final double one_over_sqrt_sigma_diff[] = new double[N];
			for (int dix=0;dix<dspace.length;dix++) {
				int d[] = dspace[dix];
				// sqrt(\sigma^2_{(m_i),i}-\sigma^2_{(m_i+d_i),i})			
				double muk_maxOfOthers_d = -1E99;
				for (int i=0;i<N;i++) {				
					//sigma_diff[i] = Math.sqrt(sigmas2mi[i]*sigmas2mi[i]-Math.pow(1/(1/sigmas2mi[i] + d[i]/sigma2e), 2));
					one_over_sqrt_sigma_diff[i] = 1/Math.sqrt(sigmas2mi[i]-1/(1/sigmas2mi[i]+d[i]/sigma2e));
					if (d[i] == 0) muk_maxOfOthers_d = Math.max(muk_maxOfOthers_d, mu_m[i]);			}		
				
				arg = calculateSkgEv(one_over_sqrt_sigma_diff, mu_m, muk_maxOfOthers_d,d)-max_mumi;
				
				vkg[dix] = arg;
				if (arg > bestArg) {
					bestArg = arg;
					xks = dix;
				}
				if (arg<worstArg) worstArg=arg;
				if (log!=null) {
					try {
						log.write(roundNo+"\t"+arg);
						log.write("\tmu_m={"+toStr(mu_m,false)+"}");
						log.write("\tsigmas2_mi={"+toStr(sigmas2mi,false)+"}");
						for (int d_ : d) {
							log.write("\t"+d_);
						}
						log.write("\r\n");
					} catch (Exception ex) {}
				}
			}
			this.best_vkg = bestArg;
			sigmas2=sigmas2mi;
			mu = mu_m;
			int aix = 0;
			int dix = 0;
			int d[] = dspace[xks].clone();
			while (aix < m) {
				while (d[dix] ==0) {
					dix++;
				}
				d[dix]--;
				res[aix]=points[dix];
				aix++;
			}			
			if (bestArg-worstArg < numericalAccuracyDistTreshold) outOfNumericalAccuracy = true;
		} else {
			ocbaFallback = true;
			res = OCBAImplementation.getOcba(points, m);
		}
		return res;
	}
	
	@Override
	public boolean isOcbaFallback () {
		return ocbaFallback;
	}

	public double kalculateKgEv(final double[] sigmas2ki_si_p1, final double[] muk, final double[] muk_maxOfOthers, final int i) {
		double v = sqrt(sigmas2ki_si_p1[i])*f.value(-abs( (muk[i]-muk_maxOfOthers[i])/sqrt(sigmas2ki_si_p1[i]) ));
		return v;
	}

	public double calculateSkgEv(final double[] one_over_aqrt_sigma_diff, final double[] mu_m, final double mum_maxOfOthers, final int[] d) {
		UnivariateFunction F = new UnivariateFunction() {				
			@Override
			public double value(double x) {		
				if (x < mum_maxOfOthers) return 0.0;
				double res = 1;				
				for (int j=0;j<mu_m.length;j++) {						
					if (d[j] > 0) { 
						res *= normal.cumulativeProbability((x-mu_m[j])*one_over_aqrt_sigma_diff[j]);
						
						
					}
				}
				return res;
			}
		};
		//double res1= CalculateEv.calculateEv_old_stepH(F,-15,15,1.0/65536);
		double res1 = CalculateEv.calculateEV_KG(F);
		return res1;
	}
	

	public static void main (String args[]) throws Exception {
		//AKG_RSalgorithm akg = new AKG_RSalgorithm();
		//KG_RSalgorithm kg = new KG_RSalgorithm();
		SKG_RSalgorithm skg0 = new SKG_RSalgorithm();
		SKG_RSalgorithm skg2 = new SKG_RSalgorithm(2);
		
		skg0.log = new BufferedWriter(new FileWriter("/temp/skg0.log"));
		skg2.log = new BufferedWriter(new FileWriter("/temp/skg2.log"));
		int N = 10;
		RandomStreamFactory rfSkg0 = new RandomStreamFactory(N, 0);
		RandomStreamFactory rfSkg2 = new RandomStreamFactory(N, 0);
		//RandomStreamFactory rfAkg = new RandomStreamFactory(N, 0);
		//RandomStreamFactory rfKg = new RandomStreamFactory(N, 0);
		RSpoint[] pointsSkg0 = new RSpoint[N];
		RSpoint[] pointsSkg2 = new RSpoint[N];
		//RSpoint[] pointsAkg = new RSpoint[N];
		//RSpoint[] pointsKg = new RSpoint[N];
		
		
		for (int i=0;i<N;i++) {
			pointsSkg0[i] = new RSpoint(rfSkg0.getStream(i), i);
			pointsSkg2[i] = new RSpoint(rfSkg2.getStream(i), i);
			//pointsAkg[i] = new RSpoint(rfAkg.getStream(i), i);
			//pointsKg[i] = new RSpoint(rfKg.getStream(i), i);
		}
		
		int a = 5;
		RSpoint[] selectedSkg0 = skg0.getPointsInit_k_0(pointsSkg0, 0,1,a);
		RSpoint[] selectedSkg2 = skg2.getPointsInit_k_0(pointsSkg2, 0,1,a);
		//RSpoint selectedAkg = akg.getPointInit_k_0(pointsAkg, 0);
		//RSpoint seleuctedKg = kg.getPointInit_k_0(pointsKg, 0);
		for (int step=1;step<=100;step++) {
			
			if (step==1) {
				System.out.print(" realval0 = {");
				for (int i=0;i<N;i++) {
					System.out.printf("%.5f", pointsSkg0[i].u);
					if (i<N-1) System.out.print(",");					
				}
				System.out.println("}");
				System.out.print(" realval2 = {");
				for (int i=0;i<N;i++) {
					System.out.printf("%.5f", pointsSkg2[i].u);
					if (i<N-1) System.out.print(",");					
				}
				System.out.println("}");
				
				System.out.print("AT STEP #0# skg=");
				for (RSpoint s : selectedSkg0) {
					System.out.print(s.i+",");
				}
				System.out.println();
				//+" akg="+selectedAkg.i+" kg="+selectedKg.i
			}				
			
			
			//System.out.print(" avg_i = {");
			//for (int i=0;i<N;i++) {
			//	System.out.printf("%.5f", points[i].getMeanY());
			//	if (i<N-1) System.out.print(",");
			//}
			//System.out.println("}");
			//System.out.println(" mu = {"+skg.toStr(skg.mu,false)+"}");
			//System.out.println(" sigma = {"+skg.toStr(skg.sigmas2,true)+"}");
				
			//System.out.print("AT STEP "+step + " selection vkg="+skg.best_vkg+" ");
			//for (int i=0;i<selectedSkg.length;i++) {
			//	System.out.print(" "+selectedSkg[i].i);
			//}
			//System.out.println();
			
			
			
			double x0[] = new double[selectedSkg0.length];
			for (int i=0;i<a;i++) {
				x0[i] = selectedSkg0[i].get_y();
			}
			
			double x2[] = new double[selectedSkg2.length];
			for (int i=0;i<a;i++) {
				x2[i] = selectedSkg2[i].get_y();
			}

			
			long time = System.currentTimeMillis();
			//selectedSkg0 = skg0.getNextPoints(x0, selectedSkg0, pointsSkg0, a, false);
			selectedSkg2 = skg2.getNextPoints(x2, selectedSkg2, pointsSkg2, a, false);
			System.out.println("TIME "+(System.currentTimeMillis()-time));
			//selectedAkg = akg.getNextPoint(selectedAkg.get_y(), selectedAkg, pointsAkg,0, false);
			//selectedKg = kg.getNextPoint(selectedKg.get_y(), selectedKg, pointsKg,0, false);
			
			
			//System.out.println("new  kg="+ skg.toStr(kg.getVkg(), false,15));
			//System.out.println("new skg0="+ skg0.toStr(skg0.getVkg(), false,15));
			System.out.println("new skg2="+ skg2.toStr(skg2.getVkg(), false,15));
			//System.out.println("new akg="+ skg.toStr(akg.getVkg(), false,15));
			
			/**
			System.out.print("AT STEP "+step + " skg0=");
			
			for (RSpoint s : selectedSkg0) {
				System.out.print(s.i+",");
			}
			System.out.println();**/
			System.out.print("AT STEP "+step + " skg2=");
			
			for (RSpoint s : selectedSkg2) {
				System.out.print(s.i+",");
			}
			System.out.println();

			//+" akg="+selectedAkg.i+" kg="+selectedKg.i
			
			
		}
		skg0.log.close();
		skg2.log.close();
	}
	
	
	protected String toStr (double[] arr, boolean sqrt) {		
		return toStr(arr, sqrt,5);
	}
	private String toStr (double[] arr, boolean sqrt, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<arr.length;i++) {
			sb.append(String.format(Locale.US,"%."+len+"f",sqrt?Math.sqrt(arr[i]):arr[i]));
			if (i<arr.length-1) sb.append(",");
		}		
		return sb.toString();
	}
	private double max(double[] arr) {
		double max = arr[0];
		for (int i=1;i<arr.length;i++) if (arr[i]>max) max = arr[i];
		return max;
	}

	
	@Override
	public double[] getVkg() {
		return this.vkg;
	}

	@Override
	public double getBestVkg() {
		return this.best_vkg;
	}
	@Override
	public int getPreselectionDistance() {
		return this.preselectionDistance;
	}
	

}
