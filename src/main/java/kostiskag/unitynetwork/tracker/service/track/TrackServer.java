package kostiskag.unitynetwork.tracker.service.track;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import kostiskag.unitynetwork.tracker.App;

/**
 * The auth server listens for bluenode and rednode clients
 * When a client is connected the connected socket is moved in a thread
 * in order to be able to accept other clients as well.
 * 
 * @author Konstantinos Kagiampakis
 */
public class TrackServer extends Thread{

    private final String pre = "^AUTH SERVER ";
    private final int authPort;
    
    public TrackServer(int authport) {
        this.authPort = authport;
    }

    @Override
    public void run() {  
        App.ConsolePrint("@Started blue auth server at " + Thread.currentThread().getName());
        try {
            ServerSocket welcomeSocket = new ServerSocket(authPort);                                    
            while (true) {    
                Socket connectionSocket = welcomeSocket.accept();                
                TrackService service = new TrackService(connectionSocket);
                service.start();
            }        
        } catch (java.net.BindException e){
            App.ConsolePrint(pre +"PORT ALREADY IN USE");  
            App.die();
        } catch (IOException e) {
            App.ConsolePrint(e.getMessage());
            App.die();
        }        
    }
}
