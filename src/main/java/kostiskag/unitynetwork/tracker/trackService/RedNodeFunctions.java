/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.tracker.trackService;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import kostiskag.unitynetwork.tracker.functions.SocketFunctions;

/**
 *
 * @author kostis
 */
public class RedNodeFunctions {

    //kanonika auth einai mia poluplokh sunarthsh opou upologizei kodinoterh apostash 
    //kai meiomeno forto ergasias alla edw apla petame ton prwto BN
    /*
     * 
     * 
     * TO DO STUFF
     * 
     * 
     * mexri twra upologizetai mono to load 
     * alla tha prepei na upologizetai kai to distance
     * 
     */
    
    public static void getRecomendedBlueNode(BufferedReader reader, PrintWriter writer, Socket socket) {                
        String data;
        int place = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeIdByLowestLoad();
        if (place>-1){
            String hostname = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeEntry(place).getHostname();                    
            String phaddress = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeEntry(place).getPhaddress();
            int port = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeEntry(place).getPort();
            int load = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeEntry(place).getLoad();
            data = hostname+" "+phaddress+" "+port+" "+load;
        } else {
            data = "NONE";
        }
        SocketFunctions.sendFinalData(data, writer);
    }

    static void getAllBlueNodes(BufferedReader reader, PrintWriter writer, Socket socket) {
        int size = kostiskag.unitynetwork.tracker.App.BNtable.getSize();
        SocketFunctions.sendFinalData("SENDING_BLUENODES " + size, writer);
        for (int i = 0; i < size; i++) {
            String hostname = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeEntry(i).getHostname();                    
            String phaddress = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeEntry(i).getPhaddress();
            int port = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeEntry(i).getPort();
            int load = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeEntry(i).getLoad();
            SocketFunctions.sendFinalData(hostname+" "+phaddress+" "+port+" "+load, writer);
        }
        SocketFunctions.sendFinalData("", writer);                                                
    }
}
