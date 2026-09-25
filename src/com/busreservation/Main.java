package com.busreservation;

import com.busreservation.exception.*;
import com.busreservation.model.*;
import com.busreservation.repository.*;
import com.busreservation.service.*;
import com.busreservation.service.delay.*;
import com.busreservation.service.payment.*;
import com.busreservation.service.rescue.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Main simulation driver demonstrating Online Bus Reservation & Travel
 * Companion Platform.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================================================");
        System.out.println("   ONLINE BUS RESERVATION & TRAVEL COMPANION PLATFORM - SIMULATION SCENARIO      ");
        System.out.println("==================================================================================\n");

        // -------------------------------------------------------------
        // Initialize Data Repositories (OOD Repos)
        // -------------------------------------------------------------
        Repository<Passenger, String> passengerRepo = new InMemoryRepository<>(User::getUserId);
        Repository<Driver, String> driverRepo = new InMemoryRepository<>(User::getUserId);
        Repository<Bus, String> busRepo = new InMemoryRepository<>(Bus::getBusId);
        Repository<Schedule, String> scheduleRepo = new InMemoryRepository<>(Schedule::getScheduleId);
        Repository<Booking, String> bookingRepo = new InMemoryRepository<>(Booking::getBookingId);
        Repository<TripPhoto, String> photoRepo = new InMemoryRepository<>(TripPhoto::getPhotoId);

        // -------------------------------------------------------------
        // Initialize Services
        // -------------------------------------------------------------
        ReservationService reservationService = new ReservationService(bookingRepo, scheduleRepo, passengerRepo);
        ComfortIndexService comfortIndexService = new ComfortIndexService(passengerRepo, scheduleRepo, bookingRepo);
        DelayPredictionService delayPredictionService = new DelayPredictionService(
                new HistoricalTrafficDelayStrategy());
        AutoRescueService autoRescueService = new AutoRescueService(bookingRepo, scheduleRepo, passengerRepo);
        TripGalleryService tripGalleryService = new TripGalleryService(passengerRepo, scheduleRepo, bookingRepo,
                photoRepo);

        // -------------------------------------------------------------
        // PHASE 1: User & Staff Registration (using Factory Pattern)
        // -------------------------------------------------------------
        System.out.println(">>> Phase 1: Registering Users and Staff (Factory Pattern)");

        Passenger passengerAlice = UserFactory.createPassenger("USR-PL-101", "Alice Johnson", "alice@example.com",
                "+91-98765-43210", "passAlice123");
        Passenger passengerBob = UserFactory.createPassenger("USR-PL-102", "Bob Smith", "bob@example.com",
                "+91-98765-43211", "passBob456");

        Driver driverSam = UserFactory.createDriver("USR-DR-201", "Sam Miller", "sam@example.com", "+91-98765-43212",
                "driverSam99", "DL-982361B", 8, "+91-98765-43212");
        Admin adminCarl = UserFactory.createAdmin("USR-AD-301", "Carl Smith", "carl@example.com", "+91-98765-43213",
                "adminCarl88", "SUPER_ADMIN");
        BusOperator operatorVolvo = UserFactory.createBusOperator("USR-OP-401", "Volvo Express LLC",
                "volvo@example.com", "+91-98765-43214", "expressVolvo", "Volvo Premium Fleet");

        passengerRepo.save(passengerAlice);
        passengerRepo.save(passengerBob);
        driverRepo.save(driverSam);

        System.out.println("Registered Passengers: " + passengerAlice.getName() + ", " + passengerBob.getName());
        System.out.println("Registered Driver: " + driverSam.getName() + " (Experience: "
                + driverSam.getYearsOfExperience() + " years)");
        System.out.println(
                "Registered Admin: " + adminCarl.getName() + " (Auth Level: " + adminCarl.getAdminLevel() + ")");
        System.out.println("Registered Operator: " + operatorVolvo.getCompanyName() + "\n");

        // -------------------------------------------------------------
        // PHASE 2: Fleet, Bus Features & Visual Assets Setup
        // -------------------------------------------------------------
        System.out.println(">>> Phase 2: Fleet and Bus Configuration");

        // Construct seats for primary bus layout - 2+2 configuration (40 seats, 10 rows)
        List<Seat> bus1Seats = new ArrayList<>();
        
        // Create 10 rows with 4 seats each (2+2 configuration: left-window, left-aisle, right-aisle, right-window)
        for (int row = 1; row <= 10; row++) {
            // Left window seat
            String seatNum = row + "A";
            bus1Seats.add(new Seat(seatNum, SeatType.WINDOW, 1.15, row, 0, "window-left"));
            
            // Left aisle seat
            seatNum = row + "B";
            bus1Seats.add(new Seat(seatNum, SeatType.AISLE, 1.0, row, 1, "aisle-left"));
            
            // Right aisle seat
            seatNum = row + "C";
            bus1Seats.add(new Seat(seatNum, SeatType.AISLE, 1.0, row, 2, "aisle-right"));
            
            // Right window seat
            seatNum = row + "D";
            bus1Seats.add(new Seat(seatNum, SeatType.WINDOW, 1.15, row, 3, "window-right"));
        }

        SeatMap primarySeatMap = new SeatMap(bus1Seats);
        Bus primaryBus = new Bus("BUS-V1", "REG-TX-777", "Volvo Premium Transit", primarySeatMap);
        primaryBus.setAssignedDriver(driverSam);
        driverSam.setAssignedBusId(primaryBus.getBusId());

        // Add features & photos to primary bus
        primaryBus.addFeature(BusFeature.AC);
        primaryBus.addFeature(BusFeature.WIFI);
        primaryBus.addFeature(BusFeature.CHARGING_PORTS);
        primaryBus.addFeature(BusFeature.RECLINING_SEATS);
        primaryBus.addPhotoUrl("https://images.transitservices.com/bus-v1-exterior.jpg");
        primaryBus.addPhotoUrl("https://images.transitservices.com/bus-v1-interior.jpg");

        busRepo.save(primaryBus);
        System.out.println("Bus " + primaryBus.getBusId() + " (" + primaryBus.getRegistrationNumber()
                + ") loaded with features: " + primaryBus.getFeatures());
        System.out.println("Vehicle images: " + primaryBus.getPhotos());
        System.out.println("Assigned driver: " + primaryBus.getAssignedDriver().getName() + " (Safety Rating: "
                + primaryBus.getAssignedDriver().getSafetyRating() + "/5.0)\n");

        // -------------------------------------------------------------
        // PHASE 3: Route & Rest Stops Setup
        // -------------------------------------------------------------
        System.out.println(">>> Phase 3: Travel Routes & Smart Food Stops Configuration");

        Route expressRoute = new Route("RT-KOC-TRV", "Kochi", "Thiruvananthapuram", 300.0);

        // Creating RestStops (Food Stops) along the route
        RestStop highwayStopA = new RestStop("STP-01", "Highway Diner & Oasis", 4.6);
        highwayStopA.addDietaryOption(DietaryOption.VEG);
        highwayStopA.addDietaryOption(DietaryOption.NON_VEG);
        highwayStopA.addPassengerReview("Clean washrooms, fast service.");
        highwayStopA.addPassengerReview("Amazing veg burgers!");

        RestStop highwayStopB = new RestStop("STP-02", "Green Valley Jain Retreat", 4.9);
        highwayStopB.addDietaryOption(DietaryOption.VEG);
        highwayStopB.addDietaryOption(DietaryOption.JAIN);
        highwayStopB.addPassengerReview("Pure veg, calm environment. Extremely hygienic.");

        expressRoute.addRestStop(highwayStopA);
        expressRoute.addRestStop(highwayStopB);

        System.out.println("Route NYC to Boston (Distance: " + expressRoute.getDistanceKm() + " km) created.");
        System.out.println("Planned food/rest stops along route:");
        for (RestStop stop : expressRoute.getRestStops()) {
            System.out.println(" - Stop Name: " + stop.getName() + " | Hygiene Rating: " + stop.getHygieneRating()
                    + "/5.0 | Dietaries: " + stop.getDietaryOptions());
            System.out.println("   Passenger Reviews: " + stop.getPassengerReviews());
        }
        System.out.println();

        // -------------------------------------------------------------
        // PHASE 4: Schedule Registration
        // -------------------------------------------------------------
        System.out.println(">>> Phase 4: Trip Scheduling");

        // Define departure time as tomorrow morning
        LocalDateTime departureTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        LocalDateTime arrivalTime = departureTime.plusHours(5);
        Schedule mainSchedule = new Schedule("SCH-1001", primaryBus, expressRoute, departureTime, arrivalTime, 50.00);
        scheduleRepo.save(mainSchedule);

        System.out.println("Schedule ID: " + mainSchedule.getScheduleId() + " registered for Bus "
                + mainSchedule.getBus().getBusId());
        System.out.println("Departure: " + mainSchedule.getDepartureTime() + " | Arrival: "
                + mainSchedule.getArrivalTime() + " | Base Price: $" + mainSchedule.getBaseFare());
        System.out.println("Current bus seat layout preview:");
        System.out.println(primaryBus.getSeatMap().displayLayout());

        // -------------------------------------------------------------
        // PHASE 5: Concurrency-Safe Booking Demo
        // -------------------------------------------------------------
        System.out.println(">>> Phase 5: Concurrency-Safe Seat Locking & Booking");

        // Alice locks Seat 1A
        System.out.println("Alice attempts to lock seat 1A...");
        boolean aliceLocked = reservationService.lockSeat("SCH-1001", "1A", "USR-PL-101");
        System.out.println("Alice locked seat 1A: " + aliceLocked);

        // Bob attempts to double-book Seat 1A (Should fail)
        System.out.println("\nBob attempts to lock seat 1A concurrently...");
        try {
            reservationService.lockSeat("SCH-1001", "1A", "USR-PL-102");
            System.out.println("Warning: Bob locked seat 1A successfully (should not happen!)");
        } catch (SeatAlreadyLockedException ex) {
            System.out.println("Success [Catch Exception]: " + ex.getMessage());
        }

        // Alice confirms her booking with Payment (UPI method)
        System.out.println("\nAlice proceeds to check out and pay for Seat 1A using UPI...");
        Booking aliceBooking = reservationService.confirmBooking("USR-PL-101", "SCH-1001", "1A", PaymentMethod.UPI,
                "alice@okhdfcbank");
        System.out.println("Alice booking confirmed! ID: " + aliceBooking.getBookingId());
        System.out.println("Transaction status: " + aliceBooking.getStatus() + " | TX-ID: "
                + aliceBooking.getPaymentTransactionId() + " | Ticket price (with seat multiplier): $"
                + aliceBooking.getFinalPrice());
        System.out.println("Alice earned reward points! Current reward balance: " + passengerAlice.getRewardPoints()
                + " points\n");

        System.out.println("Seat layout review after Alice's successful booking (Seat 1A is now BOOKED):");
        System.out.println(primaryBus.getSeatMap().displayLayout());

        // -------------------------------------------------------------
        // PHASE 6: Delay Prediction Engine (Strategy Pattern)
        // -------------------------------------------------------------
        System.out.println(">>> Phase 6: Delay Prediction (Strategy Pattern)");

        System.out.println("Default Strategy: HistoricalTrafficDelayStrategy");
        double rushHourFactor = 2.5; // Heavy traffic rush
        int delayHist = delayPredictionService.predictDelayMinutes(expressRoute, rushHourFactor, "CLEAR");
        System.out
                .println(" -> Under rush traffic (Factor 2.5) historical prediction delay: " + delayHist + " minutes");

        System.out.println("\nDynamic Context Shift: LiveWeatherCheckpointDelayStrategy");
        delayPredictionService.setStrategy(new LiveWeatherCheckpointDelayStrategy());
        int delayWeather = delayPredictionService.predictDelayMinutes(expressRoute, 1.5, "STORMY");
        System.out.println(
                " -> Under STORMY weather and moderate checkpoints, predicted delay: " + delayWeather + " minutes\n");

        // -------------------------------------------------------------
        // PHASE 7: ComfortIndex rating feedback
        // -------------------------------------------------------------
        System.out.println(">>> Phase 7: Comfort Index Rating Engine (Security Check & Calculation)");

        // Bob attempts to rate schedule SCH-1001 without bookings (should fail)
        System.out.println("Bob (Who has no booking) attempts to rate the Comfort Index of the trip...");
        try {
            comfortIndexService.submitComfortRating("USR-PL-102", "SCH-1001", 5.0, 5.0, 5.0);
        } catch (SecurityException ex) {
            System.out.println("Success [Catch Exception]: " + ex.getMessage());
        }

        // Alice rates the trip (AC: 4.0, spacing: 4.5, cleanliness: 5.0)
        System.out.println("\nAlice (Verified passenger) rates the trip details...");
        ComfortRating aliceRating = comfortIndexService.submitComfortRating("USR-PL-101", "SCH-1001", 4.0, 4.5, 5.0);
        System.out
                .println("Rating submitted! Aggregated user comfort factor: " + aliceRating.getComfortScore() + "/5.0");
        System.out.println("Bus " + primaryBus.getBusId() + " current aggregated ComfortScore index: "
                + primaryBus.getComfortScore() + "/5.0\n");

        // -------------------------------------------------------------
        // PHASE 8: Trip Memory Gallery (Passenger Photo Stream)
        // -------------------------------------------------------------
        System.out.println(">>> Phase 8: Trip Memory Gallery Uploads");

        // To upload a photo, the trip must have commenced/completed. We simulate this
        // by changing departureTime to 2 hours ago.
        System.out.println("Simulating active travel: setting Schedule departure time to 2 hours ago...");
        mainSchedule.setDepartureTime(LocalDateTime.now().minusHours(2));

        System.out.println("Alice uploads a trip photo to the memory stream...");
        TripPhoto photo = tripGalleryService.uploadTripPhoto("USR-PL-101", "SCH-1001", "Scenic view on route!",
                "https://images.tripstream.com/ny-bos-bridge.jpg");
        System.out.println("Photo uploaded! ID: " + photo.getPhotoId() + " | Caption: \"" + photo.getCaption()
                + "\" | URL: " + photo.getPhotoUrl());

        // Like photo
        photo.like();
        System.out.println("Photo like count updated: " + photo.getLikeCount());

        List<TripPhoto> gallery = tripGalleryService.getTripGallery("SCH-1001");
        System.out.println("Retrieved trip photo stream for Schedule SCH-1001: " + gallery.size() + " photo(s)\n");

        // -------------------------------------------------------------
        // PHASE 9: Auto-Rescue Booking Simulation (Breakdown)
        // -------------------------------------------------------------
        System.out.println("==================================================================================");
        System.out.println(">>> Phase 9: Emergency Auto-Rescue Simulation (Vehicle Breakdown)");
        System.out.println("==================================================================================");

        // Bob books seat 1B on primary bus (so we have multiple passengers on the
        // broken bus)
        System.out.println("Bob locks and books seat 1B on schedule SCH-1001 in preparation for trip...");
        reservationService.lockSeat("SCH-1001", "1B", "USR-PL-102");
        Booking bobBooking = reservationService.confirmBooking("USR-PL-102", "SCH-1001", "1B", PaymentMethod.CARD,
                "4000123456789010");
        System.out.println("Bob booking confirmed matching booking ID: " + bobBooking.getBookingId());

        // Create alternate Bus & Schedule on the same route NYC-BOS to serve as the
        // rescue bus.
        List<Seat> rescueSeats = new ArrayList<>();
        for (int row = 1; row <= 5; row++) {
            rescueSeats.add(new Seat(row + "A", SeatType.WINDOW, 1.15, row, 0, "window-left"));
            rescueSeats.add(new Seat(row + "B", SeatType.AISLE, 1.0, row, 1, "aisle-left"));
            rescueSeats.add(new Seat(row + "C", SeatType.AISLE, 1.0, row, 2, "aisle-right"));
            rescueSeats.add(new Seat(row + "D", SeatType.WINDOW, 1.15, row, 3, "window-right"));
        }
        SeatMap rescueSeatMap = new SeatMap(rescueSeats);
        Bus rescueBus = new Bus("BUS-RESCUE-9", "REG-EMERGENCY-1", "Volvo Backup Express", rescueSeatMap);
        busRepo.save(rescueBus);

        // Schedule alternate trip leaving tomorrow (same route, empty seats)
        Schedule alternateSchedule = new Schedule("SCH-1002-RESCUE", rescueBus, expressRoute,
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(15).withMinute(0),
                55.00);
        scheduleRepo.save(alternateSchedule);

        System.out.println("\nEmergency rescue alternate Schedule registered: " + alternateSchedule.getScheduleId());
        System.out.println("Alternate Bus: " + rescueBus.getBusId() + " | Free seats count: "
                + rescueBus.getSeatMap().getAvailableCount());

        System.out.println("\n*** CRITICAL SIGNAL: Primary Bus (BUS-V1) breakdowns down during transit! ***");
        System.out.println("Triggering AutoRescueService for all confirmed passenger bookings on Schedule SCH-1001...");

        // Trigger Auto-Rescue
        List<Booking> rescuedBookingsList = autoRescueService.triggerRescue("SCH-1001");

        System.out.println("\nVerification checkups after Auto-Rescue Execution:");
        System.out.println("Total new rescue bookings generated: " + rescuedBookingsList.size());

        // Verify Alice's old booking status
        Booking updatedAliceBooking = bookingRepo.findById(aliceBooking.getBookingId()).orElseThrow();
        System.out.println(
                "Alice old booking (" + aliceBooking.getBookingId() + ") status: " + updatedAliceBooking.getStatus()
                        + " | Rescued to new booking ID: " + updatedAliceBooking.getRescuedToBookingId());

        // Verify Bob's old booking status
        Booking updatedBobBooking = bookingRepo.findById(bobBooking.getBookingId()).orElseThrow();
        System.out
                .println("Bob old booking (" + bobBooking.getBookingId() + ") status: " + updatedBobBooking.getStatus()
                        + " | Rescued to new booking ID: " + updatedBobBooking.getRescuedToBookingId());

        // Verify that the alternate bus has passengers checked in
        System.out.println("\nRescue alternate bus seat map preview (Seats are now BOOKED by rescued passengers):");
        System.out.println(rescueBus.getSeatMap().displayLayout());

        System.out.println("Alice notification inbox alerts history: " + passengerAlice.getNotifications());
        System.out.println("Bob notification inbox alerts history: " + passengerBob.getNotifications());

        System.out.println("\n==================================================================================");
        System.out.println("   SIMULATION COMPLETED SUCCESSFULLY - ALL DESIGN PATTERNS VALIDATED             ");
        System.out.println("==================================================================================");
    }
}
