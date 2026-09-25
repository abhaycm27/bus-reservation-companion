# TransitFlow: Bus Reservation & Travel Companion Platform

An enterprise-grade, concurrency-safe Java 17 backend combined with a premium, high-fidelity glassmorphic web dashboard that showcases advanced Object-Oriented Design (OOD), design patterns, and interactive companion features.

---

## 🗺️ Architectural Mapping

```text
               ┌─────────────────────────────────────────────────────────┐
               │                  GLASSMORPHIC WEB UI                    │
               │  - User perspective banners      - Prediction strategy  │
               │  - Interactive seat selector     - Photo memory stream  │
               │  - Emergency breakdown dashboard - Notification logs    │
               └──────────────────────────┬──────────────────────────────┘
                                          │ (HTTP / JSON)
                                          ▼
               ┌─────────────────────────────────────────────────────────┐
               │              JAVA HTTP CONTROLLER LAYER                 │
               │         (com.busreservation.server.WebServer)           │
               ├──────────────────────────┬──────────────────────────────┤
               │   API router endpoints   │   Static asset handler       │
               │   (/api/state, /api/lock)│   (Serving HTML/CSS/JS)      │
               └──────────────────────────┼──────────────────────────────┘
                                          │ (Method Invocations)
                                          ▼
 ┌───────────────────────────────────────────────────────────────────────────────────────┐
 │                                   SERVICE LAYER                                       │
 ├─────────────────────────┬───────────────────────────┬─────────────────────────────────┤
 │   ReservationService    │    ComfortIndexService    │     TripGalleryService          │
 │   - Concurrency locking │    - Verified customer    │     - Upload traveler memories  │
 │   - Ticket finalization │      reviews & AC ratings │     - Manage like counts        │
 ├─────────────────────────┼───────────────────────────┴─────────────────────────────────┤
 │  DelayPredictionService │    AutoRescueService (Observer Pattern)                     │
 │  - Swappable strategy   │    - Broadcast breakdowns to passenger inboxes               │
 │    algorithms at runtime│    - Reallocate seats dynamically to Backup Bus              │
 └─────────────────────────┼─────────────────────────────────────────────────────────────┘
                           │ (Generic CRUD queries)
                           ▼
 ┌───────────────────────────────────────────────────────────────────────────────────────┐
 │                                 REPOSITORY LAYER                                      │
 ├───────────────────────────────────────────────────────────────────────────────────────┤
 │  Repository<T, ID> (Generic Data Abstraction Interface)                               │
 │   └── InMemoryRepository<T, ID> (Thread-safe storage utilizing ConcurrentHashMap)     │
 └───────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Design Patterns Implemented

| Pattern | Component | Implementation Rationale |
| :--- | :--- | :--- |
| **Factory Pattern** | `UserFactory` | Centralizes construction rules for polymorphic user models (`Passenger`, `Driver`, `Admin`, `BusOperator`) while enforcing clean inheritance hierarchies. |
| **Strategy Pattern** | `PaymentProcessor` | Supports clean, runtime swapping of payment handling algorithms (`UPIPayment`, `CardPayment`, `WalletPayment`) without mutating reservation orchestrators. |
| **Strategy Pattern** | `DelayPredictionStrategy` | Supports dynamic swaps of delay calculators on routes (`HistoricalTrafficDelayStrategy` vs. `LiveWeatherCheckpointDelayStrategy`) based on live external weather triggers. |
| **Observer Pattern** | `Observer` / `Passenger` | Passengers register as observers to schedules and receive automatic alert notifications in their inboxes when a bus breaks down. |
| **Repository Pattern**| `Repository<T, ID>` | Isolates domain storage details from service layers. Leverages generic abstractions backed by thread-safe Java `ConcurrentHashMap` collections. |

---

## 🔌 API Endpoint Specifications

The local Java server maps core simulation features to neat HTTP REST channels:

| Endpoint | Method | Payload Parameters | Response Structure | Description |
| :--- | :--- | :--- | :--- | :--- |
| `/api/state` | `GET` | *None* | Full JSON document containing passengers, drivers, schedules, bookings, and photos. | Pulls a database snapshot to sync web states dynamically. |
| `/api/lock` | `POST` | `{"userId", "scheduleId", "seatNumber"}` | `{"success": true, "message"}` or error detail. | Locks a bus seat for 5 minutes checking timestamp expirations. |
| `/api/book` | `POST` | `{"userId", "scheduleId", "seatNumber", "paymentMethod", "paymentDetail"}` | `{"success": true, "bookingId", "finalPrice", "paymentTransactionId"}` | Invokes the dynamic payment engine and completes the seat allocation. |
| `/api/predict`| `GET` | `?routeId=...&trafficFactor=...&weather=...&strategy=...` | `{"success": true, "predictedDelay", "strategyUsed"}` | Evaluates delays using either Historical or Weather models. |
| `/api/rate` | `POST` | `{"userId", "scheduleId", "acRating", "seatSpacingRating", "cleanlinessRating"}` | `{"success": true, "comfortScore", "ratingId"}` | Rates trip comfort score (security-restricted to customers with active booking). |
| `/api/upload` | `POST` | `{"userId", "scheduleId", "caption", "url"}` | `{"success": true, "photoId", "caption", "photoUrl"}` | Uploads a traveler photo memory to the public photostream. |
| `/api/like` | `POST` | `{"photoId"}` | `{"success": true, "likeCount"}` | Increments the likes metric of a photo polaroid inside the memory repository. |
| `/api/rescue` | `POST` | `{"scheduleId"}` | `{"success": true, "rescuedCount", "bookings": [...]}` | Simulates vehicle breakdown, broadcasts notifications, and moves passengers. |
| `/api/reset` | `POST` | *None* | `{"success": true, "message"}` | Resets the in-memory databases back to baseline setup. |

---

## 🎨 Visual Design Aesthetics

The interface is built with **modern Dark Mode Glassmorphism** to present a gorgeous first impression:
- **Colour Palette**: Tailored colors using HSL variables (Deep Midnight `#0a0e1a` background, dark card decks `#161b2c`, cyan `#00E5FF` highlights, emerald green seat status indicators, crimson warnings, and gold user alerts).
- **Glass Effects**: `backdrop-filter: blur(16px); background: rgba(15, 20, 32, 0.55);` coupled with subtle border highlights (`rgba(255,255,255,0.07)`), rendering containers that appear translucent and layered.
- **Micro-Animations**:
  - Rotating steering wheels simulating active operation states.
  - Vibrating bell indicators alerting users of fresh inbox items.
  - Scale transforms and glowing shadows on seat selections.
  - Intense red screen flashes and rapid camera shakes when simulated breakdowns occur.

---

## 🚀 Execution Guide

1. **Compile codebase**:
   ```bash
   javac -d bin -sourcepath src src/com/busreservation/server/WebServer.java
   ```
2. **Launch http server**:
   ```bash
   java -cp bin com.busreservation.server.WebServer
   ```
3. **Open address**:
   `http://localhost:8080/`
