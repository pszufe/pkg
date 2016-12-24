package rsalgos;
/**
 * Implementation of the KG algorithm
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.util.Arrays;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.distribution.NormalDistribution;

import simulator.RSpoint;

public class KG_RSalgorithm implements RSalgorithm,PRSalgorithm {

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
	
	@Override
	public RSpoint getPointInit_k_0(RSpoint[] points, int w) {
		if (sigmas2==null) {
			N = points.length;
			sigmas2 = new double[N];
			Arrays.fill(sigmas2, 1); //initial beliefs equal to real values
			mu =  new double[N];
			Arrays.fill(mu, 0); //initial beliefs equal to 0
		}
		//no calculations have been made - KG will be equal for all points - we pass the first point from the list  
		return points[0];
	}
	
	@Override
	public RSpoint getNextPoint(double y,final RSpoint point,RSpoint[] points, int w, boolean debug) {
		final int N = points.length;
		final double sigmas2ki[] = new double[N]; //sigma^2 w at time k
		final double sigmas2ki_si_p1[] = new double[N]; //sigma^2 w at time k
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
			sigmas2ki_si_p1[i] = sigmas2ki[i]-1/(1/sigmas2ki[i]+1/sigma2e);
		}
		
		final double[] usk_maxOfOthers = Tools.maxOfOtherElems(muk);
		int xks = -1;
		double bestArg = -1e99;
		double arg;
		vkg = new double[N];
		for (int i=0;i<N;i++) {
			arg = sqrt(sigmas2ki_si_p1[i])*f.value(-abs( (muk[i]-usk_maxOfOthers[i])/sqrt(sigmas2ki_si_p1[i]) ));
			vkg[i] = arg;
			if (arg > bestArg) {
				bestArg = arg;
				xks = i;
			}
		}	
		best_vkg = bestArg;
		sigmas2=sigmas2ki; 
		mu = muk;
		return points[xks];
	}

	@Override
	public boolean isOcbaFallback () {
		return false;
	}

	@Override
	public RSpoint[] getPointsInit_k_0(RSpoint[] points, int wNo, int a, int m) {
		if (wNo!=0 || a!=1 || m !=1) 
			throw new IllegalArgumentException("KG has exactly one worker with one slot");
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
