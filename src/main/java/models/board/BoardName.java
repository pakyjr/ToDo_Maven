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
}