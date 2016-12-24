package simulator;

import simulator.tools.RandomStream;

public interface Distribution {
	public double next(RandomStream random);
	public String getName();
	public String getParam();
}
