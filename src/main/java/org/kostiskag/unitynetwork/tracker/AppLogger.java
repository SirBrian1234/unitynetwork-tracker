package org.kostiskag.unitynetwork.tracker;

import org.kostiskag.unitynetwork.tracker.gui.MainWindow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class AppLogger {

    private static AppLogger APP_LOGGER;

    private final MainWindow window;
    private final File loggersFile;

    public static AppLogger newInstance(MainWindow window, File loggersFile) {
        if (APP_LOGGER == null) {
            APP_LOGGER = new AppLogger(window, loggersFile);
        }
        return APP_LOGGER;
    }

    public static AppLogger getLogger() {
        return APP_LOGGER;
    }

    /**
     * depending whether gui has a window or has a log file
     * @param window
     * @param loggersFile
     */
    private AppLogger(MainWindow window, File loggersFile) {
        this.window = window;
        this.loggersFile = loggersFile;
    }

    public synchronized void consolePrint(String message) {
        if (window != null) {
            window.verboseInfo(message);
        }
        System.out.println(message);

        if (loggersFile != null) {
            try (FileWriter fw = new FileWriter(App.LOG_FILE_NAME, true);) {
                fw.append(message + "\n");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
