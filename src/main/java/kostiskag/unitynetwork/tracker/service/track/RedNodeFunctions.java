package kostiskag.unitynetwork.tracker.service.track;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;

/**
* (unregistered) Rednode queries:
*
* GETBNS
* GETRBN
* 
* @author Konstantinos Kagiampakis
*/
public class RedNodeFunctions {

	public static void getRecomendedBlueNode(BufferedReader reader, PrintWriter writer, Socket socket) {
		String data;
		if (App.BNtable.getSize() > 0) {
			BlueNodeEntry recomended = App.BNtable.getBlueNodeEntryByLowestLoad();
			String hostname = recomended.getName();
			String phaddress = recomended.getPhaddress();
			int port = recomended.getPort();
			int load = recomended.getLoad();
			data = hostname + " " + phaddress + " " + port + " " + load;
		} else {
			data = "NONE";
		}
		SocketFunctions.sendFinalData(data, writer);
	}

	static void getAllConnectedBlueNodes(BufferedReader reader, PrintWriter writer, Socket socket) {
		int size = App.BNtable.getSize();
		if (App.BNtable.getSize() > 0) {
			SocketFunctions.sendFinalData("SENDING_BLUENODES " + size, writer);
			String fetched[][] = App.BNtable.buildStringInstanceObject();
			int i = 0;
			while(fetched[i] != null) {			
				SocketFunctions.sendFinalData(fetched[i][0] + " " + fetched[i][1] + " " + fetched[i][2] + " " + fetched[i][3], writer);
				i++;
			}			
			SocketFunctions.sendFinalData("", writer);
		} else {
			String data = "NONE";
			SocketFunctions.sendFinalData(data, writer);
		}
	}
}
