package com.traffic.model;

import java.time.LocalDateTime;

public class Challan {

    private String challanId;

    private String vehicleNumber;

    private ViolationType violationType;

    private String location;

    private LocalDateTime timestamp;

    private double speed;

    private double permittedSpeed;

    private double fineAmount;

    private PaymentStatus paymentStatus;

    public Challan(String challanId,
                   String vehicleNumber,
                   ViolationType violationType,
                   String location,
                   LocalDateTime timestamp,
                   double speed,
                   double permittedSpeed,
                   double fineAmount) {

        this.challanId = challanId;

        this.vehicleNumber = vehicleNumber;

        this.violationType = violationType;

        this.location = location;

        this.timestamp = timestamp;

        this.speed = speed;

        this.permittedSpeed = permittedSpeed;

        this.fineAmount = fineAmount;

        this.paymentStatus =
                PaymentStatus.UNPAID;
    }

    public String getChallanId() {
        return challanId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public ViolationType getViolationType() {
        return violationType;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getSpeed() {
        return speed;
    }

    public double getPermittedSpeed() {
        return permittedSpeed;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(
            PaymentStatus paymentStatus) {

        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {

        return "Challan{" +
                "challanId='" +
                challanId + '\'' +
                ", vehicleNumber='" +
                vehicleNumber + '\'' +
                ", violationType=" +
                violationType +
                ", location='" +
                location + '\'' +
                ", timestamp=" +
                timestamp +
                ", speed=" +
                speed +
                ", permittedSpeed=" +
                permittedSpeed +
                ", fineAmount=" +
                fineAmount +
                ", paymentStatus=" +
                paymentStatus +
                '}';
    }
}
