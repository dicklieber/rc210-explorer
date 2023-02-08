package net.wa9nnn.rc210.command;

/*
Note .toString is the i18n base key
 */
public enum Command {
    //@formatter:off
	SitePrefix("*2108",	4,	ValueType.dtmf,	3,	Locus.misc),
    TTPadTest(  "*2093",	6,	ValueType.dtmf,	5,	Locus.misc),
    SayHours(   "*5104",	1,	ValueType.bool,	5,	Locus.misc),
    Hangtime(   "*1000",	9,	ValueType.hangTime,	255,	Locus.portOut),
    IIDMinutes(   "*1002",	3,	ValueType.portInt8,	255,	Locus.portOut),
    PIDMinutes(   "*1003",	3,	ValueType.portInt8,	255,	Locus.portOut),
    TxEnable(   "111",	3,	ValueType.portBool,	1,	Locus.portOut),


    ;
    //@formatter:on

    private final String base;
    private final int memoryOffset;
    private final int memoryLength;
    private final ValueType valueType;
    private final int max;
    private final Locus locus;


    Command(String base, int memoryLength, ValueType valueType, int max, Locus locus) {
        this.base = base;
        this.memoryLength = memoryLength;
        this.memoryOffset = MemoryPos.ai.getAndAdd(memoryLength);
        this.valueType = valueType;
        this.max = max;
        this.locus = locus;
    }


    public String getBase() {
        return base;
    }

    public int getMemoryOffset() {
        return memoryOffset;
    }

    public int getMemoryLength() {
        return memoryLength;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public int getMax() {
        return max;
    }

    public Locus getLocus() {
        return locus;
    }

}

