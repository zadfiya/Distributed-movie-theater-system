package ServerObjectInterfaceApp;


/**
* ServerObjectInterfaceApp/ServerObjectInterfaceOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ServerObjectInterface.idl
* Friday, February 24, 2023 7:40:44 o'clock PM EST
*/

public interface ServerObjectInterfaceOperations 
{

  /**
          * Only for admin
          */
  String addMovieSlot (String movieID, String movieName, int bookingCapacity);
  String removeMovieSlots (String movieID, String movieName);
  String listMovieShowAvailability (String movieName);

  /**
               * Both manager and Customer
               */
  String bookMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
  String getBookingSchedule (String customerID);
  String cancelMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
  String exchangeTickets (String customerID, String newMovieID, String newMovieName, String oldMovieID, int numberOfTickets);
  void shutdown ();
} // interface ServerObjectInterfaceOperations
