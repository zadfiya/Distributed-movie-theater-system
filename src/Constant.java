import java.util.Scanner;

public class Constant {
    
    public static final String SERVER_ATWATER =  "ATWATER";
    public static final String SERVER_VERDUN =  "VERDUN";
    public static final String SERVER_OUTREMONT =  "OUTREMONT";
    public static final int USER_TYPE_CUSTOMER = 1;
    public static final int USER_TYPE_ADMIN = 2;
    public static final String MOVIE_AVTAR =  "Avtar";
    public static final String MOVIE_AVENGER =  "Avenger";
    public static final String MOVIE_TITANIC =  "Titanic";
    public static final int ATWATER_REGISTRY_PORT = 2964;
    public static final int VERDUN_REGISTRY_PORT = 2965;
    public static final int OUTREMONT_REGISTRY_PORT = 2966;
    public static final int ATWATER_SERVER_PORT = 8888;
    public static final int VERDUN_SERVER_PORT = 7777;
    public static final int OUTREMONT_SERVER_PORT = 6666;
    public static final String MOVIE_MANAGEMENT_REGISTERED_NAME= "MovieS";
    public static final int CUSTOMER_BOOK_EVENT = 1;
    public static final int CUSTOMER_GET_BOOKING_SCHEDULE = 2;
    public static final int CUSTOMER_CANCEL_EVENT = 3;
    public static final int CUSTOMER_LOGOUT = 4;
    public static final int ADMIN_ADD_EVENT = 1;
    public static final int ADMIN_REMOVE_EVENT = 2;
    public static final int ADMIN_LIST_EVENT_AVAILABILITY = 3;
    public static final int ADMIN_BOOK_EVENT = 4;
    public static final int ADMIN_GET_BOOKING_SCHEDULE = 5;
    public static final int ADMIN_CANCEL_EVENT = 6;
    public static final int ADMIN_LOGOUT = 7;
    public static String detectServer(String movieID)
    {  if (movieID.substring(0, 3).equalsIgnoreCase("ATW")) {
        return SERVER_ATWATER;
    } else if (movieID.substring(0, 3).equalsIgnoreCase("VER")) {
        //return MovieManagement.THEATER_SERVER_VERDUN;
        return SERVER_VERDUN;
    } else {
        return SERVER_OUTREMONT;
        //return MovieManagement.THEATER_SERVER_OUTREMONT;
    } }

    public static int getServerPort(String branchAcronym) {
        if (branchAcronym.equalsIgnoreCase("ATW")) {
            return ATWATER_REGISTRY_PORT;
        } else if (branchAcronym.equalsIgnoreCase("VER")) {
            return VERDUN_REGISTRY_PORT;
        } else if (branchAcronym.equalsIgnoreCase("OUT")) {
            return OUTREMONT_REGISTRY_PORT;
        }
        return 1;
    }

    public static int checkUserType(String userID) {
        if (userID.length() == 8) {
            if (userID.substring(0, 3).equalsIgnoreCase("ATW") ||
                    userID.substring(0, 3).equalsIgnoreCase("VER") ||
                    userID.substring(0, 3).equalsIgnoreCase("OUT")) {
                if (userID.substring(3, 4).equalsIgnoreCase("C")) {
                    return USER_TYPE_CUSTOMER;
                } else if (userID.substring(3, 4).equalsIgnoreCase("A")) {
                    return USER_TYPE_ADMIN;
                }
            }
        }
        return 0;
    }

    public static String askForCustomerIDFromManager(String branchAcronym,Scanner sc) {
        System.out.println("Please enter a customerID(Within " + branchAcronym + " Server):");
        String userID = sc.next().trim().toUpperCase();
        if (checkUserType(userID) != USER_TYPE_CUSTOMER || !userID.substring(0, 3).equals(branchAcronym)) {
            return askForCustomerIDFromManager(branchAcronym,sc);
        } else {
            return userID;
        }
    }

    public static void printMenu(int userType) {
        System.out.println("*************************************");
        System.out.println("Please choose an option below:");
        if (userType == USER_TYPE_CUSTOMER) {
            System.out.println("1.Book Event");
            System.out.println("2.Get Booking Schedule");
            System.out.println("3.Cancel Event");
            System.out.println("4.Logout");
        } else if (userType == USER_TYPE_ADMIN) {
            System.out.println("1.Add Event");
            System.out.println("2.Remove Event");
            System.out.println("3.List Event Availability");
            System.out.println("4.Book Event");
            System.out.println("5.Get Booking Schedule");
            System.out.println("6.Cancel Event");
            System.out.println("7.Logout");
        }
    }

    public static String promptForMovieName(Scanner sc) {
        System.out.println("*************************************");
        System.out.println("Please choose an Movie Name below:");
        System.out.println("1.Avtar");
        System.out.println("2.Avenger");
        System.out.println("3.Titanic");
        switch (sc.nextInt()) {
            case 1:
                return MOVIE_AVTAR;
            case 2:
                return MOVIE_AVENGER;
            case 3:
                return MOVIE_TITANIC;
            default:
                System.out.print("Invalid Id");
        }
        return promptForMovieName(sc);
    }

    public static String promptForMovieID(Scanner sc) {
        System.out.println("*************************************");
        System.out.println("Please enter the EventID (e.g MTLM190120)");
        String eventID = sc.next().trim().toUpperCase();
        if (eventID.length() == 10) {
            if (eventID.substring(0, 3).equalsIgnoreCase("ATW") ||
                    eventID.substring(0, 3).equalsIgnoreCase("VER") ||
                    eventID.substring(0, 3).equalsIgnoreCase("OUT")) {
                if (eventID.substring(3, 4).equalsIgnoreCase("M") ||
                        eventID.substring(3, 4).equalsIgnoreCase("A") ||
                        eventID.substring(3, 4).equalsIgnoreCase("E")) {
                    return eventID;
                }
            }
        }
        return promptForMovieID(sc);
    }

    public static int promptForCapacity(Scanner sc) {
        System.out.println("*************************************");
        System.out.println("Please enter the booking capacity:");
        return sc.nextInt();
    }

    public static int promptForTicketsCount(Scanner sc) {
        System.out.println("*************************************");
        System.out.println("Please enter the number of Tickets:");
        return sc.nextInt();
    }

}
