package org.kostiskag.unitynetwork.tracker.rundata.service;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SimpleCyclicService extends Thread {

    private final int time;
    private final AtomicBoolean kill = new AtomicBoolean(false);

    protected SimpleCyclicService(int time) throws IllegalAccessException {
        if (time <= 0) {
            throw new IllegalAccessException("time was 0 or below!");
        }
        this.time = time;
    }

    @Override
    public final void run() {
        greetingMessage();
        while (!kill.get()) {
            try {
                sleep(time * 1000);
            } catch (InterruptedException ex) {
                interruptedMessage(ex);
            } finally {
                if (kill.get()) break;
            }
            cyclicPayload();
        }
        stopMessage();
    }

    public final void kill(){
        kill.set(true);
    }

    protected abstract void greetingMessage();

    protected abstract void stopMessage();

    protected abstract void interruptedMessage(InterruptedException ex);

    protected abstract void cyclicPayload();

    protected final int getTime() {
        return time;
    }
}
