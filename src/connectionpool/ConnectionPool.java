package connectionpool;

import java.util.LinkedList;

public class ConnectionPool {
	private LinkedList<Connection> pool = new LinkedList<Connection>();
	private static final int MAX_SIZE = 20;
	
	public ConnectionPool(int initialSize) {
		if (initialSize < 0)
			throw new IllegalArgumentException("initialSize: " + initialSize);
		
		for (int i = 0; i < initialSize; i++) {
			pool.addLast(new Connection());
		}
	}
	
	public void releaseConnection() throws InterruptedException {
		synchronized (pool) {
			while (pool.size() > MAX_SIZE) {
				pool.wait();
			}
			
			pool.addLast(new Connection());
			pool.notifyAll();
		}
	}
	
	public Connection fetchConnection(long mills) throws InterruptedException {
		synchronized (pool) {
			if (mills <= 0) {
				while (pool.isEmpty())
					pool.wait(); //连接池为空，则一直等待
				
				return pool.removeFirst();
			} else {
				long futureTime = System.currentTimeMillis() + mills;
				long deltaTime = mills;
				while (pool.isEmpty() && deltaTime > 0) {
					pool.wait();
					deltaTime = futureTime - System.currentTimeMillis();
				}
				
				Connection result = null;
				if (!pool.isEmpty())
					result = pool.removeFirst();
				
				return result;
			}
		}
	}
	
}
