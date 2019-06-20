package org.kostiskag.unitynetwork.tracker.rundata.serviceoperations;

public enum  TrackerToBlueNode {

    CHECK_IF_ALIVE("CHECK"),
    KILLING_SIGNAL("KILLSIG"),
    GET_ALL_LEASED_REDNODES("GETREDNODES");

    private String val;

    TrackerToBlueNode() {
        //when you call the default constructor, it means that the value is the same as the enum's name
        this.val = this.toString();
    }

    TrackerToBlueNode(String value) {
        this.val = value;
    }

    //always use this.value() instead of this.toString()!!!!
    public String value() {
        return val;
    }
}
