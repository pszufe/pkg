package logging;
/**
 * An abstract representation of log storage mechanism. 
 * @author Bogumil Kaminski & Przemyslaw Szufel
 */
public interface StoreLog {
	public void store(String fileName, byte[] log);

	public void close();
}
