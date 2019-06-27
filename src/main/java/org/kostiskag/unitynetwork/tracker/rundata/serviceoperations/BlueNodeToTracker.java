package org.kostiskag.unitynetwork.tracker.rundata.serviceoperations;

public enum BlueNodeToTracker {

    GREET("BLUENODE"),
    OFFERPUB("OFFERPUB"),
    BN_EXITS_ON_PUBLIC_KEY_NOT_SET("EXIT"),
    LEASE("LEASE"),
    LEASE_RN("LEASE_RN"),
    RELEASE("RELEASE"),
    RELEASE_RN("RELEASE_RN"),
    GETPH("GETPH"),
    CHECK_RN("CHECK_RN"),
    CHECK_RNA("CHECK_RNA"),
    LOOKUP_H("LOOKUP_H"),
    LOOKUP_V("LOOKUP_V"),
    GETBNPUB("GETBNPUB"),
    GETRNPUB("GETRNPUB"),
    REVOKEPUB("REVOKEPUB"),

    TRACKER_RESPONCE_TO_AUTHENTICATED_WRONG_OPTION("WRONG_COMMAND"),
    TRACKER_RESPONCE_TO_ALREADY_AUTHENTICATED_TRYING_TO_REAUTH("WRONG_COMMAND");

    private String val;

    BlueNodeToTracker() {
        //when you call the default constructor, it means that the value is the same as the enum's name
        this.val = this.toString();
    }

    BlueNodeToTracker(String value) {
        this.val = value;
    }

    //always use this.value() instead of this.toString()!!!!
    public String value() {
        return val;
    }
}
