package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.FormatUtil;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.integration.service.TestDataInsertionService;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Integration tests for database interactions.
 * Ensures that ticket and parking spot management functions correctly.
 */
@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static TestDataInsertionService testDataInsertionService;
    private static final String VEHICLE_REG_NUMBER = "ABCDEF";

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        testDataInsertionService = new TestDataInsertionService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    /**
     * Helper method to compare two double values after rounding them to one decimal place.
     *
     * @param a First value to compare
     * @param b Second value to compare
     * @return true if the rounded values are equal, false otherwise
     */
    private boolean isSameRoundedValue(double a, double b) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        String ticketPrice = decimalFormat.format(a);
        String DBPrice = decimalFormat.format(b);

        return ticketPrice.equals(DBPrice);
    }

    // Ensure a ticket is created and the parking spot is marked as unavailable when a car is parked.
    @Test
    public void testParkingACar() {
        // ARRANGE : 
        // simulate parking a car 
        when(inputReaderUtil.readSelection()).thenReturn(1);
        ParkingSpotDAO spyParkingSpotDAO = spy(parkingSpotDAO);
        ParkingService parkingService = new ParkingService(inputReaderUtil, spyParkingSpotDAO, ticketDAO);

        int parkingSpotId = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        ParkingSpot parkingSpot = new ParkingSpot(parkingSpotId, ParkingType.CAR, false);

        // ACT : process incoming vehicle
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket(VEHICLE_REG_NUMBER);

        // ASSERT : verify that the ticket is created and the parking spot is marked as unavailable
        verify(spyParkingSpotDAO).updateParking(any(ParkingSpot.class));
        assertEquals(false, parkingSpot.isAvailable(), "Parking spot should be marked as unavailable");

        assertNotNull(ticket, "Ticket should exist in the database");
        assertEquals(0, ticket.getPrice(), "Initial price should be zero");
        assertNotNull(ticket.getInTime(), "In-time should be recorded");
        assertNull(ticket.getOutTime(), "Out-time should not be recorded yet");
    }

    // Verifies that the generated fare and exit time are correctly stored  in the database.
    @Test
    public void testParkingLotExit() {
        // ARRANGE :
        // simulate parking a car 1 hour ago
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Calendar inTime = Calendar.getInstance();
        inTime.add(Calendar.HOUR, -1);
        testDataInsertionService.insertTestTicket(VEHICLE_REG_NUMBER, inTime, null, 0);

        // ACT : 
        parkingService.processExitingVehicle();      
        // get the ticket from the database
        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        // ASSERT : verify that the fare and exit time are correctly stored in the database
        assertNotNull(ticket, "Ticket should exist in the database");
        assertNotNull(ticket.getOutTime().getTime(), "Exit time should be recorded");
        assertNotNull(ticket.getPrice(), "Ticket should have a price");
    }

    // The price should be the discount price for a recurring user.
    @Test
    public void testParkingLotExitRecurringUser() throws InterruptedException {
        // ARRANGE :
        // simulate parking the same car multiple times (recurring user), then add a new entry for processing the exit
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Calendar inTime = Calendar.getInstance();
        Calendar outTime = Calendar.getInstance();
        inTime.add(Calendar.HOUR, -1);
        int entry = 1;
        do {
            testDataInsertionService.insertTestTicket(VEHICLE_REG_NUMBER, inTime, outTime, 0);
            entry++;
        } while (entry < Fare.MIN_USES_FOR_FREQUENT_USER);

        testDataInsertionService.insertTestTicket(VEHICLE_REG_NUMBER, inTime, null, 0);

        // ACT :
        parkingService.processExitingVehicle();
        
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        double ticketPrice = ticket.getPrice();
        double expectedPrice = FormatUtil.roundToTwoDecimals(Fare.CAR_RATE_PER_HOUR) * (Fare.FREQUENT_USER_REDUCTION_RATE);

        // ASSERT : verify that the fare is correctly calculated for a recurring user
        assertNotNull(ticket);
        assertTrue(ticket.getPrice() < Fare.CAR_RATE_PER_HOUR, "Discounted price should be less than regular price");
        assertTrue(isSameRoundedValue(ticketPrice, expectedPrice), "Discounted price should should match the expected price rounded to the first decimal place");
    }
}
