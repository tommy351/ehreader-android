package tw.skyarrow.ehreader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.concurrent.PriorityBlockingQueue;

import tw.skyarrow.ehreader.util.L;

/**
 * Created by SkyArrow on 2015/9/28.
 */
public abstract class PriorityQueueService extends Service {
    public static final String EXTRA_PRIORITY = "PRIORITY";

    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_HIGH = 10;

    private PriorityBlockingQueue<Task> priorityQueue;

    private final String name;
    private volatile ServiceHandler serviceHandler;
    private volatile Looper looper;

    public PriorityQueueService(String name) {
        super();
        this.name = name;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread(name);
        thread.start();
        looper = thread.getLooper();
        serviceHandler = new ServiceHandler(looper);
        priorityQueue = new PriorityBlockingQueue<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int priority = intent.getIntExtra(EXTRA_PRIORITY, PRIORITY_NORMAL);
        Task task = new Task(priority, startId, intent);
        priorityQueue.add(task);
        serviceHandler.sendEmptyMessage(0);
        return START_NOT_STICKY;
    }

    public abstract void onHandleIntent(Intent intent);

    @Override
    public void onDestroy() {
        looper.quit();
        super.onDestroy();
    }

    public static class Task implements Comparable<Task> {
        private final int priority;
        private final int startId;
        private final Intent intent;

        public Task(int priority, int startId, Intent intent) {
            this.priority = priority;
            this.startId = startId;
            this.intent = intent;
        }

        public Intent getIntent() {
            return intent;
        }

        @Override
        public int compareTo(Task another) {
            if (this.priority < another.priority){
                return -1;
            } else if (this.priority > another.priority){
                return 1;
            }

            return this.startId < another.startId ? -1 : 1;
        }
    }

    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                final Task task = priorityQueue.take();
                onHandleIntent(task.getIntent());

                if (priorityQueue.isEmpty()){
                    stopSelf();
                }
            } catch (InterruptedException e){
                L.e(e);
            }
        }
    }
}
