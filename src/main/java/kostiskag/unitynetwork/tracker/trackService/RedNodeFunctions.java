/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.tracker.trackService;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import kostiskag.unitynetwork.tracker.App;
import kostiskag.unitynetwork.tracker.functions.SocketFunctions;
import kostiskag.unitynetwork.tracker.runData.BlueNodeEntry;

/**
 *
 * @author kostis
 */
public class RedNodeFunctions {

	public static void getRecomendedBlueNode(BufferedReader reader, PrintWriter writer, Socket socket) {
		String data;
		if (kostiskag.unitynetwork.tracker.App.BNtable.getSize() > 0) {
			BlueNodeEntry recomended = kostiskag.unitynetwork.tracker.App.BNtable.getBlueNodeEntryByLowestLoad();
			String hostname = recomended.getHostname();
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
		SocketFunctions.sendFinalData("SENDING_BLUENODES " + size, writer);
		//size may change while in proccess
		//needs to be updated
		for (int i = 0; i < size; i++) {
			String hostname = App.BNtable.getBlueNodeEntryById(i).getHostname();
			String phaddress = App.BNtable.getBlueNodeEntryById(i).getPhaddress();
			int port = App.BNtable.getBlueNodeEntryById(i).getPort();
			int load = App.BNtable.getBlueNodeEntryById(i).getLoad();
			SocketFunctions.sendFinalData(hostname + " " + phaddress + " " + port + " " + load, writer);
		}
		SocketFunctions.sendFinalData("", writer);
	}
}
