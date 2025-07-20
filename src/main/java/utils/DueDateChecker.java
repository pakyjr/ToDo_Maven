package utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import models.*;
import models.board.Board;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Classe di utilità che pianifica un controllo giornaliero per evidenziare
 * i ToDo con scadenza odierna. Utilizza un {@link Timer} per eseguire il controllo a mezzanotte.
 */
public class DueDateChecker {

    /**
     * Pianifica un controllo giornaliero a mezzanotte per tutti i ToDo nelle board fornite.
     * Se un ToDo ha una scadenza impostata per il giorno corrente, il colore viene impostato su rosso.
     *
     * @param boards Lista di board da controllare ogni giorno
     */
    public static void scheduleMidnightCheck(List<Board> boards) {
        Timer timer = new Timer(true); // daemon thread

        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DATE, 1); // prima esecuzione: prossima mezzanotte

        long oneDay = 24 * 60 * 60 * 1000; // millisecondi in un giorno

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LocalDate today = LocalDate.now();
                for (Board board : boards) {
                    for (ToDo todo : board.getTodoList()) {
                        LocalDate dueDate = todo.getDueDate();
                        if (dueDate != null && isSameDay(today, dueDate)) {
                            System.out.println("ToDo due today: " + todo.getTitle());
                            todo.setColor("red"); // evidenzia ToDo in scadenza
                            // Qui si possono aggiungere notifiche o logica personalizzata
                        }
                    }
                }
            }
        }, midnight.getTime(), oneDay);
    }

    /**
     * Confronta due date per verificare se rappresentano lo stesso giorno (ignora orario).
     *
     * @param d1 Prima data
     * @param d2 Seconda data
     * @return true se le date rappresentano lo stesso giorno
     */
    private static boolean isSameDay(LocalDate d1, LocalDate d2) {
        Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
        Date d11 = Date.from(d1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date d12 = Date.from(d2.atStartOfDay(ZoneId.systemDefault()).toInstant());
        c1.setTime(d11);
        c2.setTime(d12);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
