package net.wa9nnn.rc210.command;

public enum ValueType {
    unused(-1), // in [[Memory]] but not needed for  programming. Just eat bytes
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
    guestMacro(7),
    port2Int16(6),
    cwTones(12),
    alarmBool(5)
    ;

    private final int bytes;

    ValueType(int bytes) {
        this.bytes = bytes;
    }

    public int getBytes() {
        return bytes;
    }
}
