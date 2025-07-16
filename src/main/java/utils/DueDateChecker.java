package utils;

import java.time.LocalDate;
import java.time.ZoneId; // Still needed for Calendar conversion if scheduling with Date
import java.util.*;
import models.*; // Assuming this imports ToDo
import models.board.Board;

public class DueDateChecker {

    public static void scheduleMidnightCheck(List<Board> boards) {
        Timer timer = new Timer(true); // Daemon thread

        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DATE, 1); // Set to next midnight for the first run

        long oneDay = 24 * 60 * 60 * 1000; // milliseconds in a day

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LocalDate today = LocalDate.now();
                // Iterate through a copy of the boards list to avoid ConcurrentModificationException
                // if the original list is modified elsewhere.
                // Or ensure synchronization if the original list can be changed by other threads.
                // For simplicity, assuming the boards list itself is stable for iteration here.
                for (Board board : boards) {
                    // Iterate through a copy of the ToDo list for safety
                    List<ToDo> todosOnBoard = new ArrayList<>(board.getTodoList());
                    for (ToDo todo : todosOnBoard) {
                        LocalDate dueDate = todo.getDueDate();
                        // Check if the ToDo is due today and not already marked as "Completo" (Complete)
                        if (dueDate != null && isSameDay(today, dueDate) && !"Completo".equals(todo.getStatus())) {
                            System.out.println("ToDo due today: " + todo.getTitle() + " on board " + board.getName());
                            // No need to call todo.setColor("red"); as GUI handles visual overdue status.
                            // You might want to add a notification mechanism here, or update status if not done.
                        }
                    }
                }
            }
        }, midnight.getTime(), oneDay);
    }

    /**
     * Checks if two LocalDate objects represent the same day.
     * @param d1 The first LocalDate.
     * @param d2 The second LocalDate.
     * @return true if both dates are the same day, false otherwise.
     */
    private static boolean isSameDay(LocalDate d1, LocalDate d2) {
        // LocalDate objects already represent only the date part, so direct comparison is sufficient.
        return d1.equals(d2);
    }
}