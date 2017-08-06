import java.util.Calendar;

/**
 * Created by hoangnh on 05/08/2017.
 */
public class TestCalendar {
    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        java.sql.Date startDate = new java.sql.Date(calendar.getTime().getTime());
        System.out.println(startDate);
    }
}
