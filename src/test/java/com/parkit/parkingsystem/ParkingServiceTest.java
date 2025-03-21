package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @InjectMocks
    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;

    private static Ticket ticket = new Ticket();

    private void useVehicleRegistrationNumber() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    private void useStubsToGetAndUpdateTicketForExitTests() {
        useVehicleRegistrationNumber();
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        ticket.setOutTime(Calendar.getInstance());
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
    }

    @BeforeEach
    private void setUpPerTest() {
        outputStreamCaptor.reset();
        System.setOut(new PrintStream(outputStreamCaptor));

        try {
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Calendar inTime = Calendar.getInstance();
            inTime.add(Calendar.HOUR, -1);
            ticket.setInTime(inTime);
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    public void testProcessIncomingVehicle() {
        useVehicleRegistrationNumber();
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        
        parkingService.processIncomingVehicle();

        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).isVehicleAlreadyInParking(anyString());
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

        assertEquals(1, parkingService.getNextParkingNumberIfAvailable().getId());
    }

    @Test
    public void testProcessIncomingVehicle_WithLoyaltyDiscountMessage() {
        useVehicleRegistrationNumber();
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.getNbTicket(ticket.getVehicleRegNumber())).thenReturn(5);
        String expectedOutput = "Welcome back! As a regular user of our parking, you will receive a 5% discount.";

        parkingService.processIncomingVehicle();

        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(expectedOutput));
    }

    @Test
    public void testProcessIncomingVahicleAlreadyInParking() {
        useVehicleRegistrationNumber();
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.isVehicleAlreadyInParking(ticket.getVehicleRegNumber())).thenReturn(true);
        String expectedOutput = "Error : this vehicle is already in the parking";

        parkingService.processIncomingVehicle();

        verify(ticketDAO, never()).getNbTicket(anyString());
        verify(ticketDAO, never()).getTicket(anyString());
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));

        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(expectedOutput));
    }

    @Test
    public void processExitingVehicleTest(){
        useVehicleRegistrationNumber();
        useStubsToGetAndUpdateTicketForExitTests();
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        String expectedOutput = "Please pay the parking fare:";

        parkingService.processExitingVehicle();

        verify(ticketDAO).getTicket(anyString());
        verify(ticketDAO).getNbTicket(anyString());
        verify(ticketDAO).updateTicket(ticket);
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));

        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(expectedOutput));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        useStubsToGetAndUpdateTicketForExitTests();
        when(ticketDAO.updateTicket(ticket)).thenReturn(false);
        String expectedOutput = "Unable to update ticket information. Error occurred";

        parkingService.processExitingVehicle();

        verify(ticketDAO).getTicket(anyString());
        verify(ticketDAO).getNbTicket(anyString());
        verify(ticketDAO).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));

        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(expectedOutput));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        parkingService.getNextParkingNumberIfAvailable();

        assertEquals(1, parkingService.getNextParkingNumberIfAvailable().getId());
        assertEquals(true, parkingService.getNextParkingNumberIfAvailable().isAvailable());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(0);

        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();

        assertNull(spot);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        when(inputReaderUtil.readSelection()).thenReturn(3);

        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();

        assertNull(spot);
    }

    @Test
    public void processExitingVehicleTest_forFrequentUsers() {
        useStubsToGetAndUpdateTicketForExitTests();
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(5);

        parkingService.processExitingVehicle();
        
        String expectedOutput = "Thank you for your loyalty !";
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains(expectedOutput));
    }
}
