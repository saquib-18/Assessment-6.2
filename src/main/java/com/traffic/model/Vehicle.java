package com.traffic.model;

public class Vehicle {

    private String vehicleNumber;

    private String ownerName;

    private String ownerPhone;

    private VehicleType vehicleType;

    private int violationCount;

    public Vehicle(String vehicleNumber,
                   String ownerName,
                   String ownerPhone,
                   VehicleType vehicleType) {

        if (vehicleNumber == null ||
                vehicleNumber.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Vehicle number is required");
        }

        if (ownerName == null ||
                ownerName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Owner name is required");
        }

        if (vehicleType == null) {

            throw new IllegalArgumentException(
                    "Vehicle type is required");
        }

        this.vehicleNumber =
                vehicleNumber.toUpperCase();

        this.ownerName = ownerName;

        this.ownerPhone = ownerPhone;

        this.vehicleType = vehicleType;

        this.violationCount = 0;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public int getViolationCount() {
        return violationCount;
    }

    public void incrementViolationCount() {
        violationCount++;
    }

    @Override
    public String toString() {

        return "Vehicle{" +
                "vehicleNumber='" +
                vehicleNumber + '\'' +
                ", ownerName='" +
                ownerName + '\'' +
                ", vehicleType=" +
                vehicleType +
                ", violationCount=" +
                violationCount +
                '}';
    }
}
