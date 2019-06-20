package org.kostiskag.unitynetwork.tracker.rundata.serviceoperations;

public enum BlueNodeToBlueNode {

    A,
    AAA;

    private String val;

    BlueNodeToBlueNode() {
        //when you call the default constructor, it means that the value is the same as the enum's name
        this.val = this.toString();
    }

    BlueNodeToBlueNode(String value) {
        this.val = value;
    }

    //always use this.value() instead of this.toString()!!!!
    public String value() {
        return val;
    }
}
