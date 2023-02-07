package net.wa9nnn.rc210.command;

import java.util.concurrent.atomic.AtomicInteger;

public class MemoryPos {
    /**
     * Used to calculate the runnin g position within the Memory data
     * when constructing net.wa9nnn.rc210.command.CommandId
     * Java Enum won't allow a static member within an enum definition.
     */
    static AtomicInteger ai = new AtomicInteger();
}
