package com.parkit.parkingsystem.integration.service;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.Calendar;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

public class TestDataInsertionService {

    private static final Logger logger = LogManager.getLogger("TestDataInsertionService");    
        public DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    
        public void insertTestTicket(String vehicleRegNumber, Calendar inTime, Calendar outTime, double price) {
            Connection con = null;
            PreparedStatement  ps = null;
            try {
                con = dataBaseTestConfig.getConnection();
                ps = con.prepareStatement(
                    "INSERT INTO ticket (PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) VALUES (?, ?, ?, ?, ?)"
                );
                ps.setInt(1, 1); 
                ps.setString(2, vehicleRegNumber);
                ps.setDouble(3, price);
                ps.setTimestamp(4, new Timestamp(inTime.getTimeInMillis()));
                ps.setTimestamp(5, outTime != null ? new Timestamp(outTime.getTimeInMillis()) : null);
                ps.execute();
        } catch (Exception ex) {
            logger.error("Error inserting test ticket", ex);
        } finally {
            dataBaseTestConfig.closePreparedStatement(ps);
            dataBaseTestConfig.closeConnection(con);
        }
    }
}
