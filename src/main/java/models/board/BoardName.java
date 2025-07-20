package models.board;

/**
 * Enum che rappresenta i nomi predefiniti delle board dell'applicazione.
 * Ogni valore ha un nome visualizzabile associato.
 */
public enum BoardName {

    /** Board per attività universitarie */
    UNIVERSITY("University"),

    /** Board per attività lavorative */
    WORK("Work"),

    /** Board per il tempo libero */
    FREE_TIME("Free Time");

    private final String displayName;

    /**
     * Costruisce un valore enum con un nome visualizzabile.
     * @param displayName Nome visualizzabile associato
     */
    BoardName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Restituisce il nome visualizzabile dell'enum.
     * @return Nome visualizzabile
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Restituisce il nome visualizzabile (sovrascrive {@code toString}).
     * @return Nome visualizzabile
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Converte un nome visualizzabile in un valore enum {@code BoardName}.
     * Il confronto non è case-sensitive.
     *
     * @param text Nome visualizzabile da convertire
     * @return Valore {@code BoardName} corrispondente
     * @throws IllegalArgumentException se il nome non corrisponde a nessun valore
     */
    public static BoardName fromDisplayName(String text) {
        for (BoardName b : BoardName.values()) {
            if (b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No enum constant with display name " + text + " found for BoardName");
    }
}
