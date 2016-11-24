package com.robot.et.slamtek.agent;

import java.util.ArrayList;

class Worker {
    private final static String TAG = "worker";

    private final ArrayList<Runnable> jobQueue = new ArrayList<>();

    private final Object WorkerLock = new Object();
    private Thread worker;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ArrayList<Runnable> jobs = new ArrayList<>();

            while (true) {
                synchronized (jobQueue) {
                    jobs.addAll(jobQueue);
                    jobQueue.clear();
                }

                if (jobs.isEmpty()) {
                    break;
                }

                for (Runnable job: jobs) {
                    job.run();
                }
                jobs.clear();
            }

            synchronized (StateLock) {
                state = IDLE;
            }
        }
    };

    private final static int IDLE = 0;
    private final static int WORKING = 1;

    private int state;
    private final Object StateLock = new Object();

    Worker() {
        state = IDLE;
        worker = new Thread(runnable);
    }

    void push(Runnable job) {
        synchronized (jobQueue) {
            jobQueue.add(job);
        }

        execute();
    }

    void pushHead(Runnable job) {
        synchronized (jobQueue) {
            jobQueue.add(0, job);
        }
        execute();
    }

    void clear() {
        synchronized (jobQueue) {
            jobQueue.clear();
        }
    }

    private void execute() {
        synchronized (StateLock) {
            if (state == WORKING) {
                return;
            }
            state = WORKING;
        }

        if (worker.getState() == Thread.State.TERMINATED) {
            worker = new Thread(runnable);
        }

        if (worker.getState() == Thread.State.WAITING) {
            synchronized (WorkerLock) {
                worker.notify();
            }
        }

        if (worker.getState() == Thread.State.NEW) {
            worker.start();
        }
    }
}
