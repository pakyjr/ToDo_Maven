package utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import models.*;
import models.board.Board;

public class DueDateChecker {

    public static void scheduleMidnightCheck(List<Board> boards) {
        Timer timer = new Timer(true);

        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DATE, 1); // first run is next midnight

        long oneDay = 24 * 60 * 60 * 1000; // milliseconds in a day

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LocalDate today = LocalDate.now();
                for (Board board : boards) {
                    for (ToDo todo : board.getTodoList()) {
                        LocalDate dueDate = todo.getDueDate();
                        if (dueDate != null && isSameDay(today, dueDate)) {
                            System.out.println("ToDo due today: " + todo.getTitle());
                            todo.setColor("red");
                            // Add any additional actions here (e.g., notify users)
                        }
                    }
                }
            }
        }, midnight.getTime(), oneDay);
    }

    private static boolean isSameDay(LocalDate d1, LocalDate d2) {
        Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
        Date d11 = Date.from(d1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date d12 = Date.from(d2.atStartOfDay(ZoneId.systemDefault()).toInstant());
        c1.setTime(d11); c2.setTime(d12);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}