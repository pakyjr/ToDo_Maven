package models;
import java.util.Date;
public class Main {
    public static void main(String[] args) {

        ToDo T = new ToDo("Giacobbe");
        System.out.println("Prima:" + T.getDueDate());
        int s = 12;
        int d = 2050;
        Date D = new Date(s,s,d);
            System.out.println("Valore:" + D.toString());
           T.setDueDate(D);
           System.out.println("Dopo:" + T.getDueDate());

        }
}
