package org.kostiskag.unitynetwork.tracker.rundata.calculated;

public enum NumericConstraints {

    // user input max sizes
    MAX_INT_STR(32),
    MAX_STR_LEN_SMALL(128),
    MAX_STR_LEN_LARGE(256),
    MIN_PASSWORD(5),
    MIN_USERNAME(4),
    MAX_STR_ADDR("255.255.255.255".length()),
    MIN_STR_ADDR("1.1.1.1".length()),
    MAX_ALLOWED_PORT_NUM(65535),

    // network maths
    VIRTUAL_NETWORK_ADDRESS_CAPACITY((int) (Math.pow(2, 24) - 2)),
    SYSTEM_RESERVED_ADDRESS_NUMBER(1);

    private int len;

    NumericConstraints(int size) {
        this.len = size;
    }

    public int size() {
        return len;
    }
}
