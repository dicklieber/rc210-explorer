package net.wa9nnn.rc210.command;

public enum ValueType {
    dtmf(-1), // must be specified
    text(-1), // must be specified
    bool(1),
    int8(1),
    int16(2),
    hangTime(9),
    portInt8(3),
    range(4),
    portBool(3),
    portInt16(6),
    portUnlock(27),
    guestMacro(7);

    private final int bytes;

    ValueType(int bytes) {
        this.bytes = bytes;
    }

    public int getBytes() {
        return bytes;
    }
}
