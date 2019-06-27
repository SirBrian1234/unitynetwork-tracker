package org.kostiskag.unitynetwork.tracker.service.sonar;

import org.kostiskag.unitynetwork.tracker.AppLogger;
import org.kostiskag.unitynetwork.tracker.rundata.service.SimpleCyclicService;
import org.kostiskag.unitynetwork.tracker.rundata.table.BlueNodeTable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/**
 * Works like the java garbage collector but for killed bluenodes and redonodes. The sonar
 * connects to the leased bluenodes and requests to get their respective rednodes back
 * When a dead bn is found it, and its rns are removed from the network
 * If a bn has died it has to wait for this duration in order to
 * be able to reconnect to the network.
 *
 * THIS IS A SINGLETON!!!
 * 
 * @author Konstantinos Kagiampakis
 */
public final class SonarService extends SimpleCyclicService {

    private final static String pre = "^SONAR ";
    private static SonarService SONAR_SERVICE;

    public static SonarService newInstance(int givenTime) throws IllegalAccessException {
        if (SONAR_SERVICE == null) {
            SONAR_SERVICE = new SonarService(givenTime);
        }
        return SONAR_SERVICE;
    }

    public static SonarService getInstance() {
        return SONAR_SERVICE;
    }

    private SonarService(int time) throws IllegalAccessException {
        super(time);
    }

    @Override
    protected void cyclicPayload() {
        try {
            Lock lock = BlueNodeTable.getInstance().aquireLock();
            AppLogger.getLogger().consolePrint(pre + "Updating BN Tables via ping");
            BlueNodeTable.getInstance().rebuildTableViaAuthClient(lock);
        } catch (InterruptedException e) {
            AppLogger.getLogger().consolePrint(e.getMessage());
        } finally {
            BlueNodeTable.getInstance().releaseLock();
        }
    }

    @Override
    protected void greetingMessage() {
        AppLogger.getLogger().consolePrint(pre+"Started at thread "+Thread.currentThread()+" with refresh time " + getTime() + " sec");
    }

    @Override
    protected void stopMessage() {
        AppLogger.getLogger().consolePrint(pre + "stopped");
    }

    @Override
    protected void interruptedMessage(InterruptedException ex) {
        AppLogger.getLogger().consolePrint(ex.getMessage());
    }

}
