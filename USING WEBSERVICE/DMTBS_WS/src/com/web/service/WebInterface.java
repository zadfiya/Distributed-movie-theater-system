package com.web.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WebInterface {

    String addMovieSlots(String movieId, String movieName, int bookingCapacity);

    String removeMovieSlots(String movieId, String movieName);

    String listMovieShowsAvailability(String movieName);

    String bookMoviesTickets(String customerId, String movieId, String movieName, int numberOfTickets);

    String getBookingSchedule(String customerId);

    String cancelMovieTickets(String customerId, String movieId, String movieName, int numberOfTickets);

    String exchangeTickets(String customerID, String old_movieName, String movieID, String new_movieID, String new_movieName, int numberOfTickets);

}
