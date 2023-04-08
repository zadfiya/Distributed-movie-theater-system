package com.web.service.Implementation;

import Assets.StringAssets;
import Logger.Logger;
import Models.ClientModel;
import Models.MovieModel;
import com.web.service.WebInterface;


import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebService(endpointInterface = "com.web.service.WebInterface")

public class MovieManager implements WebInterface {
    public static final int ATWATER_SERVER_PORT = 8888;
    public static final int VERDUN_SERVER_PORT = 7777;
    public static final int OUTREMONT_SERVER_PORT = 6666;
    private Map<String, Map<String, MovieModel>> moviesEvents;
    private Map<String, Map<String, List<String>>> clientMovies;
    private Map<String, ClientModel> serverClients;
    private Map<String, Integer> movieBookings;
    public static final int MINVALUE = Integer.MIN_VALUE;
    private String serverId;
    private String serverName;

    public MovieManager(){}
    public MovieManager(String serverId, String serverName) {

        super();
        this.serverId = serverId;
        this.serverName = serverName;

        moviesEvents = new ConcurrentHashMap<>();
        moviesEvents.put(StringAssets.AVATAR_MOVIE, new ConcurrentHashMap<>());
        moviesEvents.put(StringAssets.AVENGERS_MOVIE, new ConcurrentHashMap<>());
        moviesEvents.put(StringAssets.TITANIC_MOVIE, new ConcurrentHashMap<>());
        clientMovies = new ConcurrentHashMap<>();
        serverClients = new ConcurrentHashMap<>();
        movieBookings = new ConcurrentHashMap<>();

    }


    private static int getServerPort(String serverBranch) {
        if (serverBranch.equalsIgnoreCase("ATW")) {
            return ATWATER_SERVER_PORT;
        } else if (serverBranch.equalsIgnoreCase("VER")) {
            return VERDUN_SERVER_PORT;
        } else if (serverBranch.equalsIgnoreCase("OUT")) {
            return OUTREMONT_SERVER_PORT;
        }
        return 1;
    }

    @Override
    @SOAPBinding(style = SOAPBinding.Style.RPC)

