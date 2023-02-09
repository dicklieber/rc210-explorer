package net.wa9nnn.rc210.command;

public enum Locus {
    misc("misc"),
    portOut("portOut"),
    portIn("portIn"),
    schedules("schedules"),
    macro("macro"),
    codes("codes");

    private final String u18n;

    public String getU18n() {
        return u18n;
    }

    Locus(String u18n) {
        this.u18n = u18n;
    }
}
