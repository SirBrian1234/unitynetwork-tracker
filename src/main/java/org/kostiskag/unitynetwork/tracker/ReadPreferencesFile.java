package org.kostiskag.unitynetwork.tracker;

import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Konstantinos Kagiampakis
 */
final class ReadPreferencesFile {

	//These are the imported settings from file
	public String netName;
	public int auth;
	// database
	public String databaseUrl;
	public String user;
	public String password;
	// capacity
	public int bncap;
	public int pingTime;
	public boolean gui = true;
	public boolean log;

	private ReadPreferencesFile() {
	}

	public static ReadPreferencesFile ParseFile(InputStream file) throws IOException {

		ReadPreferencesFile pref = new ReadPreferencesFile();

        Properties cfg = new Properties();
        cfg.load(file);

		pref.netName = cfg.getProperty("NetworkName").strip();
		pref.auth = Integer.parseInt(cfg.getProperty("AuthPort").strip());
		pref.databaseUrl = cfg.getProperty("DatabaseUrl").strip();
		pref.user = cfg.getProperty("DatabaseUser").strip();
		pref.password = cfg.getProperty("DatabsePassword").strip();
		pref.bncap = Integer.parseInt(cfg.getProperty("BlueNodeCapacity").strip());
		pref.pingTime = Integer.parseInt(cfg.getProperty("Ping").strip());
		pref.gui = Boolean.parseBoolean(cfg.getProperty("UseGUI").strip());
		pref.log = Boolean.parseBoolean(cfg.getProperty("Log").strip());

        return pref;
    }

	public static void GenerateFile(File file) throws IOException {
		PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
	    writer.print(String.join("\n",
				"#############################################",
		"#         Unity Tracker Config File         #",
		"#############################################",
		"",
		"#",
		"# Insructions for setting up the config file",
		"#",
		"# Do not comment any variable nor remove any from this file as this will result",
		"# in an application error. Change the value to an appropriate input as described",
		"# instead. If this file gets messed up, you may delete it and it will be",
		"# auto-generated from the app in its next run.",
		"#",
		"",
		"#",
		"# Network and Tracker Settings",
		"#",
		"# First of all what shall be the name of the netwrok?",
		"# Provide a TCP auth port as well. The default is 8000.",
		"NetworkName = UnityNetwork",
		"AuthPort = 8000",
		"",
		"#",
		"# Database Settings",
		"#",
		"# the url should be in this type of form for mysql",
		"# DatabaseUrl = jdbc:mysql://IPaddress:port/database",
		"# DatabaseUser = username",
		"# DatabsePassword = password",
		"#",
		"# the url should be in this type of form for sqlite",
		"# DatabaseUrl = jdbc:sqlite:local_database_file.db",
		"#",
		"DatabaseUrl = jdbc:sqlite:unity.db",
		"DatabaseUser = username",
		"DatabsePassword = password",
		"",
		"#",
		"# Load and Capacity",
		"# Leave zero for an infinite number of connected Blue Nodes",
		"# otherwise set a maximum limit, ex. 100.",
		"#",
		"BlueNodeCapacity = 0",
		"",
		"# ping time in sec",
		"# ping is the time gap where the tracker may search for all active Blue Nodes",
		"# in order to detect whether someone is not responding",
		"Ping = 180",
		"",
		"#",
		"# Application behaviour",
		"#",
		"",
		"# set GUI or command line",
		"# use true or false",
		"UseGUI = true",
		"",
		"# Logging in tracker.log",
		"# use true or false",
		"Log = true",
	    ""));
	    writer.close();		
	}

	@Override
	public String toString() {
		return String.join("\n","ReadPreferencesFile{",
				"netName='" + netName + "\',",
				"auth=" + auth +",",
				"databaseUrl='" + databaseUrl + "\',",
				"user='" + user + "\',",
				"password='" + password + "\',",
				"bncap=" + bncap + ",",
				"pingTime=" + pingTime + ",",
				"gui=" + gui + ",",
				"log=" + log + ",",
				"}");
	}
}