package com.traffic.service;

import com.traffic.exception.*;
import com.traffic.model.*;

import junit.framework.TestCase;

import java.time.LocalDateTime;

public class ChallanServiceTest
        extends TestCase {

    private ChallanService service;

    private LocalDateTime time;

    protected void setUp() {

        service =
                new ChallanService();

        time =
                LocalDateTime.of(
                        2026,
                        9,
                        10,
                        10,
                        30);

        try {

            service.registerVehicle(
                    new Vehicle(
                            "TN01AA1111",
                            "Arun",
                            "9000000001",
                            VehicleType.CAR));

            service.registerVehicle(
                    new Vehicle(
                            "TN02BB2222",
                            "Kumar",
                            "9000000002",
                            VehicleType.TWO_WHEELER));

        } catch (Exception e) {

            fail(e.getMessage());
        }
    }

    // ================================================
    // TEST 1
    // VEHICLE REGISTRATION
    // ================================================

    public void testVehicleRegistration() {

        assertEquals(
                2,
                service.getVehicleCount());
    }

    // ================================================
    // TEST 2
    // NORMAL SPEEDING
    // ================================================

    public void testNormalSpeedingFine()
            throws Exception {

        Challan challan =
                service.generateChallan(
                        "TN01AA1111",
                        ViolationType.OVER_SPEEDING,
                        "Chennai",
                        time,
                        70,
                        60);

        assertEquals(
                500.0,
                challan.getFineAmount());
    }

    // ================================================
    // TEST 3
    // MEDIUM SPEEDING
    // ================================================

    public void testMediumSpeedingFine()
            throws Exception {

        Challan challan =
                service.generateChallan(
                        "TN01AA1111",
                        ViolationType.OVER_SPEEDING,
                        "Chennai",
                        time,
                        80,
                        60);

        assertEquals(
                1000.0,
                challan.getFineAmount());
    }

    // ================================================
    // TEST 4
    // HIGH SPEEDING
    // ================================================

    public void testHighSpeedingFine()
            throws Exception {

        Challan challan =
                service.generateChallan(
                        "TN01AA1111",
                        ViolationType.OVER_SPEEDING,
                        "Chennai",
                        time,
                        100,
                        60);

        assertEquals(
                2000.0,
                challan.getFineAmount());
    }

    // ================================================
    // TEST 5
    // SPEED AT PERMITTED LIMIT
    // ================================================

    public void testSpeedAtPermittedLimit()
            throws Exception {

        Challan challan =
                service.generateChallan(
                        "TN01AA1111",
                        ViolationType.OVER_SPEEDING,
                        "Chennai",
                        time,
                        60,
                        60);

        assertEquals(
                0.0,
                challan.getFineAmount());
    }

    // ================================================
    // TEST 6
    // SIGNAL VIOLATION
    // ================================================

    public void testSignalViolation()
            throws Exception {

        Challan challan =
                service.generateChallan(
                        "TN01AA1111",
                        ViolationType.SIGNAL_VIOLATION,
                        "Chennai",
                        time,
                        40,
                        60);

        assertEquals(
                1000.0,
                challan.getFineAmount());
    }

    // ================================================
    // TEST 7
    // ILLEGAL PARKING
    // ================================================

    public void testIllegalParking()
            throws Exception {

        Challan challan =
                service.generateChallan(
                        "TN01AA1111",
                        ViolationType.ILLEGAL_PARKING,
                        "Chennai",
                        time,
                        0,
                        30);

        assertEquals(
                500.0,
                challan.getFineAmount());
    }

    // ================================================
    // TEST 8
    // REPEATED VIOLATION
    // ================================================

    public void testRepeatedViolationFine()
            throws Exception {

        service.generateChallan(
                "TN01AA1111",
                ViolationType.SIGNAL_VIOLATION,
                "Location1",
                time,
                50,
                60);

        Challan second =
                service.generateChallan(
                        "TN01AA1111",
                        ViolationType.SIGNAL_VIOLATION,
                        "Location2",
                        time.plusMinutes(10),
                        50,
                        60);

        assertEquals(
                1500.0,
                second.getFineAmount());
    }

    // ================================================
    // TEST 9
    // PAYMENT
    // ================================================

    public void testChallanPayment()
            throws Exception {

        Challan challan =
                service.generateChallan(
                        "TN01AA1111",
                        ViolationType.ILLEGAL_PARKING,
                        "Chennai",
                        time,
                        0,
                        30);

        assertEquals(
                PaymentStatus.UNPAID,
                challan.getPaymentStatus());

        service.payChallan(
                challan.getChallanId());

        assertEquals(
                PaymentStatus.PAID,
                challan.getPaymentStatus());
    }

    // ================================================
    // TEST 10
    // OUTSTANDING FINE
    // ================================================

    public void testOutstandingFine()
            throws Exception {

        service.generateChallan(
                "TN01AA1111",
                ViolationType.ILLEGAL_PARKING,
                "Chennai",
                time,
                0,
                30);

        service.generateChallan(
                "TN02BB2222",
                ViolationType.SIGNAL_VIOLATION,
                "Chennai",
                time.plusMinutes(1),
                40,
                60);

        assertEquals(
                1500.0,
                service.getTotalOutstandingFine());
    }

    // ================================================
    // TEST 11
    // VEHICLE CLASSIFICATION - CLEAN
    // ================================================

    public void testCleanVehicleClassification()
            throws Exception {

        assertEquals(
                VehicleClassification.CLEAN,
                service.classifyVehicle(
                        "TN01AA1111"));
    }

    // ================================================
    // TEST 12
    // VEHICLE CLASSIFICATION
    // ================================================

    public void testLowRiskClassification()
            throws Exception {

        service.generateChallan(
                "TN01AA1111",
                ViolationType.ILLEGAL_PARKING,
                "Location1",
                time,
                0,
                30);

        assertEquals(
                VehicleClassification.LOW_RISK,
                service.classifyVehicle(
                        "TN01AA1111"));
    }

    // ================================================
    // TEST 13
    // INVALID VEHICLE
    // ================================================

    public void testInvalidVehicle()
            throws Exception {

        try {

            service.generateChallan(
                    "INVALID",
                    ViolationType.ILLEGAL_PARKING,
                    "Chennai",
                    time,
                    0,
                    30);

            fail(
                    "Expected VehicleNotFoundException");

        } catch (VehicleNotFoundException e) {

            assertTrue(true);
        }
    }

    // ================================================
    // TEST 14
    // NEGATIVE SPEED
    // ================================================

    public void testNegativeSpeed() {

        try {

            service.generateChallan(
                    "TN01AA1111",
                    ViolationType.OVER_SPEEDING,
                    "Chennai",
                    time,
                    -10,
                    60);

            fail(
                    "Expected InvalidVehicleException");

        } catch (Exception e) {

            assertTrue(true);
        }
    }

    // ================================================
    // TEST 15
    // INVALID PERMITTED SPEED
    // ================================================

    public void testInvalidPermittedSpeed() {

        try {

            service.generateChallan(
                    "TN01AA1111",
                    ViolationType.OVER_SPEEDING,
                    "Chennai",
                    time,
                    50,
                    0);

            fail(
                    "Expected InvalidVehicleException");

        } catch (Exception e) {

            assertTrue(true);
        }
    }

    // ================================================
    // TEST 16
    // EMPTY LOCATION
    // ================================================

    public void testEmptyLocation() {

        try {

            service.generateChallan(
                    "TN01AA1111",
                    ViolationType.ILLEGAL_PARKING,
                    "",
                    time,
                    0,
                    30);

            fail(
                    "Expected InvalidVehicleException");

        } catch (Exception e) {

            assertTrue(true);
        }
    }

    // ================================================
    // TEST 17
    // NULL VIOLATION
    // ================================================

    public void testNullViolation() {

        try {

            service.generateChallan(
                    "TN01AA1111",
                    null,
                    "Chennai",
                    time,
                    50,
                    60);

            fail(
                    "Expected InvalidVehicleException");

        } catch (Exception e) {

            assertTrue(true);
        }
    }

    // ================================================
    // TEST 18
    // DUPLICATE CHALLAN
    // ================================================

    public void testDuplicateChallan()
            throws Exception {

        service.generateChallan(
                "TN01AA1111",
                ViolationType.SIGNAL_VIOLATION,
                "Chennai",
                time,
                40,
                60);

        try {

            service.generateChallan(
                    "TN01AA1111",
                    ViolationType.SIGNAL_VIOLATION,
                    "Chennai",
                    time,
                    40,
                    60);

            fail(
                    "Expected DuplicateChallanException");

        } catch (DuplicateChallanException e) {

            assertTrue(true);
        }
    }

    // ================================================
    // TEST 19
    // MULTIPLE VEHICLES
    // ================================================

    public void testMultipleVehicles()
            throws Exception {

        Challan c1 =
                service.generateChallan(
                        "TN01AA1111",
                        ViolationType.ILLEGAL_PARKING,
                        "Location1",
                        time,
                        0,
                        30);

        Challan c2 =
                service.generateChallan(
                        "TN02BB2222",
                        ViolationType.SIGNAL_VIOLATION,
                        "Location2",
                        time.plusMinutes(1),
                        50,
                        60);

        assertNotNull(c1);
        assertNotNull(c2);

        assertEquals(
                2,
                service.getChallanCount());
    }

    // ================================================
    // TEST 20
    // MULTIPLE UNPAID CHALLANS
    // ================================================

    public void testMultipleUnpaidChallans()
            throws Exception {

        service.generateChallan(
                "TN01AA1111",
                ViolationType.ILLEGAL_PARKING,
                "Location1",
                time,
                0,
                30);

        service.generateChallan(
                "TN01AA1111",
                ViolationType.SIGNAL_VIOLATION,
                "Location2",
                time.plusMinutes(1),
                40,
                60);

        double total =
                service.getVehicleOutstandingFine(
                        "TN01AA1111");

        assertEquals(
                1500.0,
                total);
    }
}