# fluffyThreadPool
WARNING: NOT FINISHED. MAY CONTAIN BUGS.

Another Thread Pool implementation in Java.

This Thread Pool will create a given Number of worker Threads and Idle all of them. Tasks can be given to the pool
as Runnables. If there is an idling worker, it instantly executes the runnable. If not, the runnable is added to a
queue and every time a Thread goes to Idle, the oldest element of the queue is processed by this Thread.

Example Code:

```java
/* Constructor needs max Number of Threads and PrintStream to print messages to (or null). */ <br />
Pool pool = new Pool(1, System.out); <br />
Runnable exampleRun = new Runnable() { <br />
		@Override <br />
		public void run() { <br />
			System.out.println("Hello ThreadPool World!"); <br />
		} <br />
	}; <br />
pool.execute(exampleRun); <br />
```