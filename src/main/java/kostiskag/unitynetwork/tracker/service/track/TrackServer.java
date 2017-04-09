package kostiskag.unitynetwork.tracker.service.track;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import kostiskag.unitynetwork.tracker.App;

/**
 *
 * @author kostis
 */
public class TrackServer extends Thread{

    public String pre = "^AUTH SERVER ";
    public static int authport;
    public static Boolean didTrigger = false;

    public TrackServer(int authport) {
        this.authport = authport;
    }

    @Override
    public void run() {  
        App.ConsolePrint("@Started blue auth server at " + Thread.currentThread().getName());
        try {
            ServerSocket welcomeSocket = new ServerSocket(authport);                                    
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
