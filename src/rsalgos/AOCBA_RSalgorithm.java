package rsalgos;

import java.io.BufferedWriter;
import java.util.Arrays;

import simulator.RSpoint;
import simulator.tools.RandomStreamFactory;

public class AOCBA_RSalgorithm implements PRSalgorithm,HasMinMeasuresPerPoint {

	private double sigmas2[] = null; //priors  
	private double mu[] = null; //priors
	private int N;
	private double[] vkg;
    private int allocatedMesurements;
    private final int minMesurementsPerPoint;
    public AOCBA_RSalgorithm (int minMesurementsPerPoint) {
    	this.minMesurementsPerPoint = minMesurementsPerPoint;
    }
	@Override
	public RSpoint[] getPointsInit_k_0(RSpoint[] points, int wNo, int a, int m) {
		//AOCBA can have many workers with many slots
		RSpoint[] res = new RSpoint[m];
		if (sigmas2==null) {
			N = points.length;
			sigmas2 = new double[N];
			Arrays.fill(sigmas2, 1); //initial beliefs equal to real values
			mu =  new double[N];
			Arrays.fill(mu, 0); //initial beliefs equal to 0
	    	this.allocatedMesurements = 0;
	    	vkg = new double[N];
		}
		//in initialization just evenly split points across available workers, since all priors are equal and no other info is available
		for (int w=0;w<m;w++) {
			res[w]=points[(w+wNo*m) % points.length];   
		}
		allocatedMesurements += res.length;
		
		return res;		
	}
	
	BufferedWriter log=null;

	
	@Override
	public RSpoint[] getNextPoints(double[] y, RSpoint[] pointsEval, RSpoint[] points, int wNo, boolean debug) {
		RSpoint[] res ;
		if (allocatedMesurements < Math.max(points.length*minMesurementsPerPoint,y.length)) {
			res = new RSpoint[y.length];
			for (int w=0;w<y.length;w++) {
				res[w]=points[ (w+allocatedMesurements) % points.length];   
			}
			allocatedMesurements += y.length;
		} else {			
			res = OCBAImplementation.getAOcba(points, y.length);			
		}
		return res;
	}	
	


	public static void main (String args[]) throws Exception {
		AOCBA_RSalgorithm ocba  = new AOCBA_RSalgorithm(5);
		int N = 5;
		RandomStreamFactory rf = new RandomStreamFactory(N, 0);
		RSpoint[] points = new RSpoint[N];
		for (int i=0;i<N;i++) {
			points[i] = new RSpoint(rf.getStream(i), i);
		}
		int m = 5;
		RSpoint[] selected = ocba.getPointsInit_k_0(points, 0,1,m);
		for (int step=1;step<=100;step++) {
			
			for (int i=0;i<N;i++) {
				System.out.print(" m("+i+")=");
				System.out.printf("%.3f", points[i].getMeanY());
				if (step==0) {
					System.out.printf(" real val= %.3f", points[i].u);
				}			
				System.out.println();
			}
			System.out.print("AT STEP "+step + " selection ");
			for (int i=0;i<selected.length;i++) {
				System.out.print(" "+selected[i].i);
			}
			System.out.println();
			

			
			double x[] = new double[selected.length];
			for (int i=0;i<N;i++) {
				x[i] = selected[i].get_y();
			}
			selected = ocba.getNextPoints(x, selected, points, 0, false);
		}
		
	}

	@Override
	public boolean isOcbaFallback () {
		return true;
	}

	@Override
	public double[] getVkg() {
		return this.vkg;
	}

	@Override
	public double getBestVkg() {
		return 0;
	}
	@Override
	public int getMinMeasuresPerPoint() {
		return minMesurementsPerPoint;
	}
}