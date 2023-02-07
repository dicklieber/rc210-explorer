package net.wa9nnn.rc210.command;

import java.util.concurrent.atomic.AtomicInteger;

/*
Note .toString is the i18n base key
 */
public enum CommandId {
    //@formatter:off
	SitePrefix("*2108",	4,	ValueType.dtmf,	3,	Locus.misc),
    TTPadTest(  "*2093",	6,	ValueType.dtmf,	5,	Locus.misc),
    SayHours(   "*5104",	1,	ValueType.bool,	5,	Locus.misc),
    Hangtime(   "*1000",	9,	ValueType.hangTime,	5,	Locus.portOut)
    ;
    //@formatter:on

    private final String base;
    private final int memoryOffset;
    private final int memoryLength;
    private final ValueType valueType;
    private final int max;
    private final Locus locus;


    CommandId(String base, int memoryLength, ValueType valueType, int max, Locus locus) {
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

