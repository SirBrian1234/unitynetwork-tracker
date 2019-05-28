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
 * @author Konstantinos Kagiampakis
 */
public class TrackServer extends Thread{

    private final String pre = "^AUTH SERVER ";
    private final int timeout = 3000;
    private final int authPort;
    
    public TrackServer(int authport) {
        this.authPort = authport;
    }

    @Override
    public void run() {  
        AppLogger.getLogger().consolePrint("@Started blue auth server at " + Thread.currentThread().getName());
        try {
            ServerSocket welcomeSocket = new ServerSocket(authPort);  
            while (true) {    
                Socket connectionSocket = welcomeSocket.accept();    
                //connectionSocket.setSoTimeout(timeout);
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
