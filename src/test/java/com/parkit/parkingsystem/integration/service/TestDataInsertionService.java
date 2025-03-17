package com.parkit.parkingsystem.integration.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.Calendar;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

public class TestDataInsertionService {

    private static final Logger logger = LogManager.getLogger("TestDataInsertionService");
    public DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public static final String SAVE_TEST_TICKET = "INSERT INTO ticket (PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) VALUES (?, ?, ?, ?, ?);";

    public void insertTestTicket(String vehicleRegNumber, Calendar inTime, Calendar outTime, double price) {
        try (Connection con = dataBaseTestConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(SAVE_TEST_TICKET)) {
            ps.setInt(1, 1);
            ps.setString(2, vehicleRegNumber);
            ps.setDouble(3, price);
            ps.setTimestamp(4, new Timestamp(inTime.getTimeInMillis()));
            ps.setTimestamp(5, outTime != null ? new Timestamp(outTime.getTimeInMillis()) : null);
            ps.execute();
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Error inserting test ticket", e);
        }
    }
}
