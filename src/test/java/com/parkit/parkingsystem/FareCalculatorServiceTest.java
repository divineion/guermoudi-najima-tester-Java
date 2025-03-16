package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.util.FormatUtil;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;

/**
 * This class tests the fare calculation logic based on different scenarios
 * (parking duration, vehicle type, user discounts).
 */
public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private Calendar inTime = Calendar.getInstance();
    private Calendar outTime = Calendar.getInstance();
    
    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
        inTime.add(Calendar.HOUR, -1);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(FormatUtil.roundToTwoDecimals(ticket.getPrice()), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike(){
        inTime.add(Calendar.HOUR, -1);

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(FormatUtil.roundToTwoDecimals(ticket.getPrice()), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType(){
        inTime.add(Calendar.HOUR, -1);
        Calendar outTime = Calendar.getInstance();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){

        inTime.add(Calendar.HOUR, +1);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    //45 minutes parking time should give 3/4th parking fare
    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        inTime.add(Calendar.MINUTE, -45);
        Calendar outTime = Calendar.getInstance();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(FormatUtil.roundToTwoDecimals(0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    //45 minutes parking time should give 3/4th parking fare
    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        inTime.add(Calendar.MINUTE, -45);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(FormatUtil.roundToTwoDecimals(0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    //24 hours parking time should give 24 * parking fare per hour
    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        inTime.add(Calendar.HOUR, -24);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);
        
        assertEquals(FormatUtil.roundToTwoDecimals(24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        inTime.add(Calendar.MINUTE, -29);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);

        assertEquals(FormatUtil.roundToTwoDecimals(0), ticket.getPrice(), "Test failed for 29 minutes parking time");
    }

    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        inTime.add(Calendar.MINUTE, -1);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);

        assertEquals(FormatUtil.roundToTwoDecimals(0), ticket.getPrice(), "Test failed for 1 minute parking time");
    }

    @Test
    public void calculateFareCarWithExactly30MinutesParkingTime() {
        inTime.add(Calendar.MINUTE, -30);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket);

        assertEquals(FormatUtil.roundToTwoDecimals(0.5 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice(), "Test failed for exactly 30 minutes parking time");
    }

    // This test should verify that the calculated price is 95% of the full price. 
    @Test
    public void calculateFareCarWithDiscount() {
        inTime.add(Calendar.HOUR, -1);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, true);

        assertEquals(FormatUtil.roundToTwoDecimals(Fare.FREQUENT_USER_REDUCTION_RATE * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithDiscount() {
        inTime.add(Calendar.HOUR, -1);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        
        fareCalculatorService.calculateFare(ticket, true);

        assertEquals((FormatUtil.roundToTwoDecimals(Fare.FREQUENT_USER_REDUCTION_RATE) * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }
}

