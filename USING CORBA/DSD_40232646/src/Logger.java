

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {


    public static void clientLog(String clientID, String action, String requestParams, String response) throws IOException {

        File file = new File(getFileName(clientID, Constant.LOG_TYPE_CLIENT));
        FileWriter fileWriter;
        if(file.exists())
        {
            fileWriter = new FileWriter(getFileName(clientID, Constant.LOG_TYPE_CLIENT), true);
        }
        else
        {

            fileWriter = new FileWriter(getFileName(clientID, Constant.LOG_TYPE_CLIENT), false);
        }
        //FileWriter fileWriter = new FileWriter(getFileName(clientID, Constant.LOG_TYPE_CLIENT), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " Client Action: " + action + " | RequestParameters: " + requestParams + " | Server Response: " + response);

        printWriter.close();
    }

    public static void clientLog(String clientID, String msg) throws IOException {
        File file = new File(getFileName(clientID, Constant.LOG_TYPE_CLIENT));
        FileWriter fileWriter;


        if(file.exists())
        {
            fileWriter = new FileWriter(getFileName(clientID, Constant.LOG_TYPE_CLIENT), true);
        }
        else
        {
            fileWriter = new FileWriter(getFileName(clientID, Constant.LOG_TYPE_CLIENT), false);
        }
//        if(file.exists())
//        {
//             fileWriter = new FileWriter(getFileName(clientID, Constant.LOG_TYPE_CLIENT), true);
//        }
//        else
//        {
//            file.mkdirs();
//            file.createTempFile(clientID,".txt");
//            System.out.println(getFileName(clientID, Constant.LOG_TYPE_CLIENT));
//            System.out.println(file.createNewFile());
//            fileWriter = new FileWriter(file, false);
//        }

        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    public static void serverLog(String serverID, String clientID, String requestType, String requestParams, String serverResponse) throws IOException {

        File file = new File(getFileName(serverID, Constant.LOG_TYPE_SERVER));
        FileWriter fileWriter;
        if (clientID.equals("null")) {
            clientID = "Admin";
        }
        if(file.exists())
        {
            fileWriter = new FileWriter(getFileName(serverID, Constant.LOG_TYPE_SERVER), true);
        }
        else
        {
            fileWriter = new FileWriter(getFileName(serverID, Constant.LOG_TYPE_SERVER), false);
        }
        //FileWriter fileWriter = new FileWriter(getFileName(serverID, Constant.LOG_TYPE_SERVER), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " ClientID: " + clientID + " | RequestType: " + requestType + " | RequestParameters: " + requestParams + " | ServerResponse: " + serverResponse);

        printWriter.close();
    }

    public static void serverLog(String serverID, String msg) throws IOException {

        File file = new File(getFileName(serverID, Constant.LOG_TYPE_SERVER));
        FileWriter fileWriter;
        if(file.exists())
        {
            fileWriter = new FileWriter(getFileName(serverID, Constant.LOG_TYPE_SERVER), true);
        }
        else
        {
            fileWriter = new FileWriter(getFileName(serverID, Constant.LOG_TYPE_SERVER), false);
        }
        //FileWriter fileWriter = new FileWriter(getFileName(serverID, Constant.LOG_TYPE_SERVER), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    public static void deleteALogFile(String ID) throws IOException {

        String fileName = getFileName(ID, Constant.LOG_TYPE_CLIENT);
        File file = new File(fileName);
        file.delete();
    }

    private static String getFileName(String ID, int logType) {
        final String dir = System.getProperty("user.dir");
        String fileName = dir;
        String path="E:\\Concordia\\Winter 2023\\DSD\\Assignments\\Assignment 1\\DSD_40232646\\src\\Logs";
        if (logType == Constant.LOG_TYPE_SERVER) {
            if (ID.equalsIgnoreCase("ATW")) {
                fileName=path+"\\Server\\ATWATER.txt";
                //fileName = dir + "\\Logs\\Server\\ATWATER.txt";
            } else if (ID.equalsIgnoreCase("VER")) {
                fileName=path+"\\Server\\VERDUN.txt";
                //fileName = dir + "\\Logs\\Server\\VERDUN.txt";
            } else if (ID.equalsIgnoreCase("OUT")) {
                fileName=path+"\\Server\\OUTREMONT.txt";
                //fileName = dir + "\\Logs\\Server\\OUTREMONT.txt";
            }
        } else {
            fileName=path+"\\Client\\" + ID + ".txt";
            //fileName = dir + "\\Logs\\Client\\" + ID + ".txt";
        }
        return fileName;
    }

    private static String getFormattedDate() {
        Date date = new Date();

        String strDateFormat = "yyyy-MM-dd hh:mm:ss a";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        return dateFormat.format(date);
    }

}