    public String addMovieSlots(String movieId, String movieName, int bookingCapacity) {
        String response;
        Date date = new Date();
        String strDateFormat = "yyMMdd";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        String dateToday = dateFormat.format(date);
        int today = Integer.parseInt(dateToday);
        String dateOfMovie = movieId.substring(8, 10) + "" + movieId.substring(6, 8) + "" + movieId.substring(4, 6);
        int movieDate = Integer.parseInt(dateOfMovie);
        if (movieDate - today > 7 || movieDate - today < 0) {
            response = "FAILURE: Movie Slot Can only be added for a week from current date";
            try {
                Logger.serverLog(movieId, "null", " Web Service addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        if (isSameServerMovieSlot(movieId)) {
            if (movieSlotExists(movieName, movieId)) {
                if (moviesEvents.get(movieName).get(movieId).getMovieCapacity() <= bookingCapacity) {
                    moviesEvents.get(movieName).get(movieId).setMovieCapacity(bookingCapacity);
                    response = "SUCCESS: Movie" + movieId + " New Capacity is " + bookingCapacity;
                    try {
                        Logger.serverLog(movieId, "null", " Web Service addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    response = "FAILURE: Movie Capacity already more than " + bookingCapacity;
                    try {
                        Logger.serverLog(movieId, "null", " Web Service addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return response;
            } else {
                MovieModel movieModel = new MovieModel(movieName, movieId, bookingCapacity);
                Map<String, MovieModel> moviesHashMap = moviesEvents.get(movieName);
                moviesHashMap.put(movieId, movieModel);
                moviesEvents.put(movieName, moviesHashMap);
                response = "SUCCESS: Movie " + movieId + " added successfully.";
                try {
                    Logger.serverLog(movieId, "null", " Web Service addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            response = "FAILURE: Cannot add Movie to other Servers";
            try {
                Logger.serverLog(movieId, "null", " Web Service addMovie ", " movieId: " + movieId + " movieName: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private synchronized boolean movieSlotExists(String movieName, String movieId) {
        return moviesEvents.get(movieName).containsKey(movieId);
    }

    private synchronized boolean isSameServerMovieSlot(String movieId) {
        return MovieModel.findMovieServer(movieId).equals(serverName);
    }

    @Override
    @SOAPBinding(style = SOAPBinding.Style.RPC)

    public String removeMovieSlots(String movieId, String movieName) {
        String response;
        if (isSameServerMovieSlot(movieId)) {
            if (movieSlotExists(movieName, movieId)) {
                List<String> clientsList = moviesEvents.get(movieName).get(movieId).getRegisteredClients();
                moviesEvents.get(movieName).remove(movieId);
                addCustomersToNextMovieSlot(movieId, movieName, clientsList);
                response = "SUCCESS: Movie Slot Removed";
                try {
                    Logger.serverLog(movieId, "null", " Web Service removeMovieSlots ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                response = "FAILURE: Movie Slot with Id: " + movieId + " does not exist";
                try {
                    Logger.serverLog(movieId, "null", " Web Service removeMovieSlots ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            response = "FAILURE: Cannot Remove Event from servers other than " + serverName;
            try {
                Logger.serverLog(movieId, "null", " Web Service removeMovieSlots ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;

    }

    private void addCustomersToNextMovieSlot(String movieId, String movieName, List<String> clientsList) {
        String response;
        for (String customerID : clientsList) {
            if (customerID.substring(0, 3).equals(movieId.substring(0, 3))) {
                int tix = movieBookings.get(customerID + movieId + movieName);
                removeMovieIfAlreadyExists(customerID, movieName, movieId);
                String nextAvailableSlot = getNextAvailableSlot(moviesEvents.get(movieName).keySet(), movieName, movieId);
                if (nextAvailableSlot.equals("FAILURE")) {
                    response = "Getting next available slot" + nextAvailableSlot;
                    try {
                        Logger.serverLog(serverId, customerID, "addCustomersToNextMovieSlot", "old movieId: " + movieId + "movieName: " + movieName + " ", response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                } else {
                    bookMoviesTickets(customerID, nextAvailableSlot, movieName, tix);
                }
            } else {
                sendUDPMessage(getServerPort(customerID.substring(0, 3)), "removeEvent", customerID, movieName, movieId, MINVALUE);
            }
        }
    }

    @Override
    @SOAPBinding(style = SOAPBinding.Style.RPC)

    public String listMovieShowsAvailability(String movieName) {

        String response;
        Map<String, MovieModel> slots = moviesEvents.get(movieName);
        StringBuffer sb = new StringBuffer();
        sb.append(serverName + " Server " + movieName + ":\n");
        if (slots.size() == 0) {
            sb.append("No Movie Slots for Movie: " + movieName);
        } else {
            for (MovieModel movies : slots.values()) {
                sb.append(movies.toString() + " || ");
            }
            sb.append("\n=====================================\n");
        }
        String server1, server2;
        if (serverId.equals("ATW")) {
            server1 = sendUDPMessage(VERDUN_SERVER_PORT, "listMovieAvailability", "null", movieName, "null", MINVALUE);
            server2 = sendUDPMessage(OUTREMONT_SERVER_PORT, "listMovieAvailability", "null", movieName, "null", MINVALUE);
        } else if (serverId.equals("VER")) {
            server1 = sendUDPMessage(ATWATER_SERVER_PORT, "listMovieAvailability", "null", movieName, "null", MINVALUE);
            server2 = sendUDPMessage(OUTREMONT_SERVER_PORT, "listMovieAvailability", "null", movieName, "null", MINVALUE);
        } else {
            server1 = sendUDPMessage(ATWATER_SERVER_PORT, "listMovieAvailability", "null", movieName, "null", MINVALUE);
            server2 = sendUDPMessage(VERDUN_SERVER_PORT, "listMovieAvailability", "null", movieName, "null", MINVALUE);
        }
        sb.append(server1).append(server2);

        response = sb.toString();
        try {
            Logger.serverLog(serverId, "null", " Web Service listMovieShowsAvailability ", " movieName: " + movieName + " ", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    @SOAPBinding(style = SOAPBinding.Style.RPC)

    public String bookMoviesTickets(String customerId, String movieId, String movieName, int numberOfTickets) {
        String response;
        checkIfClientExists(customerId);
        boolean bookingAgain = false;
        if (isSameServerMovieSlot(movieId)) {
            MovieModel bookedEvent = moviesEvents.get(movieName).get(movieId);


            if (bookedEvent != null && numberOfTickets <= bookedEvent.getMovieCapacity()) {
                if (clientMovies.containsKey(customerId)) {
                    if (clientMovies.get(customerId).containsKey(movieName)) {
                        if (clientHasSlot(customerId, movieName, movieId)) {
                            bookingAgain = true;
                        }

                        else {
                            clientMovies.get(customerId).get(movieName).add(movieId);
                        }
                    } else if (isCustomerOfThisServer(customerId)) {
                        addMovieNameAndMovie(customerId, movieName, movieId);
                    }
                    else {
                        if (clientHasSlotsOfSameTime(customerId, movieName, movieId)) {
                            response = "FAILURE: You have already booked a Movie  at different server for same time";
                            try {
                                Logger.serverLog(serverId, customerId, " Web Service bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return response;
                        }
                    }

                } else {
                    if (!isCustomerOfThisServer(customerId)) {
                        if(clientHasSlotsOfSameTime(customerId,movieName,movieId)){
                            response = "FAILURE: You have already booked Movie for same time";
                            try {
                                Logger.serverLog(serverId, customerId, " Web Service bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return response;
                        }else{
                            addCustomerAndMovie(customerId, movieName, movieId);}
                    }
                }
                if (bookingAgain) {
                    int oldQty = movieBookings.get(customerId + movieId + movieName);
                    response = "SUCCESS: Movie " + movieId + " Booked " + numberOfTickets + " more tickets, Total Tickets = " + (numberOfTickets + oldQty);
                    movieBookings.put(customerId + movieId + movieName, numberOfTickets + oldQty);
                    moviesEvents.get(movieName).get(movieId).setMovieCapacity(moviesEvents.get(movieName).get(movieId).getMovieCapacity() - (oldQty + numberOfTickets));

                    return response;
                } else if (moviesEvents.get(movieName).get(movieId).addRegisteredClientId(customerId) == MovieModel.SUCCESS) {
                    response = "SUCCESS: Movie " + movieId + " Booked Successfully" + " For " + numberOfTickets + " Tickets";
                    moviesEvents.get(movieName).get(movieId).setMovieCapacity(moviesEvents.get(movieName).get(movieId).getMovieCapacity() - numberOfTickets);
                    movieBookings.put(customerId + movieId + movieName, numberOfTickets);
                } else if (moviesEvents.get(movieName).get(movieId).addRegisteredClientId(customerId) == MovieModel.HOUSE_FULL) {
                    response = "FAILURE: Movie " + movieId + " Does not have " + numberOfTickets + " Tickets available!";
                } else {
                    response = "FAILURE: Cannot Add You To Event " + movieId;
                }
                try {
                    Logger.serverLog(serverId, customerId, " Web Service bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                response = "FAILURE: Movie " + movieId + " Does not have " + numberOfTickets + " Tickets available!";
                try {
                    Logger.serverLog(serverId, customerId, " Web Service bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
        } else {
            if(clientHasSlotsOfSameTime(customerId,movieName,movieId)){
                response = "FAILURE: You have already booked a Movie  at different server for same time";
                try {
                    Logger.serverLog(serverId, customerId, " Web Service bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
            if (clientHasSlot(customerId, movieName, movieId)) {
                int oldQty = movieBookings.get(customerId + movieId + movieName);
                response = "SUCCESS: Movie " + movieId + " Booked " + numberOfTickets + " more tickets, Total Tickets = " + (numberOfTickets + oldQty);
                movieBookings.put(customerId + movieId + movieName, numberOfTickets + oldQty);
                moviesEvents.get(movieName).get(movieId).setMovieCapacity(moviesEvents.get(movieName).get(movieId).getMovieCapacity() - (oldQty + numberOfTickets));
                return response;
            }
            if (!exceedWeeklyLimit(customerId)) {
                String serverResponse = sendUDPMessage(getServerPort(movieId.substring(0, 3)), "bookMovie", customerId, movieName, movieId, numberOfTickets);
                if (serverResponse.startsWith("SUCCESS:")) {
                    if (clientMovies.get(customerId).containsKey(movieName)) {
                        clientMovies.get(customerId).get(movieName).add(movieId);
                    } else {
                        List<String> temp = new ArrayList<>();
                        temp.add(movieId);
                        clientMovies.get(customerId).put(movieName, temp);
                    }
                }
                try {
                    Logger.serverLog(serverId, customerId, " Web Service bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                movieBookings.put(customerId + movieId + movieName, numberOfTickets);


                System.out.println(movieBookings);
                return serverResponse;
            } else {
                response = "FAILURE: Unable to Book Movie For This Week In Another Servers(Max Weekly Limit = 3)";
                try {
                    Logger.serverLog(serverId, customerId, " Web Service bookMovieTicket ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
    }

    private synchronized void addCustomerAndMovie(String customerId, String movieName, String movieId) {
        Map<String, List<String>> temp = new ConcurrentHashMap<>();
        List<String> temp2 = new ArrayList<>();
        temp2.add(movieId);
        temp.put(movieName, temp2);
        clientMovies.put(customerId, temp);
    }

    private synchronized void addMovieNameAndMovie(String customerId, String movieName, String movieId) {
        List<String> temp = new ArrayList<>();
        temp.add(movieId);
        clientMovies.get(customerId).put(movieName, temp);
    }

    private boolean isCustomerOfThisServer(String customerId) {
        return customerId.substring(0, 3).equals(serverId);
    }

    private synchronized boolean clientHasSlot(String customerId, String movieName, String movieId) {
        if (clientMovies.get(customerId).containsKey(movieName)) {
            return clientMovies.get(customerId).get(movieName).contains(movieId);
        } else {
            return false;
        }
    }

    private synchronized boolean clientHasSlotsOfSameTime(String customerId, String movieName, String movieId) {
        if (!movieBookings.isEmpty()) {
            List<String> li = new ArrayList<>();
            for (String i: movieBookings.keySet()) {
                li.add(i);
            }
            for(int i =0;i< li.size();i++){
                if(li.get(i).contains(customerId) && li.get(i).contains(movieId.substring(3,10)) && !li.get(i).equals(customerId+movieId+movieName)){
                    return true;
                }
            }

        }return false;
    }


    private synchronized boolean checkIfClientExists(String customerId) {
        if(!serverClients.containsKey(customerId)){
            addNewCustomerToClients(customerId);
            return false;
        }else{
            return true;
        }
    }
    private String getNextAvailableSlot(Set<String> keySet, String movieName, String movieId) {
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(movieId);
        Collections.sort(sortedIDs, new Comparator<String>() {
            @Override
            public int compare(String ID1, String ID2) {
                Integer timeSlot1 = 0;
                switch (ID1.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot1 = 1;
                        break;
                    case "A":
                        timeSlot1 = 2;
                        break;
                    case "E":
                        timeSlot1 = 3;
                        break;
                }
                Integer timeSlot2 = 0;
                switch (ID2.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot2 = 1;
                        break;
                    case "A":
                        timeSlot2 = 2;
                        break;
                    case "E":
                        timeSlot2 = 3;
                        break;
                }
                Integer date1 = Integer.parseInt(ID1.substring(8, 10) + ID1.substring(6, 8) + ID1.substring(4, 6));
                Integer date2 = Integer.parseInt(ID2.substring(8, 10) + ID2.substring(6, 8) + ID2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeSlotCompare = timeSlot1.compareTo(timeSlot2);
                if (dateCompare == 0) {
                    return ((timeSlotCompare == 0) ? dateCompare : timeSlotCompare);
                } else {
                    return dateCompare;
                }
            }
        });
        int index = sortedIDs.indexOf(movieId) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!moviesEvents.get(movieName).get(sortedIDs.get(i)).isHouseful()) {
                return sortedIDs.get(i);
            }
        }
        return "FAILURE";
    }

    @Override
    @SOAPBinding(style = SOAPBinding.Style.RPC)

    public String getBookingSchedule(String customerId) {
        String response;
        if(!checkIfClientExists(customerId)){
            response = "Booking Schedule Empty For " + customerId;
            try {
                Logger.serverLog(serverId, customerId, " Web Service getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        Map<String, List<String>> movies = clientMovies.get(customerId);
        if (movies.size() == 0) {
            response = "Booking Schedule Empty For " + customerId;
            try {
                Logger.serverLog(serverId, customerId, " Web Service getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        StringBuffer builder = new StringBuffer();
        for (String movieNames :
                movies.keySet()) {
            builder.append(movieNames + ":\n");
            for (String movieId :
                    movies.get(movieNames)) {

                builder.append(movieId).append("\t").append(movieBookings.get(customerId+movieId+movieNames)).append("\n");
            }
            builder.append("\n=====================================\n");
        }
        response = builder.toString();
        try {
            Logger.serverLog(serverId, customerId, " Web Service getBookingSchedule ", "null", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    @SOAPBinding(style = SOAPBinding.Style.RPC)

    public String cancelMovieTickets(String customerId, String movieId, String movieName, int numberOfTickets) {
        String response;
        int qty = movieBookings.get(customerId + movieId + movieName);
        if (isSameServerMovieSlot(movieId)) {
            if (isCustomerOfThisServer(customerId)) {
                if (!checkIfClientExists(customerId)) {
                    response = "FAILURE: You " + customerId + " Have not booked " + movieId;
                    try {
                        Logger.serverLog(serverId, customerId, " Web Service cancelMovieTickets ", " movieId: " + movieId + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (numberOfTickets > qty) {
                    response = "FAILURE: You don't have " + numberOfTickets + " Tickets booked.";
                } else if (numberOfTickets < qty) {
                    movieBookings.put(customerId + movieId + movieName, qty - numberOfTickets);
                    response = "SUCCESS: " + numberOfTickets + " Movie Tickets cancelled for " + customerId;
                    moviesEvents.get(movieName).get(movieId).setMovieCapacity(moviesEvents.get(movieName).get(movieId).getMovieCapacity() + numberOfTickets);
                } else if (removeMovieIfAlreadyExists(customerId,movieName,movieId)) {
                    moviesEvents.get(movieName).get(movieId).removeRegisteredClientId(customerId);
                    movieBookings.remove(customerId+movieId+movieName);
                    clientMovies.get(customerId).get(movieName).remove(movieId);
                    moviesEvents.get(movieName).get(movieId).setMovieCapacity(moviesEvents.get(movieName).get(movieId).getMovieCapacity() + numberOfTickets);

                    response = "SUCCESS: MOVIE " + movieId + " Canceled for " + customerId;
                    try {
                        Logger.serverLog(serverId, customerId, " Web Service cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    response = "FAILURE: You " + customerId + " Are Not Registered in " + movieId;
                    try {
                        Logger.serverLog(serverId, customerId, " Web Service cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                if (moviesEvents.get(movieName).get(movieId).removeRegisteredClientId(customerId)) {
                    movieBookings.remove(customerId+movieId+movieName);
                    moviesEvents.get(movieName).get(movieId).setMovieCapacity(moviesEvents.get(movieName).get(movieId).getMovieCapacity() + numberOfTickets);

                    response = "SUCCESS: Movie " + movieId + " Cancelled for " + customerId;
                    try {
                        Logger.serverLog(serverId, customerId, " Web Service cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    response = "FAILURE: You " + customerId + " Are Not Registered in " + movieId;
                    try {
                        Logger.serverLog(serverId, customerId, " Web Service cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response;
        }else{
            if(isCustomerOfThisServer(customerId)) {
                if (checkIfClientExists(customerId)) {
                    if (removeMovieIfAlreadyExists(customerId, movieName, movieId)) {
                        response = sendUDPMessage(getServerPort(movieId.substring(0, 3)), "cancelMovie", customerId, movieName, movieId, numberOfTickets);
                        try {
                            Logger.serverLog(serverId, customerId, " Web Service cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return response;
                    }
                }
            }

            response =  "FAILURE: You " + customerId + " Are Not Registered in " + movieId;
            try{
                Logger.serverLog(serverId, customerId, " Web Service cancelMovieTickets ", " movieID: " + movieId + " movieName: " + movieName + " ", response);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return response;
        }
    }

    @Override
    @SOAPBinding(style = SOAPBinding.Style.RPC)

    public String exchangeTickets(String customerID, String old_movieName, String movieID, String new_movieID, String new_movieName, int numberOfTickets) {
        String response;
        String resCancel="",resBook="";
        if(!checkIfClientExists(customerID)){
            response = "FAILURE: You "+ customerID+" Are not Registered in "+movieID;
            try{
                Logger.serverLog(serverId,customerID," Web Service Exchange Tickets", "oldMovieId: "+movieID+" oldMovieName: "+old_movieName+" newMovieId: "+new_movieID+" newMovieName "+ new_movieName+" numberOfTickets: "+numberOfTickets,response);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return response;
        }else {
            if (clientHasSlot(customerID,old_movieName,movieID)) {
                synchronized (this) {
                    resCancel = cancelMovieTickets(customerID, movieID, old_movieName, numberOfTickets);
                    if (resCancel.startsWith("SUCCESS:")) {
                        resBook = bookMoviesTickets(customerID, new_movieID, new_movieName, numberOfTickets);
                    } else {
                        resBook = bookMoviesTickets(customerID, new_movieID, new_movieName, numberOfTickets);
                        if (resBook.startsWith("SUCCESS:")) {
                            resCancel = cancelMovieTickets(customerID, movieID, old_movieName, numberOfTickets);
                        }
                    }

                }
                if (resBook.startsWith("SUCCESS:") && resCancel.startsWith("SUCCESS")) {
                    response = "SUCCESS: " + numberOfTickets + " Movie Tickets " + movieID + " Exchanged with " + new_movieID;
                } else if (resBook.startsWith("FAILURE:") && resCancel.startsWith("SUCCESS:")) {
                    String res = bookMoviesTickets(customerID, movieID, old_movieName, numberOfTickets);
                    response = "FAILURE: New Movie " + new_movieID + " could not be booked due to reason" + resBook + " So Old Movie Booking is Re-booked: " + res;
                } else if (resBook.startsWith("SUCCESS:") && resCancel.startsWith("FAILED:")) {
                    String res = cancelMovieTickets(customerID, new_movieID, new_movieName, numberOfTickets);
                    response = "FAILURE: Old Movie " + new_movieID + " could not be cancelled due to reason" + resCancel + " So New Movie Booking is Cancelled: " + res;
                } else {
                    response = "FAILURE: Cannot Exchange Movie Tickets: Due to reasons: " + resBook + "/n" + resCancel;
                }


            }else{
                if(!clientHasSlot(customerID,old_movieName,movieID)){
                    response = "FAILURE: You are not registered in "+old_movieName+" with movieId "+movieID;
                }
                else if(moviesEvents.get(new_movieName).get(new_movieID).getMovieCapacity() < numberOfTickets){
                    response = "New Booking Server Cannot facilitate "+numberOfTickets+" Tickets. Less Capacity Available";
                }else{
                    response = "Cannot Exchange Tickets";
                }

            }
            try {
                Logger.serverLog(serverId, customerID, " Web Service Exchange Tickets", "oldMovieId: " + movieID + " oldMovieName: " + old_movieName + " newMovieId: " + new_movieID + " newMovieName " + new_movieName + " numberOfTickets: " + numberOfTickets,response);
            }catch(Exception e){
                e.printStackTrace();
            }
            return response;
        }
    }
    private boolean exceedWeeklyLimit(String movieId){
        int count=0;
        for(String index: clientMovies.get(movieId).keySet()){
            for(String mIndex : clientMovies.get(movieId).get(index))
            {
                if(!mIndex.substring(0, 3).equals(movieId.substring(0, 3))){
                    count++;
                    if(count>=3){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private String sendUDPMessage(int serverPort, String method, String customerId, String movieName, String  movieId, Integer value) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerId + ";" + movieName + ";" + movieId+";"+value;
        try {
            Logger.serverLog(serverId, customerId, " UDP request sent " + method + " ", " movieId: " + movieId + " movieName: " + movieName + " ", " ... ");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            Logger.serverLog(serverId, customerId, " UDP reply received" + method + " ", " movieId: " + movieId + " movieName: " + movieName + " ", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    public void addNewCustomerToClients(String customerId) {
        ClientModel newCustomer = new ClientModel(customerId);
        serverClients.put(newCustomer.getClientId(), newCustomer);
        clientMovies.put(newCustomer.getClientId(), new ConcurrentHashMap<>());
    }

    public String removeMovieUDP(String oldMovieId, String movieName, String customerId) {
        if (!checkIfClientExists(customerId)){
            return "FAILURE: You " + customerId + " Are Not Registered in " + oldMovieId;
        } else {
            if (removeMovieIfAlreadyExists(customerId,movieName,oldMovieId)) {
                return "SUCCESS: Event " + oldMovieId + " Was Removed from " + customerId + " Schedule";
            } else {
                return "FAILURE: You " + customerId + " Are Not Registered in " + oldMovieId;
            }
        }
    }

    private synchronized boolean removeMovieIfAlreadyExists(String customerId, String movieName, String movieId){
        if(clientMovies.get(customerId).containsKey(movieName)){
            return clientMovies.get(customerId).get(movieName).remove(movieId);
        }
        else{
            return false;
        }
    }
    public String listMovieAvailabilityUDP(String movieName) {
        Map<String, MovieModel> movies = moviesEvents.get(movieName);
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("\n"+serverName + " Server " + movieName + ":\n");
        if (movies.size() == 0) {
            builder.append("No Events of Type " + movieName);
        } else {
            for (MovieModel movie :
                    movies.values()) {
                builder.append(movie.toString() + " || \n");
            }
        }
        builder.append("\n=====================================\n");
        return builder.toString();
    }
}
