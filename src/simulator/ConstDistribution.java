package simulator;

import simulator.tools.RandomStream;

public class ConstDistribution implements Distribution {

	final double value;
	final String param;
	public ConstDistribution (double value) {
		this.value = value;
		this.param = new Double(value).toString();
	}
	@Override
	public double next(RandomStream random) {		
		return  value+0.0*random.random();
	}
	@Override
	public String getName() {
		return "const";
	}
	@Override
	public String getParam() {
		return param;
	}
	

}
