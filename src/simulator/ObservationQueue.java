package simulator;

import sim.engine.SimState;
import sim.engine.Steppable;
import simulator.tools.RandomStream;

public class ObservationQueue implements Steppable {

	private static final long serialVersionUID = 3544996195704547683L;
	private Distribution dist;
	private RandomStream rs;
	private int queue = 0;
	private int processed = 0;
	public ObservationQueue(Distribution dist, RandomStream rs, int initQueueSize) {
		this.dist=dist;
		this.queue = initQueueSize;
		this.rs = rs;
	}
	@Override
	public void step(SimState state) {
		//System.out.println(this.getClass().getName()+" "+state.schedule.getTime());
		if (dist != null) {
			queue++;
			state.schedule.scheduleOnceIn(dist.next(rs), this,0);			
		}
		state.schedule.scheduleOnceIn(0, ((ExperimentSimulator)state).stepAllocator,100);
	}
	public void popFromQueue(int count) {
		if (count > queue) throw new IllegalArgumentException("count > queue");
		queue -= count;
		processed += count;
	}
	public int getProcessed() {
		return processed;
	}
	
	public int getQueueSize() {
		return queue;
	}

}
