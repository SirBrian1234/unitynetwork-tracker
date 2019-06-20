package org.kostiskag.unitynetwork.tracker.rundata.serviceoperations;

public enum  BlueNodeToRedNode {

    A,
    AAA;

    private String val;

    BlueNodeToRedNode() {
        //when you call the default constructor, it means that the value is the same as the enum's name
        this.val = this.toString();
    }

    BlueNodeToRedNode(String value) {
        this.val = value;
    }

    //always use this.value() instead of this.toString()!!!!
    public String value() {
        return val;
    }
}
