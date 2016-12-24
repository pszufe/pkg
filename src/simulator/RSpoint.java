package simulator;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;

import simulator.tools.RandomStream;


/**
 * Represents a R&S point Y.
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */

public class RSpoint {
    public double u; //mi uknown
    public final double sig;  //sigma uknown

	private int s = 0; //number of running calculations
	
	public final ArrayList<Double> ys;
	NormalDistribution normalD;
	private RandomStream rs;
	public final int i;
	
	public double ys_sum;
	/**
	 * @return the mean for values simulated so far or zero if nothing is known about that point
	 */
	public double getMeanY() {		
		if (ys.size()==0) return 0.0;
		return ys_sum/ys.size();
	}
	
	public double getVarianceY() {
		if (ys.size()<=1) return 0.0;
		double mean = getMeanY();
		double temp = 0;
        for(double y:ys)
            temp += (y-mean)*(y-mean);
        return temp/(ys.size()-1);
	}
	
	public RSpoint (RandomStream rs, int i) {		
		this.rs=rs;
		this.i=i;		
		//this.ys=new ArrayList<>();
		//parameterization following Frazier i Powell (2009) 
		// The knowledge gradient policy for offline learning with independent normal rewards 
		u = new NormalDistribution(0, 1).inverseCumulativeProbability(rs.random());
		sig = 1;
		normalD = new NormalDistribution(u, sig);
		ys = new ArrayList<Double>();
		ys_sum = 0.0;
		//System.out.println("R&S point "+i+" u="+u+" sig="+sig+" randomStream="+rs.id());
	}
	
	private double y;
	/**
	 * @return evaluates a new point - this method may be only evaluated by the main loop
	 */
	public double get_y() {
		y = normalD.inverseCumulativeProbability(rs.random());
		ys.add(y);
		ys_sum +=y;
		//System.out.println(this);
		return y;
	}
	
	public String toString() {
		return "R&S point "+i+"/c="+ys.size()+" real u="+u+" sig="+sig+" current u="+getMeanY();
	}	
	/**
	 * @return s - the number of currently running calculations for this point
	 */
	public int getS () {
		return s;
	}
	
	public void decS () {
		s--;
		if (s<0) throw new IllegalArgumentException("s<0  - to many point decrements.");
	}
	public void incS () {
		s++;
	}
	
	public int getNoMeasures () {
		return ys.size();
	}
}
