package rsalgos;

import simulator.RSpoint;
/**
 * Represents an abstract R&S algorithm.
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */

public interface RSalgorithm {
	
	/**
	 * @param points - R&S problem points space
	 * @param w - worker number, for synchronous algorithms it is always 0
	 * @return the next design point that should be evaluated by the R&S algorithm
	 */
	public RSpoint getPointInit_k_0 (RSpoint[] points, int w); 
	/**
	 * 
	 * @param y - newly evaluated point value
	 * @param pointEval - point that has just been evaluated
	 * @param points - R&S problem points space
	 * @param w - worker number, for synchronous algorithms it is always 0 
	 * @param debug
	 * @return the next design point that should be evaluated by the R&S algorithm
	 */
	public RSpoint getNextPoint (double y, RSpoint pointEval, RSpoint[] points, int w, boolean debug); 
}
