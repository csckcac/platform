package org.wso2.andes.server.cassandra;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class TestClass {

  public static void main(String[] args) {
    int nTasks = 15;
    long n = 40L;
//    long n = 1000L;
    int tpSize = 10;

    ThreadPoolExecutor tpe = new ThreadPoolExecutor(tpSize, tpSize, 50000L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>());

    Task[] tasks = new Task[nTasks];
    for (int i = 0; i < nTasks; i++) {
      tasks[i] = new Task(n, "Task " + i);
      tpe.execute(tasks[i]);
    }
    tpe.shutdown();
  }
}
/*
class SingleThreadAccess {

  private ThreadPoolExecutor tpe;

  public SingleThreadAccess() {
    tpe = new ThreadPoolExecutor(1, 1, 50000L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>());
  }

  public void invokeLater(Runnable r) {
    tpe.execute(r);
  }

  public void invokeAneWait(Runnable r) throws InterruptedException, ExecutionException {
    FutureTask task = new FutureTask(r, null);
    tpe.execute(task);
    task.get();
  }

  public void shutdown() {
    tpe.shutdown();
  }
}*/

class Task implements Runnable {
  long n;

  String id;

  private long fib(long n) {
    if (n == 0)
      return 0L;
    if (n == 1)
      return 1L;
    return fib(n - 1) + fib(n - 2);
  }

  public Task(long n, String id) {
    this.n = n;
    this.id = id;
  }

  public void run() {
      System.out.println("testing thress = " + n);
    Date d = new Date();
    DateFormat df = new SimpleDateFormat("HH:mm:ss:SSS");
    long startTime = System.currentTimeMillis();
    d.setTime(startTime);
    System.out.println("Starting task " + id + " at " + df.format(d));
    fib(n);
    long endTime = System.currentTimeMillis();
    d.setTime(endTime);
    System.out.println("Ending task " + id + " at " + df.format(d) + " after "
        + (endTime - startTime) + " milliseconds");
  }
}
