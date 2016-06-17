package threadpool;

public interface ThreadPool<E extends Runnable> {
	void execute(E e);
	
	void shutdown();
	
	void addWorkers(int workerNum);
	
	void removeWorker(int workerNum);
	
	int getJobSize();
}
