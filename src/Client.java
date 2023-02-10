import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    static Scanner sc;
    public static void main(String args[]) throws Exception
    {
        init();
    }

    public static void init() throws IOException
    {
        sc=new Scanner(System.in);
        String userID;
        System.out.println("Please Enter your UserID:");
        userID = sc.next().trim().toUpperCase();
        switch (Constant.checkUserType(userID)) {
            case Constant.USER_TYPE_CUSTOMER:
                try {
                    System.out.println("Customer Login successful (" + userID + ")");
                    //Logger.clientLog(userID, " Customer Login successful");
                    customer(userID, Constant.getServerPort(userID.substring(0, 3)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Constant.USER_TYPE_ADMIN:
                try {
                    System.out.println("Manager Login successful (" + userID + ")");
                    //Logger.clientLog(userID, " Manager Login successful");
                    admin(userID, Constant.getServerPort(userID.substring(0, 3)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("!!UserID is not in correct format");
                //Logger.clientLog(userID, " UserID is not in correct format");
                //Logger.deleteALogFile(userID);
                init();
        }
    }

    private static void customer(String customerID, int serverPort) throws Exception{
        if (serverPort == 1) {
            return;
        }
        Registry registry = LocateRegistry.getRegistry(serverPort);
        MovieManagementInterface remoteObject = (MovieManagementInterface) registry.lookup(Constant.MOVIE_MANAGEMENT_REGISTERED_NAME);
        boolean repeat = true;
        Constant.printMenu(Constant.USER_TYPE_CUSTOMER);
        int menuSelection = sc.nextInt();
        String movieName;
        String movieID;
        String serverResponse;
        int numberOfTickets;

        switch (menuSelection) {
            case Constant.CUSTOMER_BOOK_EVENT:
            {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                numberOfTickets = Constant.promptForTicketsCount(sc);
               //Logger.clientLog(customerID, " attempting to bookEvent");
                serverResponse = remoteObject.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                break;
            }
            case Constant.CUSTOMER_CANCEL_EVENT:
            {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                numberOfTickets = Constant.promptForTicketsCount(sc);
                //Logger.clientLog(customerID, " attempting to bookEvent");
                serverResponse = remoteObject.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                break;
            }
            case Constant.CUSTOMER_GET_BOOKING_SCHEDULE:
            {
                serverResponse = remoteObject.getBookingSchedule(customerID);
                System.out.println(serverResponse);
                break;
            }
            case Constant.CUSTOMER_LOGOUT:
            {
                System.out.println(customerID + " Logout Successfully!!");
                init();
                break;
            }
            default:
                System.out.println("Please Enter Valid Choice");
        }
        if(repeat)
        {
           customer(customerID,serverPort);

        }
    }

    private static void admin(String adminID, int serverPort) throws Exception{
        System.out.println("bcd");
        if (serverPort == 1) {
            return;
        }
        System.out.println("abc");
        Registry registry = LocateRegistry.getRegistry(serverPort);
        MovieManagementInterface remoteObject = (MovieManagementInterface) registry.lookup(Constant.MOVIE_MANAGEMENT_REGISTERED_NAME);
        boolean repeat = true;
        Constant.printMenu(Constant.USER_TYPE_ADMIN);
        String customerID;
        String movieName;
        String movieID;
        String serverResponse;
        int bookingCapacity;
        int menuSelection = sc.nextInt();
        switch(menuSelection)
        {
            case Constant.ADMIN_ADD_EVENT: {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                bookingCapacity = Constant.promptForCapacity(sc);
                serverResponse = remoteObject.addMovieSlot(movieID,movieName,bookingCapacity);
                System.out.println(serverResponse);
                break;
            }
            case Constant.ADMIN_REMOVE_EVENT:
            {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                bookingCapacity = Constant.promptForCapacity(sc);
                serverResponse = remoteObject.removeMovieSlots(movieID,movieName);
                System.out.println(serverResponse);
                break;
            }
            case Constant.ADMIN_LIST_EVENT_AVAILABILITY:{
                movieName = Constant.promptForMovieName(sc);

                serverResponse = remoteObject.listEventAvailability(movieName);
                System.out.println(serverResponse);
                break;
            }
            case Constant.ADMIN_BOOK_EVENT:{
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                bookingCapacity = Constant.promptForTicketsCount(sc);
                serverResponse = remoteObject.bookMovieTickets(adminID, movieID, movieName, bookingCapacity);
                System.out.println(serverResponse);
            }
            case Constant.ADMIN_CANCEL_EVENT:{
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                bookingCapacity = Constant.promptForCapacity(sc);
            }
            case Constant.ADMIN_GET_BOOKING_SCHEDULE:{
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                bookingCapacity = Constant.promptForCapacity(sc);
            }
            case Constant.ADMIN_LOGOUT:{
                repeat=false;
                init();
                break;
            }
            default:
            {
                System.out.println("Please Enter valid Choice");
            }

        }
        if (repeat) {
            admin(adminID, serverPort);
        }
    }

}
