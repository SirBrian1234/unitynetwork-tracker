package org.kostiskag.unitynetwork.tracker.rundata.serviceoperations;

public enum RedNodeToTracker {

    OFFER_PUBLIC_KEY("OFFERPUB"),
    REVOKE_PUBLIC_KEY("REVOKEPUB"),

    GET_ALL_BLUENODES("GETBNS"),
    GET_RECOMENDED_BLUENODE("GETRBN"),

    GET_BLUENODE_PUBLIC_KEY("GETBNPUB"),
    GET_REDNODE_PUBLIC_KEY("GETRNPUB");

    private String val;

    RedNodeToTracker() {
        //when you call the default constructor, it means that the value is the same as the enum's name
        this.val = this.toString();
    }

    RedNodeToTracker(String value) {
        this.val = value;
    }

    //always use this.value() instead of this.toString()!!!!
    public String value() {
        return val;
    }
}