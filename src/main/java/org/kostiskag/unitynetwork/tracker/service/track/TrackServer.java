package org.kostiskag.unitynetwork.tracker.service.track;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.BindException;
import java.io.IOException;
import java.security.KeyPair;

import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;

import org.kostiskag.unitynetwork.tracker.App;
import org.kostiskag.unitynetwork.tracker.AppLogger;


/**
 * The auth server listens for bluenode and rednode clients
 * When a client is connected the connected socket is moved in a thread
 * in order to be able to accept other clients as well.
 *
 * THIS IS A SINGLETON!
 *
 * @author Konstantinos Kagiampakis
 */
public final class TrackServer extends Thread {

    private static final String pre = "^TRACK ";
    private static final int TIMEOUT = 3000;
    private static TrackServer TRACK_SERVER;
    private final int authPort;
    private final KeyPair trackerKeyPair;
    
    private TrackServer(int authport, KeyPair keyPair) throws IllegalAccessException {
        if(authport <= 0 || authport > NumericConstraints.MAX_ALLOWED_PORT_NUM.size()) {
            throw new IllegalAccessException("Port number out of allowed range.");
        }
        if (keyPair == null || keyPair.getPublic() == null || keyPair.getPrivate() == null) {
            throw new IllegalAccessException("No keys were given");
        }
        this.authPort = authport;
        this.trackerKeyPair = keyPair;
    }

    public static TrackServer newInstance(int port, KeyPair keyPair) throws IllegalAccessException {
        if (TRACK_SERVER == null) {
          TRACK_SERVER = new TrackServer(port, keyPair);
        }
        return TRACK_SERVER;
    }

    public static TrackServer getInstance() {
        return TRACK_SERVER;
    }

    @Override
    public void run() {
        AppLogger.getLogger().consolePrint(pre + "Started at " + Thread.currentThread().getName()+ " on port: "+this.authPort);
        try {
            final ServerSocket server = new ServerSocket(authPort);
            while (true) {
                Socket session = server.accept();
                //server.setSoTimeout(TIMEOUT);
                TrackService service = new TrackService(session, trackerKeyPair);
                service.start();
            }
        } catch (BindException e){
            AppLogger.getLogger().consolePrint(pre + "PORT ALREADY IN USE");
            App.die();
        } catch (IOException e) {
            AppLogger.getLogger().consolePrint(pre + e.getMessage());
            App.die();
        }
    }
}
