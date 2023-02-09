package net.wa9nnn.rc210.command;

/*
Note .toString is the i18n base key
 */
public enum Command {
    //@formatter:off
    SitePrefix("*2108",	  	ValueType.dtmf,	3,	Locus.misc, 4),
    TTPadTest(  "*2093",		ValueType.dtmf,	5,	Locus.misc, 6),
    SayHours(   "*5104",		ValueType.bool,	5,	Locus.misc),
    Hangtime(   "*1000",	    ValueType.hangTime,	255,	Locus.portOut),
    IIDMinutes(   "*1002",	ValueType.portInt8,	255,	Locus.portOut),
    PIDMinutes(   "*1003",	ValueType.portInt8,	255,	Locus.portOut),
    TxEnable(   "111",	    ValueType.portBool,	1,	Locus.portOut),
    DTMFCovertone(   "113",	ValueType.portBool,	1,	Locus.portOut),
    DTMFMuteTimer(   "*1006",	ValueType.portInt16,	99,	Locus.portOut),
    KerchunkONOFF(   "115",	ValueType.portBool,	99,	Locus.portIn),
    KerchunkTimer(   "*1008",	ValueType.portInt16,	255,	Locus.portIn),
    MuteDigitSelect(   "*2090",	ValueType.int8,	2,	Locus.misc),
    CTCSSDuringID(   "*2089",	ValueType.portBool,	1,	Locus.portOut),
    CTCSSCTControl(   "*2088",	ValueType.portBool,	1,	Locus.portOut),
    TimeoutPorts(   "*2051",	ValueType.bool,	1,	Locus.misc),
    SpeechDelay(   "*1019",	ValueType.int16,	32767,	Locus.misc),
    CTCSSEncodePolarity(   "*1021",	ValueType.portInt8,	255,	Locus.portOut),
    GuestMacroRange(   "*4009",	ValueType.guestMacro,	90,	Locus.misc),

    ;
    //@formatter:on

    private final String base;
    private final int memoryOffset;
    private final int memoryLength;
    private final ValueType valueType;
    private final int max;
    private final Locus locus;


    Command(String base,  ValueType valueType, int max, Locus locus) {
        this.base = base;
        this.memoryLength = valueType.getBytes();
        this.memoryOffset = MemoryPos.ai.getAndAdd(memoryLength);
        this.valueType = valueType;
        this.max = max;
        this.locus = locus;
    }
    Command(String base, ValueType valueType, int max, Locus locus, int requiredBytes) {
        this.base = base;
        this.memoryLength = requiredBytes;
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

