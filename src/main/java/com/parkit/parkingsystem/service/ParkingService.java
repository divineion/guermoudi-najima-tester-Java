package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.FormatUtil;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * This method handles incoming vehicles.
     * Steps :
     * - checks if a parking spot is available and its number is valid ;
     * - if available, it promps for the registration number of the incoming vehicle
     * ;
     * - checks if the incoming vehicle is not already in the parking lot
     * based on its registration number ;
     * - if the vehicle is not already in the parking lot, it marks the parking spot
     * as unavailable ;
     * - creates a new parking ticket and saves it in the database ;
     * - displays a message with the assigned parkoing spot number and the recorded
     * in-time for the vehicle.
     * 
     * @see TicketDAO#isVehicleAlreadyInParking(String)
     * @see ParkingSpotDAO#updateParking(ParkingSpot)
     * 
     */
    public void processIncomingVehicle() {
        try {
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if (parkingSpot != null && parkingSpot.getId() > 0) {
                String vehicleRegNumber = getVehicleRegNumber();

                if (ticketDAO.isVehicleAlreadyInParking(vehicleRegNumber)) {
                    System.out.println("Error : this vehicle is already in the parking");

                    return;
                }

                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as false

                Calendar inTime = Calendar.getInstance();
                Ticket ticket = new Ticket();
                // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
                // ticket.setId(ticketID);
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(FormatUtil.roundToTwoDecimals(0));
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);

                int count = ticketDAO.getNbTicket(vehicleRegNumber);
                if (count >= Fare.MIN_USES_FOR_FREQUENT_USER) {
                    System.out.println("Welcome back! As a regular user of our parking, you will receive a 5% discount.");
                }

                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + ticket.getInTime().getTime());
            }
        } catch (Exception e) {
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    /**
     * Prompts the user to enter their vehicle registration number.
     *
     * @return The vehicle registration number as a string.
     * @throws Exception If an input error occurs.
     */
    private String getVehicleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     * Retrieves the next available parking spot.
     *
     * @return A ParkingSpot object if available, otherwise null.
     */
    public ParkingSpot getNextParkingNumberIfAvailable() {
        int parkingNumber = 0;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            } else {
                System.out.println("Error fetching parking number from DB. Parking slots might be full");
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch (IllegalArgumentException ie) {
            logger.error("Error parsing user input for type of vehicle", ie);
        } catch (Exception e) {
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    /**
     * Prompts the user to select the type of vehicle and returns the corresponding
     * ParkingType.
     *
     * @return The ParkingType selected by the user.
     * @throws IllegalArgumentException If the user enters an invalid selection.
     */
    private ParkingType getVehicleType() {
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    /**
     * Handles the exit process of a vehicle by:
     * - Retrieving the active ticket.
     * - Calculating the fare, applying a discount if applicable.
     * - Updating the parking spot availability.
     * - Displaying the payment details and exit time.
     */
    public void processExitingVehicle() {
        try {
            String vehicleRegNumber = getVehicleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber); 

            Calendar outTime = Calendar.getInstance();
            
            ticket.setOutTime(outTime);

            int count = ticketDAO.getNbTicket(vehicleRegNumber);

            fareCalculatorService.calculateFare(ticket, count >= Fare.MIN_USES_FOR_FREQUENT_USER);

            if (ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);

                if (count >= Fare.MIN_USES_FOR_FREQUENT_USER) {
                    System.out.println("Thank you for your loyalty !");
                }

                double displayedPrice = ticket.getPrice();

                System.out.println("Please pay the parking fare:" + displayedPrice + "\n");
                System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:"+ outTime.getTime() + "\n");
            } else {
                System.out.println("Unable to update ticket information. Error occurred");
            }
        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }
}
