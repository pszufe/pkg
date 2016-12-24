package rsalgos;
/**
 * Implementation of the AKG algorithm
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;

import simulator.RSpoint;
import simulator.tools.CalculateEv;
import simulator.tools.RandomStreamFactory;

public class AKG_RSalgorithm implements AsynchcronousRSalgorithm, PRSalgorithm {
	private boolean outOfNumericalAccuracy;
	private final double numericalAccuracyDistTreshold = 1E-12;
	private boolean ocbaFallback;
	private double sigmas2[] = null; //priors  
	private double mu[] = null; //priors
	private int N;
	private double vkg[];
	private double best_vkg;
	private UnivariateFunction f = new UnivariateFunction() {
		private NormalDistribution normal = new NormalDistribution();
		@Override
		public double value(double x) {			
			return x*normal.cumulativeProbability(x)+normal.density(x);
		}
	};
	private final NormalDistribution normal = new NormalDistribution();
	@Override
	public RSpoint getPointInit_k_0(RSpoint[] points, int wNo) {
		outOfNumericalAccuracy = false;
		ocbaFallback = false;
		if (sigmas2==null) {
			N = points.length;
			sigmas2 = new double[N];
			Arrays.fill(sigmas2, 1); //initial beliefs equal to real values
			mu =  new double[N];
			Arrays.fill(mu, 0); //initial beliefs equal to 0
		}
		//in initialization just evenly split points across available workers, since all priors are equal and no other info is available
		return points[wNo % points.length];
		//return points[0];
	}
	
	@Override
	public RSpoint getNextPoint(double y,RSpoint point,final RSpoint[] points, int w, boolean debug) {
		final int N = points.length;
		
		if (!outOfNumericalAccuracy) {		
			final double sigmas2ki[] = new double[N]; //sigma^2 w at time k
			final double sigmas2ki_si[] = new double[N]; //sigma^2 with s
			final double sigmas2ki_si_p1[] = new double[N]; //sigma^2 with s+1
			final double muk[] = new double[N]; //mi at time k
			double sigma2e = 1;
			for (int i=0;i<N;i++) {
				if (point.i == i) {		
					sigmas2ki[i] = 1/(1/sigmas2[i] + 1/sigma2e);
					muk[i] = (mu[i]/sigmas2[i]+y/sigma2e)*sigmas2ki[i];
				} else {
					sigmas2ki[i] = sigmas2[i];
					muk[i] = mu[i];
				}
				sigmas2ki_si[i] =   sigmas2ki[i]-1/(1/sigmas2ki[i]+points[i].getS()/sigma2e);
				sigmas2ki_si_p1[i] = sigmas2ki[i]-1/(1/sigmas2ki[i]+(points[i].getS()+1)/sigma2e);
			}
			
			final double[] muk_maxOfOthers = Tools.maxOfOtherElems(muk);
			int s[] = new int[N];
			for (int i=0;i<N;i++) s[i] = points[i].getS();
			final double[] muk_maxOfOthers_s = Tools.maxOfOtherElems(muk,s);
			
			
			int xks = -1;
			double bestArg = -1e99;
			double worstArg = 1e99;
			double arg;
			vkg = new double[N];
			double max_muk = max(muk);
			
			long start = System.currentTimeMillis();
			for (int i=0;i<N;i++) {
				arg = calculateAkgEv(sigmas2ki_si_p1, muk, muk_maxOfOthers_s,s,sigmas2ki_si, i)-max_muk;		
				vkg[i] = arg;
				if (arg > bestArg) {
					bestArg = arg;
					xks = i;			
				}
				if (arg < worstArg) worstArg=arg;
			}
			//System.out.println("DUPA AKG "+N+"\t"+(System.currentTimeMillis()-start)+"\tna szt:"+((System.currentTimeMillis()-start)*1.0/N) );
			best_vkg=bestArg;
			if (debug) {
				System.out.println("muk=c("+Tools.str(muk)+")");
				System.out.println("sigmas2ki_si_p1= c("+Tools.str(sigmas2ki_si_p1)+");wavesigma <- function(i) {sqrt(sigmas2ki_si_p1[i])} ");
				System.out.println("usk_maxOfOthers=c("+Tools.str(muk_maxOfOthers)+")");
				System.out.println("usk_maxOfOthers_s=c("+Tools.str(muk_maxOfOthers_s)+")");			
			}
			sigmas2=sigmas2ki; 
			mu = muk;
			if (bestArg-worstArg < numericalAccuracyDistTreshold) outOfNumericalAccuracy = true;
			return points[xks];
		} else {
			ocbaFallback = true;
			return OCBAImplementation.getAOcba(points, 1)[0];			
		}
	}
	@Override
	public boolean isOcbaFallback () {
		return ocbaFallback;
	}

	public double kalculateKgEv(final double[] sigmas2ki_si_p1, final double[] muk, final double[] muk_maxOfOthers, final int i) {
		double v = sqrt(sigmas2ki_si_p1[i])*f.value(-abs( (muk[i]-muk_maxOfOthers[i])/sqrt(sigmas2ki_si_p1[i]) ));
		return v;
	}

	public double calculateAkgEv(final double[] sigmas2ki_si_p1, final double[] muk, final double[] muk_maxOfOthers_s, final int s[], final double[] sigmas2ki_si,  final int i) {
		UnivariateFunction F = new UnivariateFunction() {				
			@Override
			public double value(double x) {		
				if (x < muk_maxOfOthers_s[i]) return 0.0;
				double res = 1;				
				for (int j=0;j<muk.length;j++) {						
					if (j==i ) { //considering si+1							
						res *= normal.cumulativeProbability((x-muk[i])/sqrt(sigmas2ki_si_p1[i]));							
					} else if (s[j] > 0) { //only in this case sigmas2ki_si exists
						res *= normal.cumulativeProbability((x-muk[j])/sqrt(sigmas2ki_si[j]));							
					}
				}
				return res;
			}
		};
		//double res1= CalculateEv.calculateEv_old_stepH(F,-15,15,1.0/65536);
		double res1 = CalculateEv.calculateEV_KG(F);
		return res1;
	}
	
	private double max(double[] arr) {
		double max = arr[0];
		for (int i=1;i<arr.length;i++) if (arr[i]>max) max = arr[i];
		return max;
	}	
	
	
	public static void main (String args[]) throws Exception {
		int N =5;
		AKG_RSalgorithm akg = new AKG_RSalgorithm();
		RandomStreamFactory rfAkg = new RandomStreamFactory(N, 0);
		RSpoint[] pointsAkg = new RSpoint[N];
		for (int i=0;i<N;i++) {
			pointsAkg[i] = new RSpoint(rfAkg.getStream(i), i);
		}
		
		int a = 1;
		RSpoint selectedAkg = akg.getPointInit_k_0(pointsAkg, 0);
		for (int step=1;step<=10;step++) {

			
			if (step==1) {
				System.out.print(" realval = {");
				for (int i=0;i<N;i++) {
					System.out.printf("%.5f", pointsAkg[i].u);
					if (i<N-1) System.out.print(",");					
				}
				System.out.println("}");
				System.out.println("AT STEP #0# akg="+selectedAkg.i);
			}				
			
			
			
			selectedAkg = akg.getNextPoint(selectedAkg.get_y(), selectedAkg, pointsAkg,0,false);
			System.out.println("new akg="+ akg.toStr(akg.vkg, false,15));		
			
			
			System.out.println("AT STEP "+step + " akg="+selectedAkg.i);
		}
	}

	private String toStr (double[] arr, boolean sqrt, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<arr.length;i++) {
			sb.append(String.format(Locale.US,"%."+len+"f",sqrt?Math.sqrt(arr[i]):arr[i]));
			if (i<arr.length-1) sb.append(",");
		}		
		return sb.toString();
	}

	@Override
	public RSpoint[] getPointsInit_k_0(RSpoint[] points, int wNo, int a, int m) {
		if (m!=1) throw new IllegalArgumentException("Wrong m="+m+" AKG must have exactly one available slot in the worker");
		return new RSpoint[] { getPointInit_k_0(points, wNo) };
	}

	@Override
	public RSpoint[] getNextPoints(double[] y, RSpoint[] pointsEval, RSpoint[] points, int wNo, boolean debug) {
		return new RSpoint[] { getNextPoint(y[0], pointsEval[0], points, wNo,debug) };		
	}

	@Override
	public double[] getVkg() {
		return this.vkg;
	}

	@Override
	public double getBestVkg() {
		return this.best_vkg;
	}
}
