package org.kostiskag.unitynetwork.tracker.service.track;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
public class TrackServer extends Thread{

    private static final String pre = "^AUTH SERVER ";
    private static final int TIMEOUT = 3000;
    private static TrackServer TRACK_SERVER;
    private final int authPort;
    
    private TrackServer(int authport) throws IllegalAccessException {
        if(authport <= 0 || authport > App.MAX_ALLOWED_PORT_NUM) {
            throw new IllegalAccessException("Port range out of range.");
        }
        this.authPort = authport;
    }

    public static TrackServer newInstance(int port) throws IllegalAccessException {
        if (TRACK_SERVER == null) {
          TRACK_SERVER = new TrackServer(port);
        }
        return TRACK_SERVER;
    }

    public static TrackServer getInstance() {
        return TRACK_SERVER;
    }

    @Override
    public void run() {  
        AppLogger.getLogger().consolePrint("@Started blue auth server at " + Thread.currentThread().getName());
        try {
            ServerSocket welcomeSocket = new ServerSocket(authPort);  
            while (true) {    
                Socket connectionSocket = welcomeSocket.accept();    
                //connectionSocket.setSoTimeout(TIMEOUT);
                TrackService service = new TrackService(connectionSocket);
                service.start();
            }        
        } catch (java.net.BindException e){
            AppLogger.getLogger().consolePrint(pre +"PORT ALREADY IN USE");
            App.die();
        } catch (IOException e) {
            AppLogger.getLogger().consolePrint(e.getMessage());
            App.die();
        }        
    }
}
