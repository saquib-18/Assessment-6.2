package com.traffic.app;

import com.traffic.model.*;
import com.traffic.service.ChallanService;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args)
            throws Exception {

        ChallanService service =
                new ChallanService();

        // ==========================================
        // REGISTER VEHICLES
        // ==========================================

        Vehicle vehicle =
                new Vehicle(
                        "TN01AB1234",
                        "Rahul Kumar",
                        "9876543210",
                        VehicleType.CAR);

        service.registerVehicle(vehicle);

        // ==========================================
        // GENERATE CHALLAN
        // ==========================================

        Challan challan =
                service.generateChallan(
                        "TN01AB1234",
                        ViolationType.OVER_SPEEDING,
                        "Chennai Anna Salai",
                        LocalDateTime.now(),
                        85,
                        60);

        // ==========================================
        // DISPLAY CHALLAN
        // ==========================================

        System.out.println(
                "========================================");

        System.out.println(
                "TRAFFIC VIOLATION & E-CHALLAN SYSTEM");

        System.out.println(
                "========================================");

        System.out.println(
                "Challan ID : "
                + challan.getChallanId());

        System.out.println(
                "Vehicle Number : "
                + challan.getVehicleNumber());

        System.out.println(
                "Violation : "
                + challan.getViolationType());

        System.out.println(
                "Location : "
                + challan.getLocation());

        System.out.println(
                "Speed : "
                + challan.getSpeed());

        System.out.println(
                "Permitted Speed : "
                + challan.getPermittedSpeed());

        System.out.println(
                "Fine : ₹"
                + challan.getFineAmount());

        System.out.println(
                "Payment Status : "
                + challan.getPaymentStatus());

        System.out.println(
                "Vehicle Classification : "
                + service.classifyVehicle(
                        "TN01AB1234"));

        System.out.println(
                "Outstanding Fine : ₹"
                + service.getTotalOutstandingFine());

        System.out.println(
                "========================================");
    }
}