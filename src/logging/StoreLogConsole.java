package logging;

import java.io.UnsupportedEncodingException;
/**
 * Outputs logs to the console
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */
public class StoreLogConsole implements StoreLog {


	public StoreLogConsole () {
	}

	@Override
	public void store(String fileName, byte[] log) {
		try {
			System.out.print(new String(log,"ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
