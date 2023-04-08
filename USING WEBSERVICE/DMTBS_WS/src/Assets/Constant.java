package Assets;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Constant {

    public static boolean isMovieDateWithinOneWeek(String movieDate) {
        try {

            String d = movieDate.substring(0, 2) + "/" + movieDate.substring(2, 4) + "/" + movieDate.substring(4);
            Date movieDateObj = new SimpleDateFormat("dd/MM/yy").parse(d);
            Date currentDate = new Date();
            long diff = movieDateObj.getTime() - currentDate.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000);
            return diffDays < 7 & diffDays>=0 ? true : false;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }
}
