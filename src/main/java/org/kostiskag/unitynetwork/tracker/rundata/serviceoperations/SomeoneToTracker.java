package org.kostiskag.unitynetwork.tracker.rundata.serviceoperations;

public enum SomeoneToTracker {

    GETPUB("GETPUB"),
    OFFERPUB("OFFERPUB"),
    EXIT_ON_PUBLIC_KEY_NOT_SET("EXIT"),
    TRACKER_GREET_TO_OUTER_HANDSHAKE("UnityTracker"),
    TRACKER_PERMITS_AUTHENTICATED_OPTIONS_AFTER_HANDSHAKE("OK"),
    TRACKER_DENIES_AUTHENTICATED_OPTIONS_AFTER_FAILED_HANDSHAKE("NOT_ALLOWED"),
    TRACKER_RESPONCE_TO_IMPROPER_GREETING("WRONG_COMMAND"),
    TRACKER_RESPONCE_TO_PUBLIC_NOT_SET("PUBLIC_NOT_SET"),
    TRACKER_RESPONCE_TO_PUBLIC_FAILED_SUBMISSION_AUTHENTICATION("NOT_ALLOWED");

    private String val;

    SomeoneToTracker() {
        //when you call the default constructor, it means that the value is the same as the enum's name
        this.val = this.toString();
    }

    SomeoneToTracker(String value) {
        this.val = value;
    }

    //always use this.value() instead of this.toString()!!!!
    public String value() {
        return val;
    }
}
