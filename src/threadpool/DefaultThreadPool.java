package threadpool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DefaultThreadPool<E extends Runnable> implements ThreadPool<E>{
	private static final int MAX_WORKER_NUMBERS = 10;
	private static final int DEFAULT_WORKER_NUMBERS = 5;
	private static final int MIN_WORDER_NUMBERS = 1;
	private final LinkedList<E> jobs = new LinkedList<E>();
	private final List<Worker> workers = Collections
			.synchronizedList(new ArrayList<Worker>());
	private int workerNum = DEFAULT_WORKER_NUMBERS;
	
	public DefaultThreadPool() {
		initializeWorkers(DEFAULT_WORKER_NUMBERS);
	} 
	
	public DefaultThreadPool(int num) {
		workerNum = (num > MAX_WORKER_NUMBERS) ? MAX_WORKER_NUMBERS
				: (num < MIN_WORDER_NUMBERS) ? MIN_WORDER_NUMBERS : num;
		initializeWorkers(workerNum);
	} 
	
	@Override
	public void execute(E e) {
		if (e != null) {
			synchronized (jobs) {
				jobs.addLast(e);
				jobs.notify(); //只要有一个唤醒即可，notifyAll扩大唤醒个数，浪费开销
			}
		}
	}

	@Override
	public void shutdown() {
		for (Worker worker : workers)
			worker.shutdown();
	}

	@Override
	public void addWorkers(int workerNum) {
		int addedNum = workerNum;
		if (workerNum + this.workerNum > MAX_WORKER_NUMBERS)
			addedNum = MAX_WORKER_NUMBERS - this.workerNum;//预添加数大于最大数
		
		synchronized (jobs) {
			initializeWorkers(addedNum);
			this.workerNum = this.workerNum + addedNum; //workerNum随list中的数量增加
		}
	}

	@Override
	public void removeWorker(int workerNum) {
		if (workerNum >= this.workerNum)
			throw new IllegalArgumentException(
					"can't remove " + workerNum + ", now num is " + this.workerNum);
		
		synchronized (jobs) {
			int count = 0;
			while (count < workerNum) {
				workers.get(count).shutdown(); //从list中逐个取出，所以不可越界
				count++;
			}
			
			this.workerNum = this.workerNum - count;		
		}
	}

	@Override
	public int getJobSize() {
		return jobs.size();
	}
	
	private void initializeWorkers(int num) {
		for (int i = 0; i < num; i++) {
			Worker worker = new Worker();
			workers.add(worker);

			Thread thread = new Thread(worker, "Worker" + "i");
			thread.start();
		}
	}
	
	class Worker implements Runnable {
		private volatile boolean running = true;

		@Override
		public void run() {
			while (running) {
				E job = null;
				synchronized (jobs) {
					while (jobs.isEmpty()) {
						try {
							jobs.wait(); //没有工作可干，进入等待
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					job = jobs.removeFirst(); //从工作队列中取出一个
				}
				
				if (job != null) {
					job.run(); //执行
				}
			}
		}
		
		public void shutdown() {
			running = false;
		}

	}

}
