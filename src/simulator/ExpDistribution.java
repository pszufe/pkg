package simulator;

import simulator.tools.RandomStream;

public class ExpDistribution implements Distribution {

	private final double lambda;
	private final String param;
	public ExpDistribution (double lambda) {
		this.lambda=lambda;
		this.param = new Double(lambda).toString();
	}
	@Override
	public double next(RandomStream random) {		
		return  Math.log(1-random.random())/(-lambda);
	}
	
	@Override
	public String getName() {
		return "~exp";
	}
	@Override
	public String getParam() {
		return param;
	}

}
