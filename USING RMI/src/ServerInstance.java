import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerInstance {

    private String serverID;
    private String serverName;
    private int serverRegistryPort;
    private int serverUdpPort;
    public ServerInstance(String serverID)  throws Exception {
        this.serverID = serverID;
        switch (serverID) {
            case "ATW":
                serverName = Constant.SERVER_ATWATER;
                serverRegistryPort = Constant.ATWATER_REGISTRY_PORT;
                serverUdpPort = Constant.ATWATER_SERVER_PORT;
                break;
            case "VER":
                serverName = Constant.SERVER_VERDUN;
                serverRegistryPort = Constant.VERDUN_REGISTRY_PORT;
                serverUdpPort = Constant.VERDUN_SERVER_PORT;
                break;
            case "OUT":
                serverName = Constant.SERVER_OUTREMONT;
                serverRegistryPort = Constant.OUTREMONT_REGISTRY_PORT;
                serverUdpPort = Constant.OUTREMONT_SERVER_PORT;
                break;
        }

        ServerImplement remoteObject = new ServerImplement(serverID, this.serverName);
        MovieManagementInterface obj = (MovieManagementInterface) UnicastRemoteObject.exportObject(remoteObject,0);
        Registry registry = LocateRegistry.createRegistry(this.serverRegistryPort);
        registry.bind(Constant.MOVIE_MANAGEMENT_REGISTERED_NAME, obj);
        System.out.println(this.serverName + " Server is Up & Running");
        Logger.serverLog(serverID, " Server is Up & Running");
        this.addTestData(remoteObject);
        Runnable task = () -> {
            listenForRequest(remoteObject, this.serverUdpPort, this.serverName, serverID);
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    private void addTestData(ServerImplement remoteObject) throws RemoteException {
        switch (serverID) {
            case "ATW":
                remoteObject.addMovieSlot("ATWA100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("ATWM100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("ATWE100223", Constant.MOVIE_AVTAR, 20);
                break;
            case "VER":
                remoteObject.addMovieSlot("VERA100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("VERM100223", Constant.MOVIE_AVTAR, 20);
                 remoteObject.addMovieSlot("VERE100223", Constant.MOVIE_AVTAR, 20);

                break;
            case "OUT":
                remoteObject.addMovieSlot("OUTA100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("OUTM100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("OUTE100223", Constant.MOVIE_AVTAR, 20);
                break;
        }
    }

    private static void listenForRequest(ServerImplement obj, int serverUdpPort, String serverName, String serverID) {
        DatagramSocket aSocket = null;
        String sendingResult = "";
        try {
            aSocket = new DatagramSocket(serverUdpPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
            Logger.serverLog(serverID, " UDP Server Started at port " + aSocket.getLocalPort());
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());

                String[] parts = sentence.split(";");
                String method = parts[0];
                String customerID = parts[1];
                String eventType = parts[2];
                String eventID = parts[3];
                int numberOfTickets = Integer.parseInt(parts[4]);

                if (method.equalsIgnoreCase("removeMovieSlot")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", " ...");

                    String result = obj.bookMovieTickets(customerID,eventID, eventType,numberOfTickets);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("listMovieShowAvailability")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventType: " + eventType + " ", " ...");
                    String result = obj.listMovieShowAvailabilityUDP(eventType);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("bookMovieShow")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", " ...");
                    String result = obj.bookMovieTicketsAnotherServer(customerID, eventID, eventType, numberOfTickets);
                    sendingResult = result + ";";
                }else if(method.equalsIgnoreCase("listBookingSchedule")){


                    String result = obj.getBookingScheduleUDP(customerID);

                    sendingResult = result + ";";
                }
                else if (method.equalsIgnoreCase("cancelMovieShow")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", " ...");
                    String result = obj.cancelMovieTicketsUDP(customerID, eventID, eventType, numberOfTickets);
                    System.out.println("result: "+ result);
                    sendingResult = result + ";";
                }
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
                Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " eventID: " + eventID + " eventType: " + eventType + " ", sendingResult);
            }
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
