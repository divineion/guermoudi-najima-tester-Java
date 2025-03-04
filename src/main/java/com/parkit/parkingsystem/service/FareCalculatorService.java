package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.Ticket;

/**
 * This class calculates the parking fare based on the in and out times 
 * and the type of the parking spot (car or bike).
 * 
 * 
 * <p>This service uses the timestamps from a {@link Ticket} object to calculate 
 * the duration in hours. 
 * If the duration is less than 30 minutes, no fare is applied. 
 * Otherwise, the fare is calculated based on the duration and the type of vehicle. 
 * </p>
 * 
 * <p>The fare calculation takes into account the parking rates defined in the {@link Fare} class.</p>
 * 
 * <p>If the discount parameter is set to {@code true}, a 5% reduction is applied to the total fare.</p>
 * 
 * @throws {@link IllegalArgumentException} in case of invalid time or invalid parking
 * 
 * @see Ticket
 * @see Fare
 */
public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){

        validateProvidedTime(ticket);

        validateParkingSpot(ticket);

        validateParkingType(ticket);

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        float duration = (outHour - inHour) / (60 * 60 * 1000f);

        if (duration < 0.5) {
            ticket.setPrice(0);
        } else {
            double fare;

            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    fare = duration * Fare.CAR_RATE_PER_HOUR;
                    break;
                }
                case BIKE: {
                    fare = duration * Fare.BIKE_RATE_PER_HOUR;
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
            
            if (discount) {
                ticket.setPrice((fare * Fare.FREQUENT_USER_REDUCTION_RATE * 100) / 100);
            } else {
               ticket.setPrice(fare);
            }
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    private void validateProvidedTime(Ticket ticket) {
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect: "+ticket.getOutTime().toString());
        }
    }

    private void validateParkingSpot(Ticket ticket) {
        if (ticket.getParkingSpot() == null) {
            throw new NullPointerException();
        }
    }

    private void validateParkingType(Ticket ticket) {

        if (ticket.getParkingSpot().getParkingType() == null) {
            throw new NullPointerException("The parking type cannot be null");
        }

        boolean isValidParkingType = false;
        for (ParkingType parkingType : ParkingType.values()) {
            if (ticket.getParkingSpot().getParkingType() == parkingType) {
                isValidParkingType = true;
                break;
            }
        }
    
        if (!isValidParkingType) {
            throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}