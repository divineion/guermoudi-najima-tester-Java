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

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

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
        Calendar inTime = Calendar.getInstance();
        inTime.add(Calendar.HOUR, -1);
        Calendar outTime = Calendar.getInstance();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(FormatUtil.roundToTwoDecimals(ticket.getPrice()), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike(){
        Calendar inTime = Calendar.getInstance();
        inTime.add(Calendar.HOUR, -1);
        Calendar outTime = Calendar.getInstance();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(FormatUtil.roundToTwoDecimals(ticket.getPrice()), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType(){
        Calendar inTime = Calendar.getInstance();
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
        Calendar inTime = Calendar.getInstance();
        Calendar outTime = Calendar.getInstance();
        inTime.add(Calendar.HOUR, +1);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Calendar inTime = Calendar.getInstance();
        inTime.add(Calendar.MINUTE, -45);//45 minutes parking time should give 3/4th parking fare
        Calendar outTime = Calendar.getInstance();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(FormatUtil.roundToTwoDecimals(0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice() );
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Calendar inTime = Calendar.getInstance();
        inTime.add(Calendar.MINUTE, -45);//45 minutes parking time should give 3/4th parking fare
        Calendar outTime = Calendar.getInstance();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(FormatUtil.roundToTwoDecimals(0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Calendar inTime = Calendar.getInstance();
        inTime.add(Calendar.HOUR, -24);//24 hours parking time should give 24 * parking fare per hour
        Calendar outTime = Calendar.getInstance();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(FormatUtil.roundToTwoDecimals(24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        Calendar outTime = Calendar.getInstance();
        Calendar inTime = Calendar.getInstance();
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
        Calendar outTime = Calendar.getInstance();
        Calendar inTime = Calendar.getInstance();
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
        Calendar outTime = Calendar.getInstance();
        Calendar inTime = Calendar.getInstance();
        inTime.add(Calendar.MINUTE, -30);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        assertEquals(FormatUtil.roundToTwoDecimals(0.5 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice(), "Test failed for exactly 30 minutes parking time");
    }

    //ce test doit appeler la méthode calculateFare 
    //avec un ticket concernant une voiture et avec le paramètre discount à true, 
    //puis vérifier que le prix calculé est est bien de 95% du tarif plein. 
    //La durée du ticket doit être de plus de 30 minutes.
    @Test
    public void calculateFareCarWithDiscount() {
        Calendar inTime = Calendar.getInstance();
        Calendar outTime = Calendar.getInstance();
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
        Calendar inTime = Calendar.getInstance();
        Calendar outTime = Calendar.getInstance();
        inTime.add(Calendar.HOUR, -1);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);

        assertEquals((FormatUtil.roundToTwoDecimals(Fare.FREQUENT_USER_REDUCTION_RATE) * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }
}

