package com.parkit.parkingsystem.model;

import java.util.Calendar;

public class Ticket {
    private int id;
    private ParkingSpot parkingSpot;
    private String vehicleRegNumber;
    private double price;
    private Calendar inTime;
    private Calendar outTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Calendar getInTime() {
        return inTime;
    }

    public void setInTime(Calendar inTime) {
        this.inTime = inTime;
    }

    public Calendar getOutTime() {
        return outTime;
    }

    public void setOutTime(Calendar outTime) {
        this.outTime = outTime;
    }
}
