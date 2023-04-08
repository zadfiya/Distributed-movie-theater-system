package Models;

import Assets.StringAssets;

import java.util.ArrayList;
import java.util.List;

public class MovieModel {
    private static final int ALREADY_EXIST = 0;
    public static final int SUCCESS = 1;
    public static final int HOUSE_FULL = -1;
    private String movieName;
    private String movieId;
    public int movieCapacity;
    private String movieDate;
    private String movieTime;
    private String movieServer;
    private List<String> registeredClientsList;

    private int bookedSeats;
    public MovieModel(String movieName, String movieId, int movieCapacity) {
        setMovieName(movieName);
        setMovieId(movieId);
        setMovieCapacity(movieCapacity);
        setMovieDate(findMovieDate());
        setMovieTime(findMovieTiming());
        setMovieServer(findMovieServer(movieId));
        registeredClientsList = new ArrayList<>();
    }
    public String getMovieDate() {
        return movieDate;
    }

    public int getBookedSeats() {
        return this.bookedSeats;
    }

    public void seatBooked(int var1) {
        this.bookedSeats += var1;
    }

    public void seatCanceled(int var1) {
        this.bookedSeats -= var1;
    }

    public void setBookedSeats(int var1) {
        this.bookedSeats = var1;
    }

    public void setMovieDate(String movieDate) {
        this.movieDate = movieDate;
    }

    public String getMovieTime() {
        return movieTime;
    }

    public void setMovieTime(String movieTime) {
        this.movieTime = movieTime;
    }

    public String getMovieServer() {
        return movieServer;
    }

    public void setMovieServer(String movieServer) {
        this.movieServer = movieServer;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public int getMovieCapacity() {
        return movieCapacity;
    }

    public void setMovieCapacity(int movieCapacity) {
        this.movieCapacity = movieCapacity;
    }

    public  String findMovieTiming(){
        char ch = movieId.charAt(3);
        switch (ch){
            case 'A':
                return StringAssets.AFTERNOON_SHOW;
            case 'E':
                return StringAssets.EVENING_SHOW;
            case 'M':
                return StringAssets.MORNING_SHOW;
        }
        return null;
    }
    public  String findMovieDate(){
        return movieId.substring(4,6)+"-"+movieId.substring(6,8)+"-20"+movieId.substring(8,10);
    }
    public static  String findMovieServer(String movieId) {
        String serverId = movieId.substring(0,3);
        switch (serverId) {
            case "ATW":
                return StringAssets.ATWATER_SERVER;
            case "VER":
                return StringAssets.VERDUN_SERVER;
            case "OUT":
                return StringAssets.OUTREMONT_SERVER;
            default:
                return null;
        }
    }



    public List<String> getRegisteredClients(){
        return registeredClientsList;
    }

    public void setRegisteredClients(List<String> clientIds){
        this.registeredClientsList = clientIds;
    }

    public int addRegisteredClientId(String clientId){
        if(!isHouseful()){
            if(registeredClientsList.contains(clientId)){
                return ALREADY_EXIST;
            }
            else {
                registeredClientsList.add(clientId);
                return SUCCESS;
            }

        }
        else{
            return HOUSE_FULL;
        }
    }

    public boolean removeRegisteredClientId(String clientId){
        return registeredClientsList.remove(clientId);
    }

    public boolean isHouseful() {
        return  getMovieCapacity() == registeredClientsList.size();
    }

    @Override
    public String toString() {
        return "Movie ID: "+getMovieId()+"\nMovie Time: "+getMovieTime()+"\nMovie Date: "+ getMovieDate()+"\nMovie Capacity: "+getMovieCapacity();
    }

    public int setRemainingCapacity(int qty){
        setMovieCapacity(getMovieCapacity()-qty);
        return getMovieCapacity();
    }
}
