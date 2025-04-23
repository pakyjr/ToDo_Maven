package utils;

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
                Date today = new Date();
                for (Board board : boards) {
                    for (ToDo todo : board.getTodoList()) {
                        Date dueDate = todo.getDueDate();
                        if (dueDate != null && isSameDay(today, dueDate)) {
                            System.out.println("ToDo due today: " + todo.getTitle());
                            // Add any additional actions here (e.g., notify users)
                        }
                    }
                }
            }
        }, midnight.getTime(), oneDay);
    }

    private static boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
        c1.setTime(d1); c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}