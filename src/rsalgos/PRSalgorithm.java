package rsalgos;

import simulator.RSpoint;
/**
 * Represents an abstract parallel R&S algorithm.
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */

public interface PRSalgorithm {
	
	/**
	 * @param points R&S problem points space
	 * @param wNo number of experimenter, for synchronous algorithms it is always 0 
	 * @param a total number of available experimenters
     * @param m number of parallel slots per experimenter
	 * 
	 * @return the list of design points to be evaluated by the P-R&S algorithm
	 */
	public RSpoint[] getPointsInit_k_0 (RSpoint[] points, int wNo, int a, int m); 
	/**
	 * 
	 * @param y newly evaluated points value
	 * @param points points that have just been evaluated
	 * @param points P-R&S problem points space
	 * @param wNo worker number, for synchronous algorithms it is always 0 
	 * @param number of point requested so far by P-R&S algorithm
	 * @param debug
	 * @return the next design point that should be evaluated by the R&S algorithm
	 */
	public RSpoint[] getNextPoints (double y[], RSpoint[] pointsEval, RSpoint[] points, int wNo, boolean debug);
	
	public double[] getVkg();
	public double getBestVkg();
	public boolean isOcbaFallback();
}
