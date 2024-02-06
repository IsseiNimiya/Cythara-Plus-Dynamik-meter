package com.github.cythara;

public enum NoteName {

    C("C", "Do"),
    D("D", "Re"),
    E("E", "Mi"),
    F("F", "Fa"),
    G("G", "Sol"),
    A("A", "La"),
    B("B", "Si");

    private final String scientific;
    private final String sol;

    NoteName(String scientific, String sol) {
        this.scientific = scientific;
        this.sol = sol;
    }

    public String getScientific() {
        return scientific;
    }
    /**音名を取得するメソッド*/

    public String getSol() {
        return sol;
    }
    /**階名を取得するメソッド*/

    public static NoteName fromScientificName(String scientificName) {
        for (NoteName noteName : NoteName.values()) {
            if (noteName.getScientific().equalsIgnoreCase(scientificName)) {
                return noteName;
            }
        }
        /**どうして、音名の方のみメソッドが用意されているのだろう？*/

        throw new IllegalArgumentException("Could not convert from name: " + scientificName);
    }
}
