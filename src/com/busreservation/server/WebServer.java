package com.busreservation.server;

import com.busreservation.exception.*;
import com.busreservation.model.*;
import com.busreservation.repository.*;
import com.busreservation.service.*;
import com.busreservation.service.delay.*;
import com.busreservation.service.payment.*;
import com.busreservation.service.rescue.*;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer {
    private static final int PORT = 8080;

    // Repositories
    private Repository<Passenger, String> passengerRepo;
    private Repository<Driver, String> driverRepo;
    private Repository<Admin, String> adminRepo;
    private Repository<Bus, String> busRepo;
    private Repository<Schedule, String> scheduleRepo;
    private Repository<Booking, String> bookingRepo;
    private Repository<TripPhoto, String> photoRepo;

    // Services
    private ReservationService reservationService;
    private ComfortIndexService comfortIndexService;
    private DelayPredictionService delayPredictionService;
    private AutoRescueService autoRescueService;
    private TripGalleryService tripGalleryService;

    public static void main(String[] args) {
        try {
            WebServer server = new WebServer();
            server.start();
        } catch (Exception e) {
            System.err.println("Fatal error starting WebServer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public WebServer() {
        resetState();
    }

    private synchronized void resetState() {
        // Initialize OOD repos
        this.passengerRepo = new InMemoryRepository<>(User::getUserId);
        this.driverRepo = new InMemoryRepository<>(User::getUserId);
        this.adminRepo = new InMemoryRepository<>(User::getUserId);
        this.busRepo = new InMemoryRepository<>(Bus::getBusId);
        this.scheduleRepo = new InMemoryRepository<>(Schedule::getScheduleId);
        this.bookingRepo = new InMemoryRepository<>(Booking::getBookingId);
        this.photoRepo = new InMemoryRepository<>(TripPhoto::getPhotoId);

        // Initialize Services
        this.reservationService = new ReservationService(bookingRepo, scheduleRepo, passengerRepo);
        this.comfortIndexService = new ComfortIndexService(passengerRepo, scheduleRepo, bookingRepo);
        this.delayPredictionService = new DelayPredictionService(new HistoricalTrafficDelayStrategy());
        this.autoRescueService = new AutoRescueService(bookingRepo, scheduleRepo, passengerRepo);
        this.tripGalleryService = new TripGalleryService(passengerRepo, scheduleRepo, bookingRepo, photoRepo);

        // Seed the system with Kerala-based users, drivers, routes, and fleet data.
        Admin adminAbhay = UserFactory.createAdmin("USR-AD-301", "Abhay C Manoj", "abhay@example.com",
            "+91-98765-43299", "abhayAdmin123", "SUPER_ADMIN");
        Driver driverSam = UserFactory.createDriver("USR-DR-201", "Sam Miller", "sam@example.com", "+91-98765-43212",
            "driverSam99", "DL-982361B", 8, "+91-98765-43212");
        Passenger passengerDevapriya = UserFactory.createPassenger("USR-PL-103", "Devapriya K C",
            "devapriya@example.com", "+91-98470-11223", "devapriya123");
        Passenger passengerSreedev = UserFactory.createPassenger("USR-PL-104", "Sreedev S Sreejith",
            "sreedev@example.com", "+91-90721-33445", "sreedev123");
        Passenger passengerFathima = UserFactory.createPassenger("USR-PL-105", "Fathima Rena",
            "fathima@example.com", "+91-98952-55667", "fathima123");
        Driver driverArun = UserFactory.createDriver("USR-DR-202", "Arun Kumar Nair", "arun.nair@example.com",
            "+91-94470-22110", "driverArun88", "KL-07-20161234567", 12, "+91-94470-22110");
        Driver driverVishnu = UserFactory.createDriver("USR-DR-203", "Vishnu Prasad Menon", "vishnu.menon@example.com",
            "+91-97465-44122", "driverVishnu91", "KL-01-20181456789", 9, "+91-97465-44122");
        Driver driverAnil = UserFactory.createDriver("USR-DR-204", "Anil Raj Kumar", "anil.kumar@example.com",
            "+91-86061-77889", "driverAnil86", "KL-11-20091345678", 15, "+91-86061-77889");
        BusOperator operatorVolvo = UserFactory.createBusOperator("USR-OP-401", "Volvo Express LLC",
            "volvo@example.com", "+91-98765-43214", "expressVolvo", "Volvo Premium Fleet");

        adminRepo.save(adminAbhay);
        driverRepo.save(driverSam);
        driverRepo.save(driverArun);
        driverRepo.save(driverVishnu);
        driverRepo.save(driverAnil);
        passengerRepo.save(passengerDevapriya);
        passengerRepo.save(passengerSreedev);
        passengerRepo.save(passengerFathima);

        // Set up seat layouts - 2+2 configuration (40 seats, 10 rows)
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

        primaryBus.addFeature(BusFeature.AC);
        primaryBus.addFeature(BusFeature.WIFI);
        primaryBus.addFeature(BusFeature.CHARGING_PORTS);
        primaryBus.addFeature(BusFeature.RECLINING_SEATS);
        primaryBus.addPhotoUrl(
                "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=800");
        primaryBus.addPhotoUrl(
                "https://images.unsplash.com/photo-1570125909232-eb263c188f7e?auto=format&fit=crop&q=80&w=800");

        busRepo.save(primaryBus);

        Bus coastalBus = new Bus("BUS-KL-02", "KL-15-A-7788", "Ashok Leyland Coastal Rider",
            new SeatMap(createSeats(8)));
        coastalBus.setAssignedDriver(driverArun);
        driverArun.setAssignedBusId(coastalBus.getBusId());
        coastalBus.addFeature(BusFeature.AC);
        coastalBus.addFeature(BusFeature.WIFI);
        coastalBus.addFeature(BusFeature.CHARGING_PORTS);
        busRepo.save(coastalBus);

        Bus hillBus = new Bus("BUS-KL-03", "KL-07-B-4499", "Tata Starbus Hill Connect",
            new SeatMap(createSeats(7)));
        hillBus.setAssignedDriver(driverVishnu);
        driverVishnu.setAssignedBusId(hillBus.getBusId());
        hillBus.addFeature(BusFeature.AC);
        hillBus.addFeature(BusFeature.RECLINING_SEATS);
        busRepo.save(hillBus);

        Bus northBus = new Bus("BUS-KL-04", "KL-11-C-8833", "Eicher Skyline North Express",
            new SeatMap(createSeats(9)));
        northBus.setAssignedDriver(driverAnil);
        driverAnil.setAssignedBusId(northBus.getBusId());
        northBus.addFeature(BusFeature.AC);
        northBus.addFeature(BusFeature.WIFI);
        northBus.addFeature(BusFeature.RECLINING_SEATS);
        busRepo.save(northBus);

        // Routes & Rest Stops Setup
        Route expressRoute = new Route("RT-KOC-TRV", "Kochi", "Thiruvananthapuram", 300.0);
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

        // Schedule Setup (Main Trip)
        LocalDateTime departureTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        LocalDateTime arrivalTime = departureTime.plusHours(5);
        Schedule mainSchedule = new Schedule("SCH-1001", primaryBus, expressRoute, departureTime, arrivalTime, 50.00);
        scheduleRepo.save(mainSchedule);

        // Support backup/rescue setup with smaller bus (5 rows = 20 seats)
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

        Schedule alternateSchedule = new Schedule("SCH-1002-RESCUE", rescueBus, expressRoute,
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(15).withMinute(0), 55.00);
        scheduleRepo.save(alternateSchedule);

        Route kochiKozhikode = new Route("RT-KOC-KZD", "Kochi", "Kozhikode", 185.0);
        Route kochiMunnar = new Route("RT-KOC-MNR", "Kochi", "Munnar", 130.0);
        Route kochiKannur = new Route("RT-KOC-KNR", "Kochi", "Kannur", 275.0);

        LocalDateTime nextDay = LocalDateTime.now().plusDays(1);
        scheduleRepo.save(new Schedule("SCH-1003", coastalBus, kochiKozhikode,
            nextDay.withHour(7).withMinute(30), nextDay.withHour(12).withMinute(15), 420.00));
        scheduleRepo.save(new Schedule("SCH-1004", hillBus, kochiMunnar,
            nextDay.withHour(9).withMinute(0), nextDay.withHour(13).withMinute(0), 350.00));
        scheduleRepo.save(new Schedule("SCH-1005", northBus, kochiKannur,
            nextDay.withHour(6).withMinute(45), nextDay.withHour(14).withMinute(0), 520.00));
    }

        private List<Seat> createSeats(int rows) {
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= rows; row++) {
            seats.add(new Seat(row + "A", SeatType.WINDOW, 1.15, row, 0, "window-left"));
            seats.add(new Seat(row + "B", SeatType.AISLE, 1.0, row, 1, "aisle-left"));
            seats.add(new Seat(row + "C", SeatType.AISLE, 1.0, row, 2, "aisle-right"));
            seats.add(new Seat(row + "D", SeatType.WINDOW, 1.15, row, 3, "window-right"));
        }
        return seats;
        }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/state", new StateHandler());
        server.createContext("/api/lock", new LockHandler());
        server.createContext("/api/book", new BookHandler());
        server.createContext("/api/predict", new PredictHandler());
        server.createContext("/api/rate", new RateHandler());
        server.createContext("/api/upload", new UploadHandler());
        server.createContext("/api/like", new LikeHandler());
        server.createContext("/api/rescue", new RescueHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/remove-user", new RemoveUserHandler());
        server.createContext("/api/reset", new ResetHandler());

        server.setExecutor(null); // default executor
        System.out.println("=========================================================");
        System.out.println("   Bus Reservation Platform Hosted Locally on Port " + PORT);
        System.out.println("   Open http://localhost:" + PORT + "/ in your browser");
        System.out.println("=========================================================");
        server.start();
    }

    // Static Server Handler serving from project 'web/' folder
    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pathStr = exchange.getRequestURI().getPath();
            if (pathStr.equals("/")) {
                pathStr = "/index.html";
            }

            // Clean path to prevent path traversal vulnerability (serve only from web/)
            pathStr = pathStr.replace("..", "").replace("\\", "/");
            Path targetFile = Paths.get("web", pathStr);

            if (!Files.exists(targetFile) || Files.isDirectory(targetFile)) {
                sendError(exchange, 404, "File not found: " + pathStr);
                return;
            }

            byte[] content = Files.readAllBytes(targetFile);
            String contentType = "text/plain";
            if (pathStr.endsWith(".html"))
                contentType = "text/html; charset=UTF-8";
            else if (pathStr.endsWith(".css"))
                contentType = "text/css; charset=UTF-8";
            else if (pathStr.endsWith(".js"))
                contentType = "application/javascript; charset=UTF-8";
            else if (pathStr.endsWith(".png"))
                contentType = "image/png";
            else if (pathStr.endsWith(".jpg") || pathStr.endsWith(".jpeg"))
                contentType = "image/jpeg";
            else if (pathStr.endsWith(".svg"))
                contentType = "image/svg+xml";

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }
    }

    // STATE API Handler
    private class StateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String response = getSystemStateJson();
                sendJson(exchange, response);
            } catch (Exception e) {
                sendError(exchange, 500, "Error getting system state: " + e.getMessage());
            }
        }
    }

    // LOCK SEAT API Handler
    private class LockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> bodyMap = parseJson(body);

                String scheduleId = bodyMap.get("scheduleId");
                String seatNumber = bodyMap.get("seatNumber");
                String userId = bodyMap.get("userId");

                if (scheduleId == null || seatNumber == null || userId == null) {
                    sendJsonError(exchange, "Missing parameters (scheduleId, seatNumber, or userId)");
                    return;
                }

                boolean locked = reservationService.lockSeat(scheduleId, seatNumber, userId);
                if (locked) {
                    sendJsonSuccess(exchange, "{\"success\":true,\"message\":\"Seat " + seatNumber
                            + " locked temporarily for 5 mins.\"}");
                } else {
                    sendJsonError(exchange, "Failed to lock seat: seat details match or lock error occurred.");
                }
            } catch (SeatAlreadyLockedException e) {
                sendJsonError(exchange, e.getMessage());
            } catch (Exception e) {
                sendJsonError(exchange, "Error locking seat: " + e.getMessage());
            }
        }
    }

    // CONFIRM BOOKING API Handler
    private class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> bodyMap = parseJson(body);

                String userId = bodyMap.get("userId");
                String scheduleId = bodyMap.get("scheduleId");
                String seatNumber = bodyMap.get("seatNumber");
                String paymentMethodStr = bodyMap.get("paymentMethod");
                String paymentDetail = bodyMap.get("paymentDetail");

                if (userId == null || scheduleId == null || seatNumber == null || paymentMethodStr == null) {
                    sendJsonError(exchange, "Missing parameters (userId, scheduleId, seatNumber, or paymentMethod)");
                    return;
                }

                PaymentMethod method;
                try {
                    method = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    sendJsonError(exchange, "Invalid payment method. Use UPI, CARD, or WALLET");
                    return;
                }

                Booking booking = reservationService.confirmBooking(userId, scheduleId, seatNumber, method,
                        paymentDetail);

                String rJson = "{" +
                        "\"success\":true," +
                        "\"bookingId\":\"" + booking.getBookingId() + "\"," +
                        "\"finalPrice\":" + booking.getFinalPrice() + "," +
                        "\"paymentTransactionId\":\"" + booking.getPaymentTransactionId() + "\"," +
                        "\"status\":\"" + booking.getStatus() + "\"" +
                        "}";
                sendJsonSuccess(exchange, rJson);
            } catch (BookingException e) {
                sendJsonError(exchange, e.getMessage());
            } catch (Exception e) {
                sendJsonError(exchange, "Booking failed: " + e.getMessage());
            }
        }
    }

    // DELAY PREDICTION API Handler
    private class PredictHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = parseQuery(query);

                String routeId = params.get("routeId");
                String trafficStr = params.get("trafficFactor");
                String weather = params.get("weather");
                String strategyStr = params.get("strategy"); // historical or weather

                if (routeId == null || trafficStr == null || weather == null) {
                    sendJsonError(exchange, "Missing query params (routeId, trafficFactor, weather)");
                    return;
                }

                double trafficFactor = Double.parseDouble(trafficStr);

                // Set Strategy dynamically (pattern)
                if ("weather".equalsIgnoreCase(strategyStr)) {
                    delayPredictionService.setStrategy(new LiveWeatherCheckpointDelayStrategy());
                } else {
                    delayPredictionService.setStrategy(new HistoricalTrafficDelayStrategy());
                }

                Route targetRoute = null;
                for (Schedule s : scheduleRepo.findAll()) {
                    if (s.getRoute().getRouteId().equals(routeId)) {
                        targetRoute = s.getRoute();
                        break;
                    }
                }

                if (targetRoute == null) {
                    // Fallback to route search or default one
                    targetRoute = new Route(routeId, "New York", "Boston", 350.0);
                }

                int delayMins = delayPredictionService.predictDelayMinutes(targetRoute, trafficFactor, weather);
                String activeStrategyName = "weather".equalsIgnoreCase(strategyStr) ? "Live Weather Checkpoint Strategy"
                        : "Historical Traffic Strategy";

                String resJson = "{" +
                        "\"success\":true," +
                        "\"predictedDelay\":" + delayMins + "," +
                        "\"strategyUsed\":\"" + activeStrategyName + "\"" +
                        "}";
                sendJsonSuccess(exchange, resJson);
            } catch (Exception e) {
                sendJsonError(exchange, "Prediction calculation failed: " + e.getMessage());
            }
        }
    }

    // SUBMIT COMFORT RATING API Handler
    private class RateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> bodyMap = parseJson(body);

                String userId = bodyMap.get("userId");
                String scheduleId = bodyMap.get("scheduleId");
                String acScoreStr = bodyMap.get("acRating");
                String spacingScoreStr = bodyMap.get("seatSpacingRating");
                String cleanlinessScoreStr = bodyMap.get("cleanlinessRating");

                if (userId == null || scheduleId == null || acScoreStr == null || spacingScoreStr == null
                        || cleanlinessScoreStr == null) {
                    sendJsonError(exchange,
                            "Missing parameters (userId, scheduleId, acRating, seatSpacingRating, cleanlinessRating)");
                    return;
                }

                double ac = Double.parseDouble(acScoreStr);
                double spacing = Double.parseDouble(spacingScoreStr);
                double cleanliness = Double.parseDouble(cleanlinessScoreStr);

                ComfortRating comfortRating = comfortIndexService.submitComfortRating(userId, scheduleId, ac, spacing,
                        cleanliness);

                String rJson = "{" +
                        "\"success\":true," +
                        "\"comfortScore\":" + comfortRating.getComfortScore() + "," +
                        "\"ratingId\":\"" + comfortRating.getRatingId() + "\"," +
                        "\"acRating\":" + comfortRating.getAcRating() + "," +
                        "\"seatSpacingRating\":" + comfortRating.getSeatSpacingRating() + "," +
                        "\"cleanlinessRating\":" + comfortRating.getCleanlinessRating() +
                        "}";
                sendJsonSuccess(exchange, rJson);
            } catch (InvalidRatingException e) {
                sendJsonError(exchange, e.getMessage());
            } catch (SecurityException e) {
                sendJsonError(exchange, "Access denied: " + e.getMessage());
            } catch (Exception e) {
                sendJsonError(exchange, "Rating submission failed: " + e.getMessage());
            }
        }
    }

    // PHOTO UPLOAD API Handler
    private class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> bodyMap = parseJson(body);

                String userId = bodyMap.get("userId");
                String scheduleId = bodyMap.get("scheduleId");
                String caption = bodyMap.get("caption");
                String url = bodyMap.get("url");

                if (userId == null || scheduleId == null || url == null) {
                    sendJsonError(exchange, "Missing parameters (userId, scheduleId, or url)");
                    return;
                }

                // Simulate trip completion if it hasn't commenced, to allow photo upload
                Schedule sch = scheduleRepo.findById(scheduleId).orElse(null);
                if (sch != null && sch.getDepartureTime().isAfter(LocalDateTime.now())) {
                    // Set departure in past to simulate trip active/completed
                    sch.setDepartureTime(LocalDateTime.now().minusHours(2));
                }

                TripPhoto photo = tripGalleryService.uploadTripPhoto(userId, scheduleId, caption, url);

                String rJson = "{" +
                        "\"success\":true," +
                        "\"photoId\":\"" + photo.getPhotoId() + "\"," +
                        "\"caption\":\"" + escapeJson(photo.getCaption()) + "\"," +
                        "\"photoUrl\":\"" + escapeJson(photo.getPhotoUrl()) + "\"," +
                        "\"likeCount\":" + photo.getLikeCount() +
                        "}";
                sendJsonSuccess(exchange, rJson);
            } catch (Exception e) {
                sendJsonError(exchange, "Photo upload failed: " + e.getMessage());
            }
        }
    }

    // PHOTO LIKE API Handler
    private class LikeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> bodyMap = parseJson(body);
                String photoId = bodyMap.get("photoId");

                if (photoId == null) {
                    sendJsonError(exchange, "Missing parameter: photoId");
                    return;
                }

                Optional<TripPhoto> photoOpt = photoRepo.findById(photoId);
                if (photoOpt.isPresent()) {
                    TripPhoto photo = photoOpt.get();
                    photo.like();
                    sendJsonSuccess(exchange, "{\"success\":true,\"likeCount\":" + photo.getLikeCount() + "}");
                } else {
                    sendJsonError(exchange, "Photo not found: " + photoId);
                }
            } catch (Exception e) {
                sendJsonError(exchange, "Error liking photo: " + e.getMessage());
            }
        }
    }

    // BREAKDOWN AUTO-RESCUE API Handler
    private class RescueHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> bodyMap = parseJson(body);
                String scheduleId = bodyMap.get("scheduleId");

                if (scheduleId == null) {
                    sendJsonError(exchange, "Missing parameter: scheduleId");
                    return;
                }

                List<Booking> rescued = autoRescueService.triggerRescue(scheduleId);
                StringBuilder sb = new StringBuilder();
                sb.append("{\"success\":true,\"rescuedCount\":").append(rescued.size()).append(",\"bookings\":[");
                for (int i = 0; i < rescued.size(); i++) {
                    Booking bk = rescued.get(i);
                    sb.append("{");
                    sb.append("\"bookingId\":\"").append(bk.getBookingId()).append("\",");
                    sb.append("\"passengerId\":\"").append(bk.getPassengerId()).append("\",");
                    sb.append("\"seatNumber\":\"").append(bk.getSeatNumber()).append("\",");
                    sb.append("\"newBookingId\":\"").append(bk.getRescuedToBookingId()).append("\"");
                    sb.append("}");
                    if (i < rescued.size() - 1)
                        sb.append(",");
                }
                sb.append("]}");

                sendJsonSuccess(exchange, sb.toString());
            } catch (AutoRescueFailureException e) {
                sendJsonError(exchange, e.getMessage());
            } catch (Exception e) {
                sendJsonError(exchange, "Rescue execution failed: " + e.getMessage());
            }
        }
    }

    // REGISTER NEW USER / STAFF API Handler
    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> bodyMap = parseJson(body);

                String type = bodyMap.get("type"); // passenger or driver
                String userId = bodyMap.get("userId");
                String name = bodyMap.get("name");
                String email = bodyMap.get("email");
                String phone = bodyMap.get("phone");
                String credentials = bodyMap.get("credentials");

                if (type == null || userId == null || name == null || email == null || phone == null
                        || credentials == null) {
                    sendJsonError(exchange, "Missing parameters for registration");
                    return;
                }

                if ("passenger".equalsIgnoreCase(type)) {
                    Passenger p = UserFactory.createPassenger(userId, name, email, phone, credentials);
                    passengerRepo.save(p);
                    sendJsonSuccess(exchange,
                            "{\"success\":true,\"message\":\"Passenger registered successfully!\",\"userId\":\""
                                    + userId + "\"}");
                } else if ("driver".equalsIgnoreCase(type)) {
                    String license = bodyMap.getOrDefault("license", "LIC-NEW-" + System.currentTimeMillis());
                    int exp = Integer.parseInt(bodyMap.getOrDefault("experience", "3"));
                    Driver d = UserFactory.createDriver(userId, name, email, phone, credentials, license, exp, phone);
                    driverRepo.save(d);
                    sendJsonSuccess(exchange,
                            "{\"success\":true,\"message\":\"Driver registered successfully!\",\"userId\":\"" + userId
                                    + "\"}");
                } else {
                    sendJsonError(exchange, "Invalid user type. Must be 'passenger' or 'driver'.");
                }
            } catch (Exception e) {
                sendJsonError(exchange, "Registration failed: " + e.getMessage());
            }
        }
    }

    // REMOVE USER API Handler (admin only)
    private class RemoveUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> bodyMap = parseJson(body);
                String adminUserId = bodyMap.get("adminUserId");
                String targetUserId = bodyMap.get("targetUserId");

                if (adminUserId == null || targetUserId == null) {
                    sendJsonError(exchange, "Missing adminUserId or targetUserId");
                    return;
                }

                if (!adminRepo.findById(adminUserId).isPresent()) {
                    sendJsonError(exchange, "Only admins can remove users");
                    return;
                }

                if (adminUserId.equals(targetUserId)) {
                    sendJsonError(exchange, "Admin account cannot be removed");
                    return;
                }

                boolean removed = false;
                if (passengerRepo.findById(targetUserId).isPresent()) {
                    passengerRepo.deleteById(targetUserId);
                    removed = true;
                } else if (driverRepo.findById(targetUserId).isPresent()) {
                    driverRepo.deleteById(targetUserId);
                    removed = true;
                } else if (adminRepo.findById(targetUserId).isPresent()) {
                    sendJsonError(exchange, "Admin accounts cannot be removed by this action");
                    return;
                }

                if (!removed) {
                    sendJsonError(exchange, "User not found: " + targetUserId);
                    return;
                }

                sendJsonSuccess(exchange,
                        "{\"success\":true,\"message\":\"User removed successfully.\",\"targetUserId\":\""
                                + targetUserId + "\"}");
            } catch (Exception e) {
                sendJsonError(exchange, "User removal failed: " + e.getMessage());
            }
        }
    }

    // RESET STATE API Handler
    private class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                resetState();
                sendJsonSuccess(exchange,
                        "{\"success\":true,\"message\":\"System state reset to simulator baseline scenario.\"}");
            } catch (Exception e) {
                sendJsonError(exchange, "Failed to reset state: " + e.getMessage());
            }
        }
    }

    // Internal Serializer: Constructs a state snapshot representing database values
    // as JSON
    private synchronized String getSystemStateJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // 1. Passengers
        sb.append("\"passengers\":[");
        List<Passenger> passengers = new ArrayList<>(passengerRepo.findAll());
        for (int i = 0; i < passengers.size(); i++) {
            sb.append(passengerToJson(passengers.get(i)));
            if (i < passengers.size() - 1)
                sb.append(",");
        }
        sb.append("],");

        // 2. Drivers
        sb.append("\"drivers\":[");
        List<Driver> drivers = new ArrayList<>(driverRepo.findAll());
        for (int i = 0; i < drivers.size(); i++) {
            sb.append(driverToJson(drivers.get(i)));
            if (i < drivers.size() - 1)
                sb.append(",");
        }
        sb.append("],");

        // 3. Admins
        sb.append("\"admins\":[");
        List<Admin> admins = new ArrayList<>(adminRepo.findAll());
        for (int i = 0; i < admins.size(); i++) {
            sb.append(adminToJson(admins.get(i)));
            if (i < admins.size() - 1)
                sb.append(",");
        }
        sb.append("],");

        // 4. Buses
        sb.append("\"buses\":[");
        List<Bus> buses = new ArrayList<>(busRepo.findAll());
        for (int i = 0; i < buses.size(); i++) {
            sb.append(busToJson(buses.get(i)));
            if (i < buses.size() - 1)
                sb.append(",");
        }
        sb.append("],");

        // 4. Schedules
        sb.append("\"schedules\":[");
        List<Schedule> schedules = new ArrayList<>(scheduleRepo.findAll());
        for (int i = 0; i < schedules.size(); i++) {
            sb.append(scheduleToJson(schedules.get(i)));
            if (i < schedules.size() - 1)
                sb.append(",");
        }
        sb.append("],");

        // 5. Bookings
        sb.append("\"bookings\":[");
        List<Booking> bookings = new ArrayList<>(bookingRepo.findAll());
        for (int i = 0; i < bookings.size(); i++) {
            sb.append(bookingToJson(bookings.get(i)));
            if (i < bookings.size() - 1)
                sb.append(",");
        }
        sb.append("],");

        // 6. Photo stream gallery
        sb.append("\"photos\":[");
        List<TripPhoto> photos = new ArrayList<>(photoRepo.findAll());
        for (int i = 0; i < photos.size(); i++) {
            sb.append(photoToJson(photos.get(i)));
            if (i < photos.size() - 1)
                sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    // --- JSON Serialization Helpers ---

    private static String escapeJson(String s) {
        if (s == null)
            return "null";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String passengerToJson(Passenger p) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"userId\":\"").append(escapeJson(p.getUserId())).append("\",");
        sb.append("\"name\":\"").append(escapeJson(p.getName())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(p.getEmail())).append("\",");
        sb.append("\"phone\":\"").append(escapeJson(p.getPhone())).append("\",");
        sb.append("\"rewardPoints\":").append(p.getRewardPoints()).append(",");
        sb.append("\"notifications\":[");
        List<String> notifs = p.getNotifications();
        for (int i = 0; i < notifs.size(); i++) {
            sb.append("\"").append(escapeJson(notifs.get(i))).append("\"");
            if (i < notifs.size() - 1)
                sb.append(",");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private static String driverToJson(Driver d) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"userId\":\"").append(escapeJson(d.getUserId())).append("\",");
        sb.append("\"name\":\"").append(escapeJson(d.getName())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(d.getEmail())).append("\",");
        sb.append("\"phone\":\"").append(escapeJson(d.getPhone())).append("\",");
        sb.append("\"licenseNumber\":\"").append(escapeJson(d.getLicenseNumber())).append("\",");
        sb.append("\"yearsOfExperience\":").append(d.getYearsOfExperience()).append(",");
        sb.append("\"safetyRating\":").append(d.getSafetyRating());
        sb.append("}");
        return sb.toString();
    }

    private static String adminToJson(Admin a) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"userId\":\"").append(escapeJson(a.getUserId())).append("\",");
        sb.append("\"name\":\"").append(escapeJson(a.getName())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(a.getEmail())).append("\",");
        sb.append("\"phone\":\"").append(escapeJson(a.getPhone())).append("\",");
        sb.append("\"adminLevel\":\"").append(escapeJson(a.getAdminLevel())).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private static String busToJson(Bus b) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"busId\":\"").append(escapeJson(b.getBusId())).append("\",");
        sb.append("\"registrationNumber\":\"").append(escapeJson(b.getRegistrationNumber())).append("\",");
        sb.append("\"operatorName\":\"").append(escapeJson(b.getOperatorName())).append("\",");
        sb.append("\"comfortScore\":").append(b.getComfortScore()).append(",");

        // driver
        Driver dr = b.getAssignedDriver();
        if (dr != null) {
            sb.append("\"driver\":").append(driverToJson(dr)).append(",");
        } else {
            sb.append("\"driver\":null,");
        }

        // features
        sb.append("\"features\":[");
        Set<BusFeature> features = b.getFeatures();
        int idx = 0;
        for (BusFeature f : features) {
            sb.append("\"").append(f.name()).append("\"");
            if (idx++ < features.size() - 1)
                sb.append(",");
        }
        sb.append("],");

        // photos
        sb.append("\"photos\":[");
        List<String> photos = b.getPhotos();
        for (int i = 0; i < photos.size(); i++) {
            sb.append("\"").append(escapeJson(photos.get(i))).append("\"");
            if (i < photos.size() - 1)
                sb.append(",");
        }
        sb.append("],");

        // seat map list
        sb.append("\"seats\":[");
        List<Seat> seats = new ArrayList<>(b.getSeatMap().getAllSeats());
        Instant now = Instant.now();
        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            sb.append("{");
            sb.append("\"seatNumber\":\"").append(escapeJson(s.getSeatNumber())).append("\",");
            sb.append("\"seatType\":\"").append(s.getSeatType().name()).append("\",");
            sb.append("\"status\":\"").append(s.getStatus()).append("\",");
            sb.append("\"lockedByPassengerId\":").append(
                    s.getLockedByPassengerId() == null ? "null" : "\"" + escapeJson(s.getLockedByPassengerId()) + "\"")
                    .append(",");
            sb.append("\"isAvailable\":").append(s.isAvailable(now)).append(",");
            sb.append("\"row\":").append(s.getRow()).append(",");
            sb.append("\"column\":").append(s.getColumn()).append(",");
            sb.append("\"position\":\"").append(escapeJson(s.getPosition())).append("\"");
            sb.append("}");
            if (i < seats.size() - 1)
                sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private static String restStopToJson(RestStop rs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"restStopId\":\"").append(escapeJson(rs.getRestStopId())).append("\",");
        sb.append("\"name\":\"").append(escapeJson(rs.getName())).append("\",");
        sb.append("\"hygieneRating\":").append(rs.getHygieneRating()).append(",");

        // dietary options
        sb.append("\"dietaryOptions\":[");
        Set<DietaryOption> di = rs.getDietaryOptions();
        int idx = 0;
        for (DietaryOption d : di) {
            sb.append("\"").append(d.name()).append("\"");
            if (idx++ < di.size() - 1)
                sb.append(",");
        }
        sb.append("],");

        // reviews
        sb.append("\"passengerReviews\":[");
        List<String> reviews = rs.getPassengerReviews();
        for (int i = 0; i < reviews.size(); i++) {
            sb.append("\"").append(escapeJson(reviews.get(i))).append("\"");
            if (i < reviews.size() - 1)
                sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private static String routeToJson(Route r) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"routeId\":\"").append(escapeJson(r.getRouteId())).append("\",");
        sb.append("\"source\":\"").append(escapeJson(r.getSource())).append("\",");
        sb.append("\"destination\":\"").append(escapeJson(r.getDestination())).append("\",");
        sb.append("\"distanceKm\":").append(r.getDistanceKm()).append(",");

        sb.append("\"restStops\":[");
        List<RestStop> stops = r.getRestStops();
        for (int i = 0; i < stops.size(); i++) {
            sb.append(restStopToJson(stops.get(i)));
            if (i < stops.size() - 1)
                sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private static String scheduleToJson(Schedule s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"scheduleId\":\"").append(escapeJson(s.getScheduleId())).append("\",");
        sb.append("\"busId\":\"").append(escapeJson(s.getBus().getBusId())).append("\",");
        sb.append("\"route\":").append(routeToJson(s.getRoute())).append(",");
        sb.append("\"departureTime\":\"").append(s.getDepartureTime().toString()).append("\",");
        sb.append("\"arrivalTime\":\"").append(s.getArrivalTime().toString()).append("\",");
        sb.append("\"baseFare\":").append(s.getBaseFare());
        sb.append("}");
        return sb.toString();
    }

    private static String bookingToJson(Booking bk) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"bookingId\":\"").append(escapeJson(bk.getBookingId())).append("\",");
        sb.append("\"passengerId\":\"").append(escapeJson(bk.getPassengerId())).append("\",");
        sb.append("\"scheduleId\":\"").append(escapeJson(bk.getScheduleId())).append("\",");
        sb.append("\"seatNumber\":\"").append(escapeJson(bk.getSeatNumber())).append("\",");
        sb.append("\"finalPrice\":").append(bk.getFinalPrice()).append(",");
        sb.append("\"status\":\"").append(bk.getStatus()).append("\",");
        sb.append("\"bookingTime\":\"").append(bk.getBookingTime().toString()).append("\",");
        sb.append("\"paymentTransactionId\":").append(
                bk.getPaymentTransactionId() == null ? "null" : "\"" + escapeJson(bk.getPaymentTransactionId()) + "\"")
                .append(",");
        sb.append("\"rescuedToBookingId\":").append(
                bk.getRescuedToBookingId() == null ? "null" : "\"" + escapeJson(bk.getRescuedToBookingId()) + "\"");
        sb.append("}");
        return sb.toString();
    }

    private static String photoToJson(TripPhoto p) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"photoId\":\"").append(escapeJson(p.getPhotoId())).append("\",");
        sb.append("\"passengerId\":\"").append(escapeJson(p.getPassengerId())).append("\",");
        sb.append("\"tripId\":\"").append(escapeJson(p.getTripId())).append("\",");
        sb.append("\"caption\":\"").append(escapeJson(p.getCaption())).append("\",");
        sb.append("\"photoUrl\":\"").append(escapeJson(p.getPhotoUrl())).append("\",");
        sb.append("\"timestamp\":\"").append(p.getTimestamp().toString()).append("\",");
        sb.append("\"likeCount\":").append(p.getLikeCount());
        sb.append("}");
        return sb.toString();
    }

    // --- Static Utility Helpers for Parsing ---

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toString(StandardCharsets.UTF_8.name());
    }

    private static Map<String, String> parseJson(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty())
            return map;
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,\\]}]+))");
        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {
            String key = matcher.group(1);
            String val = matcher.group(2);
            if (val == null) {
                val = matcher.group(3).trim(); // for unquoted values
            }
            map.put(key, val);
        }
        return map;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty())
            return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            } else if (kv.length == 1) {
                map.put(kv[0], "");
            }
        }
        return map;
    }

    private static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendJsonSuccess(HttpExchange exchange, String json) throws IOException {
        sendJson(exchange, json);
    }

    private static void sendJsonError(HttpExchange exchange, String errorMsg) throws IOException {
        String json = "{\"success\":false,\"error\":\"" + escapeJson(errorMsg) + "\"}";
        sendJson(exchange, json);
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
