package com.web.client;

import Assets.Constant;
import Assets.StringAssets;

import Logger.Logger;
import com.web.service.WebInterface;


import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client {
    static Scanner input;
    public static final int USER_TYPE_CUSTOMER = 1;
    public static final int USER_TYPE_ADMIN = 2;
    public static final int CUSTOMER_BOOK_MOVIE = 1;
    public static final int CUSTOMER_GET_MOVIE_SCHEDULE = 2;
    public static final int CUSTOMER_CANCEL_MOVIE = 3;
    public static final int CUSTOMER_EXCHANGE_MOVIE = 4;
    public static final int CUSTOMER_LOGOUT = 5;
    public static final int ADMIN_ADD_MOVIE = 1;
    public static final int ADMIN_REMOVE_MOVIE = 2;
    public static final int ADMIN_LIST_MOVIE_AVAILABILITY = 3;
    public static final int ADMIN_BOOK_MOVIE = 4;
    public static final int ADMIN_GET_BOOKING_SCHEDULE = 5;
    public static final int ADMIN_CANCEL_MOVIE = 6;
    public static final int ADMIN_EXCHANGE_MOVIE = 7;
    public static final int ADMIN_LOGOUT = 8;
    public static Service atwaterService;
    public static Service verdunService;
    public static Service outremontService;
    private static WebInterface obj;
    public static void main(String[] args) throws Exception{
        System.out.println("\n|===========================================================|");
        System.out.println("| Welcome To DMTBS: Distributed Movie Ticket Booking System |");
        System.out.println("|===========================================================|");
        System.out.println("\nSystem is made by:\n");
        System.out.println("\t\t\t Naren Zadafiya (40232646)\n\n");

        URL atwaterURL = new URL("http://localhost:8080/atwater?wsdl");
        QName atwaterQName = new QName("http://Implementation.service.web.com/","MovieManagerService");
        atwaterService = Service.create(atwaterURL,atwaterQName);

        URL verdunURL = new URL("http://localhost:8080/verdun?wsdl");
        QName verdunrQName = new QName("http://Implementation.service.web.com/","MovieManagerService");
        verdunService = Service.create(verdunURL,verdunrQName);

        URL outremontURL = new URL("http://localhost:8080/outremont?wsdl");
        QName outremontQName = new QName("http://Implementation.service.web.com/","MovieManagerService");
        outremontService = Service.create(outremontURL,outremontQName);
        init();

    }

    public static void init() throws Exception{
        input = new Scanner(System.in);
        String userId;
        System.out.println("*************************************");
        System.out.println("*************************************");
        System.out.println("Enter your UserID: ");
        userId=input.next().trim().toUpperCase();
        Logger.clientLog(userId, "Login attempt");
        switch (checkUserType(userId)){
            case USER_TYPE_CUSTOMER:
                try {
                    System.out.println("Customer Login successful (" + userId + ")");
                    Logger.clientLog(userId, " Customer Login successful");
                    customer(userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case USER_TYPE_ADMIN:
                try {
                    System.out.println("Admin Login successful (" + userId + ")");
                    Logger.clientLog(userId, " Admin Login successful");
                    admin(userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                System.out.println("UserID is not in correct format. Please enter correct UserID");
                Logger.clientLog(userId, " UserID is not in correct format");
                Logger.deleteALogFile(userId);
                init();
        }
    }
    public static int checkUserType(String userId){
        if (userId.length() == 8) {
            if (userId.substring(0, 3).equalsIgnoreCase("ATW") ||
                    userId.substring(0, 3).equalsIgnoreCase("OUT") ||
                    userId.substring(0, 3).equalsIgnoreCase("VER")) {
                if (userId.substring(3, 4).equalsIgnoreCase("C")) {
                    return USER_TYPE_CUSTOMER;
                } else if (userId.substring(3, 4).equalsIgnoreCase("A")) {
                    return USER_TYPE_ADMIN;
                }
            }
        }
        return 0;
    }
    private static void customer(String customerID) throws Exception {
        String serverId = getServerId(customerID);
        if (serverId.equals("1")) {
            init();
        }
        boolean repeat = true;
        printMenu(USER_TYPE_CUSTOMER);
        int menuSelection = input.nextInt();
        String movieName;
        String movieID;
        String serverResponse;
        int numberOfTickets;
        switch (menuSelection) {
            case CUSTOMER_BOOK_MOVIE:
                movieName = promptForMovieType();
                movieID = promptForMovieID();
                numberOfTickets = promptForNumberOfTickets();
                Logger.clientLog(customerID, " attempting to book Movie");
                serverResponse = obj.bookMoviesTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(customerID, " bookMovie", " movieID: " + movieID + " movieName: " + movieName + " Number of tickets: "+numberOfTickets+" ", serverResponse);
                break;
            case CUSTOMER_GET_MOVIE_SCHEDULE:
                Logger.clientLog(customerID, " attempting to getMovieSchedule");
                serverResponse = obj.getBookingSchedule(customerID);
                System.out.println(serverResponse);
                Logger.clientLog(customerID, " bookMovie", " null ", serverResponse);
                break;
            case CUSTOMER_CANCEL_MOVIE:
                movieName = promptForMovieType();
                movieID = promptForMovieID();
                numberOfTickets = promptForNumberOfTickets();
                Logger.clientLog(customerID, " attempting to cancelEvent");
                serverResponse = obj.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(customerID, " cancelMovie", " movieID: " + movieID + " movieName: " + movieName + " Number of tickets: "+numberOfTickets+" ", serverResponse);
                break;
            case CUSTOMER_EXCHANGE_MOVIE:
                System.out.println("Old Movie Details-");
                String oldMovieName = promptForMovieType();
                String oldMovieId = promptForMovieID();
                System.out.println("New Movie Details");
                movieName = promptForMovieType();
                movieID = promptForMovieID();
                numberOfTickets = promptForNumberOfTickets();
                serverResponse = obj.exchangeTickets(customerID,oldMovieName,oldMovieId,movieID,movieName,numberOfTickets);
                System.out.println(serverResponse);
                break;
            case CUSTOMER_LOGOUT:
                repeat = false;
                Logger.clientLog(customerID, " attempting to Logout");
                init();
                break;
        }
        if (repeat) {
            customer(customerID);
        }
    }
    private static void admin(String movieAdminId) throws Exception {
        String serverId = getServerId(movieAdminId);
        if (serverId.equals("1")) {
            init();
        }

        boolean repeat = true;
        printMenu(USER_TYPE_ADMIN);
        String customerID;
        String movieName;
        String movieID;
        String serverResponse;
        int numberOfTickets;
        int menuSelection = input.nextInt();
        switch (menuSelection) {
            case ADMIN_ADD_MOVIE:
                movieName = promptForMovieType();
                movieID = promptForMovieID();
                if(!Constant.isMovieDateWithinOneWeek(movieID.substring(4)))
                 {
                    System.out.println("You Can enter movie show for this week only!");
                    break;
                 }
                numberOfTickets = promptForNumberOfTickets();
                Logger.clientLog(movieAdminId, " attempting to addMovie");
                serverResponse = obj.addMovieSlots(movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(movieAdminId, " addMovie", " movieId: " + movieID + " movieName: " + movieName + " movieCapacity: " + numberOfTickets + " ", serverResponse);
                break;
            case ADMIN_REMOVE_MOVIE:
                movieName = promptForMovieType();
                movieID = promptForMovieID();
                Logger.clientLog(movieAdminId, " attempting to removeMovie");
                serverResponse = obj.removeMovieSlots(movieID, movieName);
                System.out.println(serverResponse);
                Logger.clientLog(movieAdminId, " removeMovie", " movieId: " + movieID + " movieName: " + movieName + " ", serverResponse);
                break;
            case ADMIN_LIST_MOVIE_AVAILABILITY:
                movieName = promptForMovieType();
                Logger.clientLog(movieAdminId, " attempting to listMovieAvailability");
                serverResponse = obj.listMovieShowsAvailability(movieName);
                System.out.println(serverResponse);
                Logger.clientLog(movieAdminId, " listMovieAvailability", " movieName: " + movieName + " ", serverResponse);
                break;
            case ADMIN_BOOK_MOVIE:
                customerID = askForCustomerIDFromAdmin(movieAdminId.substring(0, 3));
                movieName = promptForMovieType();
                movieID = promptForMovieID();
                numberOfTickets = promptForNumberOfTickets();
                Logger.clientLog(movieAdminId, " attempting to bookMovie");
                serverResponse = obj.bookMoviesTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(movieAdminId, " bookMovie", " customerID: " + customerID + " movieId: " + movieID + " movieName: " + movieName + " movieCapacity: "+numberOfTickets+" ", serverResponse);
                break;
            case ADMIN_GET_BOOKING_SCHEDULE:
                customerID = askForCustomerIDFromAdmin(movieAdminId.substring(0, 3));
                Logger.clientLog(movieAdminId, " attempting to getBookingSchedule");
                serverResponse = obj.getBookingSchedule(customerID);
                System.out.println(serverResponse);
                Logger.clientLog(movieAdminId, " getBookingSchedule", " customerID: " + customerID + " ", serverResponse);
                break;
            case ADMIN_CANCEL_MOVIE:
                customerID = askForCustomerIDFromAdmin(movieAdminId.substring(0, 3));
                movieName = promptForMovieType();
                movieID = promptForMovieID();
                numberOfTickets = promptForNumberOfTickets();
                Logger.clientLog(movieAdminId, " attempting to cancelMovie");
                serverResponse = obj.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                Logger.clientLog(movieAdminId, " cancelMovie", " customerID: " + customerID + " movieId: " + movieID + " movieName: " + movieName +" numberOfTickets: " + numberOfTickets +  " ", serverResponse);
                break;
            case ADMIN_EXCHANGE_MOVIE:

                customerID = askForCustomerIDFromAdmin(movieAdminId.substring(0,3));
                System.out.println("Old Movie Details-");
                String oldMovieName = promptForMovieType();
                String oldMovieId = promptForMovieID();
                System.out.println("New Movie Details");
                movieName = promptForMovieType();
                movieID = promptForMovieID();
                numberOfTickets = promptForNumberOfTickets();
                serverResponse = obj.exchangeTickets(customerID,oldMovieName,oldMovieId,movieID,movieName,numberOfTickets);
                System.out.println(serverResponse);
                break;

            case ADMIN_LOGOUT:
                repeat = false;
                Logger.clientLog(movieAdminId, "attempting to Logout");
                init();
                break;
        }
        if (repeat) {
            admin(movieAdminId);
        }
    }

    private static void printMenu(int userType) {
        System.out.println("*************************************");
        System.out.println("Please choose an option below:");
        if (userType == USER_TYPE_CUSTOMER) {
            System.out.println("1.Book Movie Tickets");
            System.out.println("2.Get Booking Schedule");
            System.out.println("3.Cancel Movie");
            System.out.println("4.Exchange Tickets");
            System.out.println("5.Logout");
        } else if (userType == USER_TYPE_ADMIN) {
            System.out.println("1.Add Movie");
            System.out.println("2.Remove Movie");
            System.out.println("3.List Movie Availability");
            System.out.println("4.Book Movie");
            System.out.println("5.Get Booking Schedule");
            System.out.println("6.Cancel Movie");
            System.out.println("7.Exchange Tickets");
            System.out.println("8.Logout");
        }
    }
    private static String promptForMovieType() throws InputMismatchException {
        System.out.println("*************************************");
        System.out.println("Please choose a Movie name from below:");
        System.out.println("1.Avatar");
        System.out.println("2.Avengers");
        System.out.println("3.Titanic");
        input.nextLine();
        int movieIndex = input.nextInt();
        switch (movieIndex) {
            case 1:
                return StringAssets.AVATAR_MOVIE;
            case 2:
                return StringAssets.AVENGERS_MOVIE;
            case 3:
                return StringAssets.TITANIC_MOVIE;
        }
        return promptForMovieType();
    }
    private static String promptForMovieID() {
        System.out.println("*************************************");
        System.out.println("Please enter the MovieID");
        String eventID = input.next().trim().toUpperCase();
        if (eventID.length() == 10) {
            if (eventID.substring(0, 3).equalsIgnoreCase("ATW") ||
                    eventID.substring(0, 3).equalsIgnoreCase("OUT") ||
                    eventID.substring(0, 3).equalsIgnoreCase("VER")) {
                if (eventID.substring(3, 4).equalsIgnoreCase("M") ||
                        eventID.substring(3, 4).equalsIgnoreCase("A") ||
                        eventID.substring(3, 4).equalsIgnoreCase("E")) {
                    return eventID;
                }
                else{
                    System.out.println("Invalid Movie Slot, Please Try Again");
                }
            }
            else{
                System.out.println("Invalid Server ID, Please Try Again");
            }
        }
        else{
            System.out.println("Invalid Movie ID, Please Try Again");
        }
        return promptForMovieID();
    }

    private static int promptForNumberOfTickets(){
        System.out.println("*************************************");
        System.out.println("Please enter the number of tickets: ");
        int numberOfTickets = Integer.parseInt(input.next().trim());
        return numberOfTickets;
    }
    private static String getServerId(String userId) {
        String branchAcronym = userId.substring(0,3);
        if (branchAcronym.equalsIgnoreCase("ATW")){
            obj = atwaterService.getPort(WebInterface.class);
            return branchAcronym;
        } else if(branchAcronym.equalsIgnoreCase("VER")){
            obj = verdunService.getPort(WebInterface.class);
            return branchAcronym;
        } else if(branchAcronym.equalsIgnoreCase("OUT")){
            obj = outremontService.getPort(WebInterface.class);
            return branchAcronym;
        }
        return "1";
    }
    private static String askForCustomerIDFromAdmin(String branchAcronym) {
        System.out.println("Please enter a customerID(Within " + branchAcronym + " Server):");
        String userID = input.next().trim().toUpperCase();
        if (checkUserType(userID) != USER_TYPE_CUSTOMER || !userID.substring(0, 3).equals(branchAcronym)) {
            return askForCustomerIDFromAdmin(branchAcronym);
        } else {
            return userID;
        }
    }
}
