public class Server {
    public Server() {}
    public static void main(String args[])
    {
        try
        {

            ServerInstance atwaterServer = new ServerInstance("ATW");
            ServerInstance verdunServer = new ServerInstance("VER");
            ServerInstance outremontServer = new ServerInstance("OUT");
        }

        catch(Exception e)
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
