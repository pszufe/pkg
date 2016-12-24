package simulator;

import sim.engine.SimState;
import sim.engine.Steppable;
import simulator.tools.RandomStream;

public class ObservationWorker implements Steppable {

	private static final long serialVersionUID = 3544996195704547683L;
	public final int parallelCapacitySlots;
	private boolean available;
	public ObservationWorker(int parallelCapacitySlots) {
		this.parallelCapacitySlots = parallelCapacitySlots;
		this.available = true;
	}	
		
	
	@Override
	public void step(SimState state) {
		//System.out.println(this.getClass().getName()+" "+state.schedule.getTime());
		available = true;
		state.schedule.scheduleOnceIn(0, ((ExperimentSimulator)state).stepAllocator,100);
	}
	public boolean isAvailable() {
		return available;
	}
	public void schedule(double delta, SimState state) {
		this.available = false;
		state.schedule.scheduleOnceIn(delta, this, 10);
		
	}
	
	RSpoint[] selected = null;

}
