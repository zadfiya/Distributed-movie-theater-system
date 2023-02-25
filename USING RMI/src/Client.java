import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    static Scanner sc;
    public static void main(String args[]) throws Exception
    {
        System.out.println("\n|===========================================================|");
        System.out.println("| Welcome To DMTBS: Distributed Movie Ticket Booking System |");
        System.out.println("|===========================================================|");
        System.out.println("\nSystem is made by:\n");
        System.out.println("\t\t\t Naren Zadafiya (40232646)\n\n");
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
                    Logger.clientLog(userID, " Customer Login successful");
                    customer(userID, Constant.getServerPort(userID.substring(0, 3)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Constant.USER_TYPE_ADMIN:
                try {
                    System.out.println("Admin Login successful (" + userID + ")");
                    Logger.clientLog(userID, " Admin Login successful");
                    admin(userID, Constant.getServerPort(userID.substring(0, 3)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("!!UserID is not in correct format");

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
            case Constant.CUSTOMER_BOOK_MOVIE:
            {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                numberOfTickets = Constant.promptForTicketsCount(sc);
                Logger.clientLog(customerID, " attempting to book movie for "+ movieName+" with movieId "+ movieID+" and number of Tickets : "+numberOfTickets);
                serverResponse = remoteObject.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
                Logger.clientLog(customerID, " bookMovieShow", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);
                System.out.println(serverResponse);
                break;
            }
            case Constant.CUSTOMER_CANCEL_MOVIE:
            {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                numberOfTickets = Constant.promptForTicketsCount(sc);
                Logger.clientLog(customerID, " attempting to cancel movie for "+ movieName+" with movieId "+ movieID+" and number of Tickets : "+numberOfTickets);
                serverResponse = remoteObject.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(customerID, " cancelMovieShow", " movieID: " + movieID + " movieName: " + movieName +" numberOfTickets: "+numberOfTickets + " ", serverResponse);

                break;
            }
            case Constant.CUSTOMER_GET_BOOKING_SCHEDULE:
            {
                Logger.clientLog(customerID, " attempting to get Movie Schedule");
                serverResponse = remoteObject.getBookingSchedule(customerID);

                System.out.println(serverResponse);
                Logger.clientLog(customerID, " getBookingSchedule", " null ", serverResponse);
                break;
            }
            case Constant.CUSTOMER_LOGOUT:
            {
                System.out.println(customerID + " Logout Successfully!!");
                Logger.clientLog(customerID, " Logout Successfully");
                repeat=false;
                init();
                break;
            }
            default:
                System.out.println("Please Enter Valid Choice.");
        }
        if(repeat)
        {
           customer(customerID,serverPort);

        }
    }

    private static void admin(String adminID, int serverPort) throws Exception{

        if (serverPort == 1) {
            return;
        }
        Registry registry = LocateRegistry.getRegistry(serverPort);
        MovieManagementInterface remoteObject = (MovieManagementInterface) registry.lookup(Constant.MOVIE_MANAGEMENT_REGISTERED_NAME);
        boolean repeat = true;
        Constant.printMenu(Constant.USER_TYPE_ADMIN);

        String movieName;
        String movieID;
        String serverResponse;
        int bookingCapacity;
        int menuSelection = sc.nextInt();
        switch(menuSelection)
        {
            case Constant.ADMIN_ADD_MOVIE: {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                if(!Constant.isMovieDateWithinOneWeek(movieID.substring(4))){
                    System.out.println("You Can enter movie show for this week only!");
                    break;
                }
                bookingCapacity = Constant.promptForCapacity(sc);
                Logger.clientLog(adminID, " attempting to Add Slot for "+ movieName+" with movieId "+ movieID+" and capacity: "+bookingCapacity);
                serverResponse = remoteObject.addMovieSlot(movieID,movieName,bookingCapacity);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " addMovieSlot", " movieID: " + movieID + " movieName: " + movieName +" capacity: "+bookingCapacity+ " ", serverResponse);

                break;
            }
            case Constant.ADMIN_REMOVE_MOVIE:
            {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                Logger.clientLog(adminID, " attempting to remove movie slot for "+ movieName+" with movieId"+ movieID);

                serverResponse = remoteObject.removeMovieSlots(movieID,movieName);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " removeMovieSlot", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);

                break;
            }
            case Constant.ADMIN_LIST_MOVIE_AVAILABILITY:{
                movieName = Constant.promptForMovieName(sc);
                Logger.clientLog(adminID, " attempting to list schedule of movie for "+ movieName);

                serverResponse = remoteObject.listMovieShowAvailability(movieName);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " listMovieShowAvailability", " movieName: " + movieName + " ", serverResponse);

                break;
            }
            case Constant.ADMIN_BOOK_MOVIE:{
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                bookingCapacity = Constant.promptForTicketsCount(sc);
                Logger.clientLog(adminID, " attempting to book Slot for "+ movieName+" with movieId"+ movieID+" and capacity: "+bookingCapacity);
                serverResponse = remoteObject.bookMovieTickets(adminID, movieID, movieName, bookingCapacity);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " bookMovieShow", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);

                break;
            }
            case Constant.ADMIN_CANCEL_MOVIE:{
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                bookingCapacity = Constant.promptForCapacity(sc);
                Logger.clientLog(adminID, " attempting to cancel movie seats for "+ movieName+" with movieId"+ movieID+" and capacity: "+bookingCapacity);

                serverResponse = remoteObject.cancelMovieTickets(adminID, movieID, movieName, bookingCapacity);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " cancelMovieShow", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);

                break;
            }
            case Constant.ADMIN_GET_BOOKING_SCHEDULE:{
                Logger.clientLog(adminID, " attempting to get Movie Schedule");
                serverResponse = remoteObject.getBookingSchedule(adminID);
                System.out.println(serverResponse);
                Logger.clientLog(adminID, " getBookingSchedule", " null ", serverResponse);

                break;
            }
            case Constant.ADMIN_LOGOUT:{
                repeat=false;
                System.out.println(adminID + " Logout Successfully!!");
                Logger.clientLog(adminID, " Logout Successfully");
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
