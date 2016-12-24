package simulator.tools;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;


/**
 * Allows to create and combine random streams 
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */
public class RandomStreamFactory {
	
	private ArrayList<Double> numbers[];
	private int currentIndexes[];
	private MersenneTwisterFast random;
	
	/**
	 * @param ids sub-stream ids
	 * @return creates a random stream on the base of one or many sub-streams
	 */
	public RandomStream getStream(final int... ids) {
		return new RandomStream() {
			int ix = -1;
			@Override
			public double random() {
				ix++;
				return rand(ids[ix % ids.length]);
			}
			@Override
			public String id() {
				StringBuilder res = new StringBuilder("[");
				for (int i=0;i<ids.length;i++) res.append(ids[i]+((i<ids.length-1)?",":""));
				res.append("]");
				return res.toString();
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public RandomStreamFactory (int k, long seed) {		
		numbers = new ArrayList[k];
		for (int i=0;i<k;i++) {
			numbers[i] = new ArrayList<Double>();
		}	
		currentIndexes = new int[k];
		random = new MersenneTwisterFast(seed);
	}
	
	private double rand(int stream) {
		if (currentIndexes[stream] <= numbers[stream].size()) {
			for (int i=0;i<numbers.length;i++) {
				 numbers[i].add(random.nextDouble());
			}
		}
		currentIndexes[stream]++;
		return numbers[stream].get(currentIndexes[stream]-1);
	}
}
