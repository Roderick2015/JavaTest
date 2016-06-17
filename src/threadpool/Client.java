package threadpool;

import java.util.concurrent.CountDownLatch;

public class Client {
	static ThreadPool<Job> pool = new DefaultThreadPool<Job>(10);
	
	public static void main(String[] args) throws InterruptedException {
		test1();
	}
	
	private static void test1() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(10);
		for (int i = 0; i < 10; i++) {
			Thread t = new Thread(new Task(latch), "Task " + i);
			t.start();
		}
		
		latch.await();
		
		pool.removeWorker(5);
		
		while (pool.getJobSize() > 0) {
			Thread.sleep(50);
			System.out.println(pool.getJobSize()); 
		}
		
		System.exit(0);
		/*
		Thread.sleep(20);
		System.out.println(pool.getJobSize()); 
		
		Thread.sleep(20);
		System.out.println(pool.getJobSize());

		Thread.sleep(20);
		System.out.println(pool.getJobSize());

		Thread.sleep(5000);
		System.out.println(pool.getJobSize());*/
	}
	
	static class Task implements Runnable {
		CountDownLatch latch = null;
		
		public Task(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void run() {
			for (int i = 0; i < 1000; i++) {
				pool.execute(new Job());
			}
			System.out.println(Thread.currentThread() + String.valueOf(pool.getJobSize())); 
			latch.countDown();
		}
		
	}

}
