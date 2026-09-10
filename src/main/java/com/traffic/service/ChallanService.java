package com.traffic.service;

import com.traffic.exception.DuplicateChallanException;
import com.traffic.exception.InvalidVehicleException;
import com.traffic.exception.VehicleNotFoundException;

import com.traffic.model.*;

import java.time.LocalDateTime;
import java.util.*;

public class ChallanService {

    private Map<String, Vehicle> vehicles;

    private Map<String, Challan> challans;

    public ChallanService() {

        vehicles =
                new HashMap<String, Vehicle>();

        challans =
                new LinkedHashMap<String, Challan>();
    }

    // ================================================
    // REGISTER VEHICLE
    // ================================================

    public void registerVehicle(
            Vehicle vehicle)
            throws InvalidVehicleException {

        if (vehicle == null) {

            throw new InvalidVehicleException(
                    "Vehicle cannot be null");
        }

        if (vehicle.getVehicleNumber()
                .trim().isEmpty()) {

            throw new InvalidVehicleException(
                    "Vehicle number is invalid");
        }

        if (vehicles.containsKey(
                vehicle.getVehicleNumber())) {

            throw new InvalidVehicleException(
                    "Vehicle already registered");
        }

        vehicles.put(
                vehicle.getVehicleNumber(),
                vehicle);
    }

    // ================================================
    // GENERATE CHALLAN
    // ================================================

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

        validateViolationData(
                vehicleNumber,
                violationType,
                location,
                timestamp,
                speed,
                permittedSpeed);

        Vehicle vehicle =
                vehicles.get(
                        vehicleNumber.toUpperCase());

        if (vehicle == null) {

            throw new VehicleNotFoundException(
                    "Vehicle not found: "
                    + vehicleNumber);
        }

        // --------------------------------------------
        // PREVENT DUPLICATE CHALLAN
        // --------------------------------------------

        for (Challan existing :
                challans.values()) {

            if (isSameViolationEvent(
                    existing,
                    vehicleNumber,
                    violationType,
                    location,
                    timestamp)) {

                throw new DuplicateChallanException(
                        "Duplicate challan for "
                        + "the same violation event");
            }
        }

        // --------------------------------------------
        // INCREMENT VIOLATION COUNT
        // --------------------------------------------

        vehicle.incrementViolationCount();

        // --------------------------------------------
        // CALCULATE FINE
        // --------------------------------------------

        double fine =
                calculateFine(
                        violationType,
                        speed,
                        permittedSpeed,
                        vehicle.getViolationCount());

        // --------------------------------------------
        // CREATE CHALLAN
        // --------------------------------------------

        String challanId =
                "CH-" +
                String.format(
                        "%04d",
                        challans.size() + 1);

        Challan challan =
                new Challan(
                        challanId,
                        vehicleNumber.toUpperCase(),
                        violationType,
                        location,
                        timestamp,
                        speed,
                        permittedSpeed,
                        fine);

        challans.put(
                challanId,
                challan);

        return challan;
    }

    // ================================================
    // VALIDATION
    // ================================================

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

    // ================================================
    // DUPLICATE CHECK
    // ================================================

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

    // ================================================
    // CALCULATE FINE
    // ================================================

    public double calculateFine(
            ViolationType violationType,
            double speed,
            double permittedSpeed,
            int previousViolations) {

        double baseFine;

        switch (violationType) {

            case OVER_SPEEDING:

                double excess =
                        speed - permittedSpeed;

                if (excess <= 0) {

                    return 0;
                }

                if (excess <= 10) {

                    baseFine = 500;

                } else if (excess <= 20) {

                    baseFine = 1000;

                } else {

                    baseFine = 2000;
                }

                break;

            case SIGNAL_VIOLATION:

                baseFine = 1000;

                break;

            case ILLEGAL_PARKING:

                baseFine = 500;

                break;

            default:

                baseFine = 0;
        }

        // --------------------------------------------
        // REPEATED VIOLATION PENALTY
        // --------------------------------------------

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

    // ================================================
    // PAY CHALLAN
    // ================================================

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

    // ================================================
    // TOTAL OUTSTANDING FINE
    // ================================================

    public double getTotalOutstandingFine() {

        double total = 0;

        for (Challan challan :
                challans.values()) {

            if (challan.getPaymentStatus()
                    == PaymentStatus.UNPAID) {

                total +=
                        challan.getFineAmount();
            }
        }

        return total;
    }

    // ================================================
    // VEHICLE OUTSTANDING FINE
    // ================================================

    public double getVehicleOutstandingFine(
            String vehicleNumber) {

        double total = 0;

        for (Challan challan :
                challans.values()) {

            if (challan.getVehicleNumber()
                    .equalsIgnoreCase(
                            vehicleNumber)

                    &&
                    challan.getPaymentStatus()
                    == PaymentStatus.UNPAID) {

                total +=
                        challan.getFineAmount();
            }
        }

        return total;
    }

    // ================================================
    // CLASSIFY VEHICLE
    // ================================================

    public VehicleClassification
    classifyVehicle(
            String vehicleNumber)
            throws VehicleNotFoundException {

        Vehicle vehicle =
                vehicles.get(
                        vehicleNumber.toUpperCase());

        if (vehicle == null) {

            throw new VehicleNotFoundException(
                    "Vehicle not found");
        }

        int count =
                vehicle.getViolationCount();

        if (count == 0) {

            return VehicleClassification.CLEAN;

        } else if (count <= 2) {

            return VehicleClassification.LOW_RISK;

        } else if (count <= 4) {

            return VehicleClassification.MEDIUM_RISK;

        } else {

            return VehicleClassification.HIGH_RISK;
        }
    }

    // ================================================
    // GET CHALLAN
    // ================================================

    public Challan getChallan(
            String challanId) {

        return challans.get(challanId);
    }

    // ================================================
    // GET ALL CHALLANS
    // ================================================

    public List<Challan> getAllChallans() {

        return new ArrayList<Challan>(
                challans.values());
    }

    // ================================================
    // GET VEHICLE
    // ================================================

    public Vehicle getVehicle(
            String vehicleNumber) {

        return vehicles.get(
                vehicleNumber.toUpperCase());
    }

    // ================================================
    // COUNTS
    // ================================================

    public int getVehicleCount() {

        return vehicles.size();
    }

    public int getChallanCount() {

        return challans.size();
    }
}