package server;

import Assets.StringAssets;
import Logger.Logger;
import com.web.service.Implementation.MovieManager;


import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerInst {
    private String serverId,serverName;
    private String serverEndpoint;
    private  int serverUDPPort;
    public ServerInst(String serverId   ) throws Exception {
        this.serverId = serverId;
        if (serverId.equals("ATW")) {
            serverName = StringAssets.ATWATER_SERVER;
            serverUDPPort = MovieManager.ATWATER_SERVER_PORT;
            serverEndpoint = "http://localhost:8080/atwater";
        } else if (serverId.equals("VER")) {
            serverName = StringAssets.VERDUN_SERVER;
            serverUDPPort = MovieManager.VERDUN_SERVER_PORT;
            serverEndpoint = "http://localhost:8080/verdun";
        } else if (serverId.equals("OUT")) {
            serverName = StringAssets.OUTREMONT_SERVER;
            serverUDPPort = MovieManager.OUTREMONT_SERVER_PORT;
            serverEndpoint = "http://localhost:8080/outremont";
        }

        try{

            System.out.println(serverName+" Server is Up and Running");
            Logger.serverLog(serverId,"Server is Up and Running");
            MovieManager service = new MovieManager(serverId,serverName);
            Endpoint endpoint = Endpoint.publish(serverEndpoint,service);
            Runnable task = () -> {
                listenForRequest(service,serverUDPPort,serverName,serverId);
            };
            Thread thread = new Thread(task);
            thread.start();

        }
        catch(Exception e){
            e.printStackTrace();
            Logger.serverLog(serverId,"Exception: "+e);
        }
        
        //Logger.serverLog(serverId," Server Shutting down");
    }

    private static void listenForRequest(MovieManager obj, int serverUDPPort,String serverName, String serverId){
        DatagramSocket aSocket = null;
        String sendingResult = "";
        try{
            aSocket = new DatagramSocket(serverUDPPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName+" UPD Server Started at PORT: "+aSocket.getLocalPort());
            Logger.serverLog(serverId, " UDP Server Started at port "+aSocket.getLocalPort());

            while(true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());
                String[] parts = sentence.split(";");
                String method = parts[0];
                String customerID = parts[1];
                String movieName = parts[2];
                String movieId = parts[3];
                Integer qTickets = Integer.valueOf(parts[4]);

                if (method.equalsIgnoreCase("removeMovie")) {
                    Logger.serverLog(serverId, customerID, " UDP request received " + method + " ", " movieName: " + movieName + " movieId: " + movieId + " number of tickets: "+ qTickets+  " ", " ...");
                    String result = obj.removeMovieUDP(movieId, movieName, customerID);
                    sendingResult = result + ";";}
                else if (method.equalsIgnoreCase("listMovieAvailability")) {
                    Logger.serverLog(serverId, customerID, " UDP request received " + method + " ", " movieName: " + movieName + " movieId: " + movieId +" number of tickets: "+ qTickets+  " ", " ...");
                    String result = obj.listMovieAvailabilityUDP(movieName);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("bookMovie")) {
                    Logger.serverLog(serverId, customerID, " UDP request received " + method + " ", " movieName: " + movieName + " movieId: " + movieId + " number of tickets: "+ qTickets+ " ", " ...");
                    String result = obj.bookMoviesTickets(customerID, movieId, movieName,qTickets);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("cancelMovie")) {
                    Logger.serverLog(serverId, customerID, " UDP request received " + method + " ", " movieId: " + movieId + " movieName: " + movieName +" number of tickets: "+ qTickets+  " ", " ...");
                    String result = obj.cancelMovieTickets(customerID, movieId, movieName,qTickets);
                    sendingResult = result + ";";
                }
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
                Logger.serverLog(serverId, customerID, " UDP reply sent " + method + " ", " movieId: " + movieId + " movieName: " + movieName + " ", sendingResult);

            }
        } catch (SocketException e) {
            System.err.println("SocketException: " + e);
            e.printStackTrace(System.out);
        }
        catch(IOException e){
            System.err.println("Exception: " + e);
            e.printStackTrace(System.out);
        }
        finally {
            if(aSocket != null){
                aSocket.close();
            }
        }

    }
}
