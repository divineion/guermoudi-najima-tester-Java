package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public boolean saveTicket(Ticket ticket) {
        boolean result = false;
        try (Connection con = dataBaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET)) {
            // ID AUTO_INCR, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTimeInMillis()));
            ps.setTimestamp(5,
                    (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTimeInMillis())));
            result = ps.execute();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error fetching next available slot", e);
        }

        return result;
    }

    public Ticket getTicket(String vehicleRegNumber) {
        Ticket ticket = null;
        try (
                Connection con = dataBaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET)) {
            // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1, vehicleRegNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ticket = new Ticket();
                    Calendar inTime = Calendar.getInstance();
                    ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),
                            false);

                    ticket.setParkingSpot(parkingSpot);
                    ticket.setId(rs.getInt(2));
                    ticket.setVehicleRegNumber(vehicleRegNumber);
                    ticket.setPrice(rs.getDouble(3));
                    inTime.setTimeInMillis(rs.getTimestamp(4).getTime());
                    ticket.setInTime(inTime);
                    Timestamp outTime = rs.getTimestamp(5);
                    if (outTime != null) {
                        Calendar outCalendar = Calendar.getInstance();
                        outCalendar.setTime(outTime);
                        ticket.setOutTime(outCalendar);
                    } else {
                        ticket.setOutTime(null);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching next available slot", e);
        }

        return ticket;
    }

    public boolean updateTicket(Ticket ticket) {
        try (Connection con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET)) {
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTimeInMillis()));
            ps.setInt(3, ticket.getId());
            ps.executeUpdate();

            return true;
        } catch (Exception e) {
            logger.error("Error saving ticket info", e);
        }

        return false;
    }

    /**
     * This method verifies if the entering vehicle is not already in.
     * 
     * @param vehicleRegNumber
     * @return true if the entering vehicle has already an in-time without out-time,
     *         false otherwise.
     */
    public boolean isVehicleAlreadyInParking(String vehicleRegNumber) {
        boolean isInParking = false;

        try (Connection con = dataBaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(DBConstants.CHECK_VEHICLE_IN_WITHOUT_OUT)) {
            ps.setString(1, vehicleRegNumber);
            try (ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    logger.info("Entry attempt failure: the vehicle {} has never exited since the last entry",
                            vehicleRegNumber);

                    isInParking = true;
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred : ", e);
        }

        return isInParking;
    }

    /**
     * This method verifies how many times a vehicle has used the service in the
     * last 30 days.
     * 
     * @param vehicleRegNumber the registration number of the vehicle.
     * @return the number of times the vehicle has used the service within the last
     *         30 days.
     */
    public int getNbTicket(String vehicleRegNumber) {
        int count = 0;
        try (Connection con = dataBaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(DBConstants.GET_NB_TICKETS)) {
            ps.setString(1, vehicleRegNumber);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching ticket count for vehicle {}", vehicleRegNumber, e);
        }

        return count;
    }
}
