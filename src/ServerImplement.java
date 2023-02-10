import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerImplement implements MovieManagementInterface{
    public static final int ATWATER_SERVER_PORT = 8888;
    public static final int VERDUN_SERVER_PORT = 7777;
    public static final int OUTREMONT_SERVER_PORT = 6666;
    public static final String THEATER_SERVER_ATWATER = "ATWATER";
    public static final String THEATER_SERVER_VERDUN = "VERDUN";
    public static final String THEATER_SERVER_OUTREMONT = "OUTREMONT";
    private String serverID;
    private String serverName;
    // HashMap<MovieName, HashMap <MovieID, booking Capacity>>
    private Map<String, Map<String, Integer>> allMovieShows;
    // HashMap<CustomerID, HashMap <MovieName, List<MovieID>>>
    private Map<String, Map<String, List<String>>> clientEvents;
    // HashMap<ClientID, Client>
    //private Map<String, ClientModel> serverClients;
    public ServerImplement(String serverID, String serverName) throws RemoteException {
        super();
        this.serverID = serverID;
        this.serverName = serverName;
        allMovieShows = new ConcurrentHashMap<>();
        allMovieShows.put(Constant.MOVIE_AVENGER, new ConcurrentHashMap<>());
        allMovieShows.put(Constant.MOVIE_AVTAR, new ConcurrentHashMap<>());
        allMovieShows.put(Constant.MOVIE_TITANIC, new ConcurrentHashMap<>());
        clientEvents = new ConcurrentHashMap<>();
        //serverClients = new ConcurrentHashMap<>();
//        addTestData();
    }
    @Override
    public String addMovieSlot(String movieID, String movieName, int bookingCapacity) throws RemoteException {
        String response;
        if(allMovieShows.get(movieName).containsKey(movieID))
        {
            allMovieShows.get(movieName).put(movieID,bookingCapacity);
            response ="Success: Movie Show " + movieID + " updated with number of booking seats " + bookingCapacity;
        }

        if(Constant.detectServer(movieID).equals(serverName))
        {
            Map<String, Integer> movieHashMap = allMovieShows.get(movieName);
            movieHashMap.put(movieID,bookingCapacity );
            allMovieShows.put(movieName, movieHashMap);
            response = "Success: Movie " + movieID + " added successfully";
        }
        else
        {
            response = "Failed: Cannot Add Event to servers other than " + serverName;
        }
        return response;
    }

    @Override
    public String removeMovieSlots(String movieID, String movieName) throws RemoteException {
        String response;
        if(Constant.detectServer(movieID).equals(serverName))
        {
            if(allMovieShows.get(movieName).containsKey(movieID))
            {
                //List<String> registeredClients = allMovieShows.get(movieName).get(movieID).getRegisteredClientIDs();
                allMovieShows.get(movieName).remove(movieID);
                //addCustomersToNextSameEvent(movieID, movieName, registeredClients);
                response = "Success: Movie Show Removed Successfully";
            }
            else
            {
                response = "Failed: Movie " + movieID + " Does Not Exist";
            }
        }
        else
        {
            response = "Failed: Cannot Remove Movie Show from servers other than " + serverName;
        }
        return response;
    }

    @Override
    public String listEventAvailability(String movieName) throws RemoteException {
        Map<String, Integer> movieShows = allMovieShows.get(movieName);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server " + movieName + ":\n");
        if (movieShows.size() == 0) {
            builder.append("No Events of Type " + movieName);
        } else {
            movieShows.entrySet().forEach(items->{
                builder.append(items.getKey() + " "+ items.getValue() + " || ");
            });

        }
        builder.append("\n=====================================\n");
        return builder.toString();
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException {
        String response = "Movie Show " + movieID +" Booked Successfully";;
//        if (!serverClients.containsKey(customerID)) {
//            addNewCustomerToClients(customerID);
//        }
        if(allMovieShows.get(movieName).containsKey(movieID))
        {
            if(Constant.detectServer(movieID).equals(serverName))
            {

                int seatsAvailable = allMovieShows.get(movieName).get(movieID);
                if(seatsAvailable-numberOfTickets>0)
                {
                    System.out.println(clientEvents.containsKey(customerID)+" customer contains");
                    if(clientEvents.containsKey(customerID))
                    {
                        System.out.println(clientEvents.get(customerID).containsKey(movieName)+" customer contains for specific movie");
                        if(clientEvents.get(customerID).containsKey(movieName))
                        {
                            System.out.println(!clientEvents.get(customerID).get(movieName).contains(movieID)+" customer movieID");
                            if(!clientEvents.get(customerID).get(movieName).contains(movieID))
                            {
                                clientEvents.get(customerID).get(movieName).add(movieID);
                                allMovieShows.get(movieName).put(movieID,allMovieShows.get(movieName).get(movieID)-numberOfTickets);
                                System.out.println(allMovieShows.entrySet()+" first print");
                                System.out.println(allMovieShows.get(movieName).entrySet()+" second print");
                            }
                            else
                            {
                                response ="Failed: Event " + movieID + " Already Booked";
                            }
                        }
                        else
                        {
                            List<String> temp = new ArrayList<>();
                            temp.add(movieID);
                            clientEvents.get(customerID).put(movieName,temp);
                            System.out.println(clientEvents.get(customerID).entrySet()+" customer events");
                        }
                    }
                    else
                    {
                        Map<String, List<String>> temp = new ConcurrentHashMap<>();
                        List<String> temp2 = new ArrayList<>();
                        temp2.add(movieID);
                        temp.put(movieName, temp2);
                        clientEvents.put(customerID, temp);
                        System.out.println(clientEvents.get(customerID).entrySet()+" customer events outer loop");
                    }
                }
                else
                {
                    response ="Failed: Movie Show " + movieID + " is Full";
                }
            }else
            {
                System.out.println(exceedWeeklyLimit(customerID, movieID.substring(4))+" week limit");
                if (!exceedWeeklyLimit(customerID, movieID.substring(4)))
                {
                    String serverResponse = sendUDPMessage(Constant.getServerPort(movieID.substring(0, 3)), "bookMovieShow", customerID, movieName, movieID,numberOfTickets);
                    System.out.println(serverResponse+" response");
                    if (serverResponse.startsWith("Success:")) {
                        if (clientEvents.get(customerID).containsKey(movieName)) {
                            clientEvents.get(customerID).get(movieName).add(movieID);
                        } else {
                            List<String> temp = new ArrayList<>();
                            temp.add(movieID);
                            clientEvents.get(customerID).put(movieName, temp);
                        }
                        System.out.println(clientEvents.get(customerID).entrySet());
                    }
                    else{
                        response = "Failed: You Cannot Book Event in Other Servers For This Week(Max Weekly Limit = 3)";
                    }
                }
            }
        }
        else {
            response = "Movie Show is not opened yet by Administration";
        }

        return response;
    }

    private boolean exceedWeeklyLimit(String customerID, String movieDate) {
        int limit = 0;
        for (int i = 0; i < 3; i++) {
            List<String> registeredIDs = new ArrayList<>();
            switch (i) {
                case 0:
                    if (clientEvents.get(customerID).containsKey(Constant.MOVIE_AVTAR)) {
                        registeredIDs = clientEvents.get(customerID).get(Constant.MOVIE_AVTAR);
                    }
                    break;
                case 1:
                    if (clientEvents.get(customerID).containsKey(Constant.MOVIE_AVENGER)) {
                        registeredIDs = clientEvents.get(customerID).get(Constant.MOVIE_AVENGER);
                    }
                    break;
                case 2:
                    if (clientEvents.get(customerID).containsKey(Constant.MOVIE_TITANIC)) {
                        registeredIDs = clientEvents.get(customerID).get(Constant.MOVIE_TITANIC);
                    }
                    break;
            }
            for (String movieID :
                    registeredIDs) {
                if (movieID.substring(6, 8).equals(movieDate.substring(2, 4)) && movieID.substring(8, 10).equals(movieDate.substring(4, 6))) {
                    int week1 = Integer.parseInt(movieID.substring(4, 6)) / 7;
                    int week2 = Integer.parseInt(movieDate.substring(0, 2)) / 7;
//                    int diff = Math.abs(week1 - week2);
                    if (week1 == week2) {
                        limit++;
                    }
                }
                if (limit == 3)
                    return true;
            }
        }
        return false;
    }

    private String sendUDPMessage(int serverPort, String method, String customerID, String movieName, String movieID, int numberOfTickets) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerID + ";" + movieName + ";" + movieID+";"+numberOfTickets;

        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

        return result;
    }

    @Override
    public String getBookingSchedule(String customerID) throws RemoteException {
        String response;
//        if (!serverClients.containsKey(customerID)) {
//            addNewCustomerToClients(customerID);
//            response = "Booking Schedule Empty For " + customerID;
//            try {
//                Logger.serverLog(serverID, customerID, " RMI getBookingSchedule ", "null", response);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return response;
//        }
        Map<String, List<String>> movieShows = clientEvents.get(customerID);
        if (movieShows.size() == 0) {
            response = "Booking Schedule Empty For " + customerID;
//            try {
//                Logger.serverLog(serverID, customerID, " RMI getBookingSchedule ", "null", response);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            return response;
        }
        StringBuilder builder = new StringBuilder();
        for (String eventType :
                movieShows.keySet()) {
            builder.append(eventType + ":\n");
            for (String eventID :
                    movieShows.get(eventType)) {
                builder.append(eventID + " ||");
            }
            builder.append("\n=====================================\n");
        }
        response = builder.toString();
//        try {
//            Logger.serverLog(serverID, customerID, " RMI getBookingSchedule ", "null", response);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return response;

    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException {
        String response;
        if (Constant.detectServer(movieID).equals(serverName)) {
            if (customerID.substring(0, 3).equals(serverID)) {
//                if (!serverClients.containsKey(customerID)) {
//                    addNewCustomerToClients(customerID);
//                }
                //else{
                if (clientEvents.get(customerID).get(movieName).remove(movieID)) {
                    int count = allMovieShows.get(movieName).get(movieID);
                    allMovieShows.get(movieName).put(movieID,count+numberOfTickets);
                    response = "Success: Event " + movieID + " Canceled for " + customerID;
                }
                else
                {
                    response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
                }
            }
            else
            {
                if (clientEvents.get(customerID).get(movieName).contains(movieID)) {
                    clientEvents.get(customerID).get(movieName).contains(movieID);
                    int count = allMovieShows.get(movieName).get(movieID);
                    allMovieShows.get(movieName).put(movieID,count+numberOfTickets);
                    response = "Success: Event " + movieID + " Canceled for " + customerID;
                }
                else
                {
                    response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
                }
            }
        }
        else
        {
            if (clientEvents.get(customerID).get(movieName).remove(movieID)) {
                return sendUDPMessage(Constant.getServerPort(movieID.substring(0, 3)), "cancelMovieShow", customerID, movieName, movieID,numberOfTickets);
            }
            response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
        }


        return response;
    }

    public String removeEventUDP(String oldMovieID, String movieName, String customerID) throws RemoteException {
//        if (!serverClients.containsKey(customerID)) {
//            addNewCustomerToClients(customerID);
//            return "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
//        } else {
            if (clientEvents.get(customerID).get(movieName).remove(oldMovieID)) {
                return "Success: Movie " + oldMovieID + " Was Removed from " + customerID + " Schedule";
            } else {
                return "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
            }
       // }
    }
}
