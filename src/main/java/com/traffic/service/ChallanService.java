package com.traffic.service;

import com.traffic.exception.DuplicateChallanException;
import com.traffic.exception.InvalidVehicleException;
import com.traffic.exception.VehicleNotFoundException;

import com.traffic.model.Challan;
import com.traffic.model.PaymentStatus;
import com.traffic.model.Vehicle;
import com.traffic.model.VehicleClassification;
import com.traffic.model.ViolationType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChallanService {

    private Map<String, Vehicle> vehicles;

    private Map<String, Challan> challans;


    // ==================================================
    // CONSTRUCTOR
    // ==================================================

    public ChallanService() {

        vehicles =
                new LinkedHashMap<String, Vehicle>();

        challans =
                new LinkedHashMap<String, Challan>();
    }


    // ==================================================
    // REGISTER VEHICLE
    // ==================================================

    public void registerVehicle(
            Vehicle vehicle)
            throws InvalidVehicleException {

        if (vehicle == null) {

            throw new InvalidVehicleException(
                    "Vehicle cannot be null");
        }

        if (vehicle.getVehicleNumber() == null ||
                vehicle.getVehicleNumber()
                        .trim()
                        .isEmpty()) {

            throw new InvalidVehicleException(
                    "Vehicle number is invalid");
        }

        String vehicleNumber =
                vehicle.getVehicleNumber()
                        .trim()
                        .toUpperCase();

        if (vehicles.containsKey(vehicleNumber)) {

            throw new InvalidVehicleException(
                    "Vehicle already registered");
        }

        vehicles.put(
                vehicleNumber,
                vehicle);
    }


    // ==================================================
    // GENERATE CHALLAN
    // ==================================================

    public Challan generateChallan(
            String vehicleNumber,
            ViolationType violationType,
            String location,
            LocalDateTime timestamp,
            double speed,
            double permittedSpeed)
            throws VehicleNotFoundException,
            InvalidVehicleException,
            DuplicateChallanException {


        // ----------------------------------------------
        // VALIDATE INPUT
        // ----------------------------------------------

        validateViolationData(
                vehicleNumber,
                violationType,
                location,
                timestamp,
                speed,
                permittedSpeed);


        // ----------------------------------------------
        // FIND VEHICLE
        // ----------------------------------------------

        String number =
                vehicleNumber
                        .trim()
                        .toUpperCase();

        Vehicle vehicle =
                vehicles.get(number);

        if (vehicle == null) {

            throw new VehicleNotFoundException(
                    "Vehicle not found: "
                    + vehicleNumber);
        }


        // ----------------------------------------------
        // CHECK DUPLICATE CHALLAN
        // ----------------------------------------------

        for (Challan existing :
                challans.values()) {

            if (isSameViolationEvent(
                    existing,
                    number,
                    violationType,
                    location,
                    timestamp)) {

                throw new DuplicateChallanException(
                        "Duplicate challan for "
                        + "the same violation event");
            }
        }


        // ==================================================
        // IMPORTANT FIX
        // ==================================================
        //
        // Get the OLD violation count first.
        //
        // First violation:
        // previousViolations = 0
        //
        // Second violation:
        // previousViolations = 1
        //
        // Therefore the first violation does NOT receive
        // the repeated-violation penalty.
        // ==================================================

        int previousViolations =
                vehicle.getViolationCount();


        // ----------------------------------------------
        // CALCULATE FINE
        // ----------------------------------------------

        double fine =
                calculateFine(
                        violationType,
                        speed,
                        permittedSpeed,
                        previousViolations);


        // ----------------------------------------------
        // NOW INCREMENT VIOLATION COUNT
        // ----------------------------------------------

        vehicle.incrementViolationCount();


        // ----------------------------------------------
        // GENERATE CHALLAN ID
        // ----------------------------------------------

        String challanId =
                "CH-" +
                String.format(
                        "%04d",
                        challans.size() + 1);


        // ----------------------------------------------
        // CREATE CHALLAN
        // ----------------------------------------------

        Challan challan =
                new Challan(
                        challanId,
                        number,
                        violationType,
                        location,
                        timestamp,
                        speed,
                        permittedSpeed,
                        fine);


        // ----------------------------------------------
        // STORE CHALLAN
        // ----------------------------------------------

        challans.put(
                challanId,
                challan);


        return challan;
    }


    // ==================================================
    // VALIDATE VIOLATION DATA
    // ==================================================

    private void validateViolationData(
            String vehicleNumber,
            ViolationType violationType,
            String location,
            LocalDateTime timestamp,
            double speed,
            double permittedSpeed)
            throws InvalidVehicleException {


        if (vehicleNumber == null ||
                vehicleNumber.trim().isEmpty()) {

            throw new InvalidVehicleException(
                    "Vehicle number is required");
        }


        if (violationType == null) {

            throw new InvalidVehicleException(
                    "Violation type is required");
        }


        if (location == null ||
                location.trim().isEmpty()) {

            throw new InvalidVehicleException(
                    "Violation location is required");
        }


        if (timestamp == null) {

            throw new InvalidVehicleException(
                    "Timestamp is required");
        }


        if (speed < 0) {

            throw new InvalidVehicleException(
                    "Speed cannot be negative");
        }


        if (permittedSpeed <= 0) {

            throw new InvalidVehicleException(
                    "Permitted speed must be greater than zero");
        }
    }


    // ==================================================
    // CHECK DUPLICATE VIOLATION
    // ==================================================

    private boolean isSameViolationEvent(
            Challan challan,
            String vehicleNumber,
            ViolationType violationType,
            String location,
            LocalDateTime timestamp) {


        return challan.getVehicleNumber()
                .equalsIgnoreCase(vehicleNumber)

                && challan.getViolationType()
                == violationType

                && challan.getLocation()
                .equalsIgnoreCase(location)

                && challan.getTimestamp()
                .equals(timestamp);
    }


    // ==================================================
    // CALCULATE FINE
    // ==================================================

    public double calculateFine(
            ViolationType violationType,
            double speed,
            double permittedSpeed,
            int previousViolations) {


        double baseFine;


        switch (violationType) {


            // ------------------------------------------
            // OVER SPEEDING
            // ------------------------------------------

            case OVER_SPEEDING:

                double excess =
                        speed - permittedSpeed;


                // Speed is within permitted limit
                if (excess <= 0) {

                    return 0.0;
                }


                // 1 - 10 km/h over limit
                if (excess <= 10) {

                    baseFine = 500.0;
                }


                // 11 - 20 km/h over limit
                else if (excess <= 20) {

                    baseFine = 1000.0;
                }


                // More than 20 km/h over limit
                else {

                    baseFine = 2000.0;
                }

                break;


            // ------------------------------------------
            // SIGNAL VIOLATION
            // ------------------------------------------

            case SIGNAL_VIOLATION:

                baseFine = 1000.0;

                break;


            // ------------------------------------------
            // ILLEGAL PARKING
            // ------------------------------------------

            case ILLEGAL_PARKING:

                baseFine = 500.0;

                break;


            default:

                baseFine = 0.0;
        }


        // ==================================================
        // REPEATED VIOLATION PENALTY
        // ==================================================
        //
        // previousViolations = 0
        //     Normal fine
        //
        // previousViolations >= 1
        //     1.5 times fine
        //
        // previousViolations >= 3
        //     Additional 1.5 times
        // ==================================================

        if (previousViolations >= 1) {

            baseFine =
                    baseFine * 1.5;
        }


        if (previousViolations >= 3) {

            baseFine =
                    baseFine * 1.5;
        }


        return baseFine;
    }


    // ==================================================
    // PAY CHALLAN
    // ==================================================

    public void payChallan(
            String challanId) {


        Challan challan =
                challans.get(challanId);


        if (challan == null) {

            throw new IllegalArgumentException(
                    "Challan not found");
        }


        if (challan.getPaymentStatus()
                == PaymentStatus.PAID) {

            throw new IllegalArgumentException(
                    "Challan is already paid");
        }


        challan.setPaymentStatus(
                PaymentStatus.PAID);
    }


    // ==================================================
    // TOTAL OUTSTANDING FINE
    // ==================================================

    public double getTotalOutstandingFine() {

        double total = 0.0;


        for (Challan challan :
                challans.values()) {


            if (challan.getPaymentStatus()
                    == PaymentStatus.UNPAID) {

                total =
                        total +
                        challan.getFineAmount();
            }
        }


        return total;
    }


    // ==================================================
    // VEHICLE OUTSTANDING FINE
    // ==================================================

    public double getVehicleOutstandingFine(
            String vehicleNumber) {

        double total = 0.0;


        if (vehicleNumber == null) {

            return 0.0;
        }


        for (Challan challan :
                challans.values()) {


            if (challan.getVehicleNumber()
                    .equalsIgnoreCase(
                            vehicleNumber.trim())

                    &&

                    challan.getPaymentStatus()
                    == PaymentStatus.UNPAID) {


                total =
                        total +
                        challan.getFineAmount();
            }
        }


        return total;
    }


    // ==================================================
    // CLASSIFY VEHICLE
    // ==================================================

    public VehicleClassification
    classifyVehicle(
            String vehicleNumber)
            throws VehicleNotFoundException {


        if (vehicleNumber == null ||
                vehicleNumber.trim().isEmpty()) {

            throw new VehicleNotFoundException(
                    "Vehicle number is required");
        }


        Vehicle vehicle =
                vehicles.get(
                        vehicleNumber
                                .trim()
                                .toUpperCase());


        if (vehicle == null) {

            throw new VehicleNotFoundException(
                    "Vehicle not found");
        }


        int count =
                vehicle.getViolationCount();


        if (count == 0) {

            return VehicleClassification.CLEAN;
        }


        else if (count <= 2) {

            return VehicleClassification.LOW_RISK;
        }


        else if (count <= 4) {

            return VehicleClassification.MEDIUM_RISK;
        }


        else {

            return VehicleClassification.HIGH_RISK;
        }
    }


    // ==================================================
    // GET CHALLAN
    // ==================================================

    public Challan getChallan(
            String challanId) {

        return challans.get(challanId);
    }


    // ==================================================
    // GET ALL CHALLANS
    // ==================================================

    public List<Challan> getAllChallans() {

        return new ArrayList<Challan>(
                challans.values());
    }


    // ==================================================
    // GET VEHICLE
    // ==================================================

    public Vehicle getVehicle(
            String vehicleNumber) {

        if (vehicleNumber == null) {

            return null;
        }

        return vehicles.get(
                vehicleNumber
                        .trim()
                        .toUpperCase());
    }


    // ==================================================
    // GET VEHICLE COUNT
    // ==================================================

    public int getVehicleCount() {

        return vehicles.size();
    }


    // ==================================================
    // GET CHALLAN COUNT
    // ==================================================

    public int getChallanCount() {

        return challans.size();
    }
}