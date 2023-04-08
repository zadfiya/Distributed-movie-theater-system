package server;

public class Server {
    public static void main(String[] args) throws Exception{
        Runnable atwater = () ->{
            try{
                ServerInst AtwaterServer = new ServerInst("ATW");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        };

        Runnable verdun = () ->{
            try{
                ServerInst AtwaterServer = new ServerInst("VER");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        };

        Runnable outremont = () ->{
            try{
                ServerInst AtwaterServer = new ServerInst("OUT");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        };
        Thread t1 = new Thread(atwater);
        t1.start();
        Thread t2 = new Thread(verdun);
        t2.start();
        Thread t3 = new Thread(outremont);
        t3.start();
    }
}
