package logging;

/**
 * Allows to store logs in Amazon S3 bucket
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */

public class StoreLogIgnore implements StoreLog {

	@Override
	public void store(String fileName, byte[] log) {
		throw new UnsupportedOperationException("StoreLogIgnore - use instanceof insted");
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
	
}
