package org.kostiskag.unitynetwork.tracker;

import java.io.*;
import java.nio.file.Path;

import org.kostiskag.unitynetwork.common.utilities.ReadPreferencesFile;


/**
 *
 * @author Konstantinos Kagiampakis
 */
final class ReadTrackerPreferencesFile {

	//These are the imported settings from file
	public final String netName;
	public final int auth;
	// database
	public final String databaseUrl;
	public final String user;
	public final String password;
	// capacity
	public final int bncap;
	public final int pingTime;
	public final boolean gui;
	public final boolean log;

	public ReadTrackerPreferencesFile(Path filePath) throws IOException {
		var prefs = ReadPreferencesFile.readPreferencesFile(filePath);

		this.netName = prefs.getProperty("NetworkName").strip();
		this.auth = Integer.parseInt(prefs.getProperty("AuthPort").strip());
		this.databaseUrl = prefs.getProperty("DatabaseUrl").strip();
		this.user = prefs.getProperty("DatabaseUser").strip();
		this.password = prefs.getProperty("DatabsePassword").strip();
		this.bncap = Integer.parseInt(prefs.getProperty("BlueNodeCapacity").strip());
		this.pingTime = Integer.parseInt(prefs.getProperty("Ping").strip());
		this.gui = Boolean.parseBoolean(prefs.getProperty("UseGUI").strip());
		this.log = Boolean.parseBoolean(prefs.getProperty("Log").strip());
	}

	public static ReadTrackerPreferencesFile ParseFile(Path filePath) throws IOException {
		return new ReadTrackerPreferencesFile(filePath);
    }

	public static void GenerateFile(Path filePath) throws IOException {
		ReadPreferencesFile.generateFile(filePath, () -> String.join("\n",
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