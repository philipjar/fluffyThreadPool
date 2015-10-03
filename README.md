# fluffyThreadPool
WARNING: NOT FINISHED. MAY CONTAIN (LOTS OF) BUGS.

Another Thread Pool implementation in Java.
Yes, there is a ThreadPool already in Java, but I was bored so I wrote one myself just for fun.

This Thread Pool will create a Number of worker Threads and Idle all of them. Tasks can be given to the pool
as Runnables. If there is an idling worker, it instantly executes the runnable. If not, the runnable is attached to a
queue and every time a Thread goes to Idle, the oldest element of the queue is processed by this Thread.
