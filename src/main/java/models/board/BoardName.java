package models.board;

public enum BoardName {

    UNIVERSITY("University"),
    WORK("Work"),
    FREE_TIME("Free Time");

    private final String displayName;

    BoardName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static BoardName fromDisplayName(String text) {
        for (BoardName b : BoardName.values()) {
            if (b.displayName.equalsIgnoreCase(text)) { // Case-insensitive comparison
                return b;
            }
        }
        throw new IllegalArgumentException("No enum constant with display name " + text + " found for BoardName");
    }

}