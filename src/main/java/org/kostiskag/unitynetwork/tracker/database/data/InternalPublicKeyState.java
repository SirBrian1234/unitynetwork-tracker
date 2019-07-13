package org.kostiskag.unitynetwork.tracker.database.data;

public enum InternalPublicKeyState {
    NOT_SET,
    KEY_SET;

    String value;

    InternalPublicKeyState() {
        value = this.name();
    }

    InternalPublicKeyState(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
