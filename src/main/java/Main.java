import models.ToDo;

import java.time.LocalDate;
public class Main {
    public static void main(String[] args) {

        ToDo T = new ToDo("Giacobbe");
        System.out.println("Prima:" + T.getDueDate());
        int s = 12;
        int d = 2050;
        LocalDate D = LocalDate.now();
        D = LocalDate.of(2023,12,12);
            System.out.println("Valore:" + D.toString());
           T.setDueDate(D);
           System.out.println("Dopo:" + T.getDueDate());

        }
}
