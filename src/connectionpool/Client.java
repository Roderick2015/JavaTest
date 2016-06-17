package connectionpool;

import java.util.concurrent.CountDownLatch;

public class Client {
	static ConnectionPool pool = new ConnectionPool(10);
	static CountDownLatch latch = new CountDownLatch(1);
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		int a = 9%10;
		
		for (int i = 0; i < 2; i++) {
			Producer producer = new Producer(latch);
			Thread t = new Thread(producer, "Producer" + i);
			t.start();
		}
		
		for (int i = 0; i < 5; i++) {
			Consumer consumer = new Consumer(latch);
			Thread t = new Thread(consumer, "Consumer" + i);
			t.start();
		}
		
		latch.countDown(); //解锁线程等待
	}

    static class Producer implements Runnable {
    	private CountDownLatch latch;
    	
    	public Producer(CountDownLatch latch) {
    		this.latch = latch;
    	}

		@Override
		public void run() {
			try {
				latch.await(); //全部在此等待，
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
			while (true) {
				try {
					Thread.sleep(1000);
					pool.releaseConnection(); //每个1秒释放(生成)一个链接
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				System.out.println(Thread.currentThread() + " put a connection.");
			}
		}
    }
    
    static class Consumer implements Runnable {
    	private CountDownLatch latch;
    	
    	public Consumer(CountDownLatch latch) {
    		this.latch = latch;
    	}
    	
		@Override
		public void run() {
			try {
				latch.await();
				
				while (true) {
					Thread.sleep(1000);
					Connection connection = pool.fetchConnection(1000);
					if (connection == null) {
						System.out.println(Thread.currentThread() + " can not got a connection");
					} else {
						System.out.println(Thread.currentThread() + " got a connection");
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    	
    }

}
