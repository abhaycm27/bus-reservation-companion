# Bus Reservation & Travel Companion Platform

A next-generation enterprise-grade Online Bus Reservation and Travel Companion Platform backend designed in Java 17. The system is architected around clean Object-Oriented Design (OOD) practices, SOLID principles, and concurrency-safe mechanisms.

## Project Structure
```text
C:\Users\PRO\.gemini\antigravity\scratch\bus-reservation-companion\
├── bin/                       # Compiled Java class files
├── src/
│   └── com/
│       └── busreservation/
│           ├── model/         # Domain models (User hierarchy, Bus, Seat, Route, etc.)
│           ├── service/       # Business logic (Reservation, Comfort index, Gallery, etc.)
│           ├── repository/    # Storage abstraction (Generic and InMemory repositories)
│           ├── exception/     # Custom runtime exception hierarchy
│           └── Main.java      # Comprehensive simulation runner
├── run_log_utf8.txt           # Console output of the complete simulation run
└── README.md                  # System documentation
```

## Core Design Patterns Implemented
1. **Factory Pattern**:
   - `UserFactory` dynamically instantiates subclasses of the abstract `User` class (`Passenger`, `Driver`, `Admin`, `BusOperator`).
   - `PlatformPaymentFactory` returns the requested `PaymentProcessor` strategy instance dynamically.
2. **Strategy Pattern**:
   - **Payment Module**: Supports dynamic swapping between `UPIPayment`, `CardPayment`, and `WalletPayment` strategies.
   - **Delay Prediction Engine**: Supports swapping between `HistoricalTrafficDelayStrategy` and `LiveWeatherCheckpointDelayStrategy` at runtime.
3. **Observer Pattern**:
   - Registered passengers act as `Observer`s of schedules, receiving automated incident notifications (such as system breakdowns or delay notifications).
4. **DAO / Repository Layer**:
   - Clean interface abstraction (`Repository<T, ID>`) featuring a generic in-memory implementation (`InMemoryRepository<T, ID>`) utilizing thread-safe `ConcurrentHashMap`.

## Compiling & Executing

### Prerequisites
- JDK 17 or higher.

### Steps
1. Navigate to the project root directory:
   ```bash
   cd C:\Users\PRO\.gemini\antigravity\scratch\bus-reservation-companion
   ```
2. Compile the source codebase:
   ```bash
   javac -d bin -sourcepath src src/com/busreservation/Main.java
   ```
3. Execute the simulator run:
   ```bash
   java -cp bin com.busreservation.Main
   ```

## Concurrency and Locking Strategy
Temporary seat locking is handled thread-safely directly within the `Seat` domain model using Java `synchronized` monitors. When checking seat availability, the system evaluates the absolute `lockExpiration` timestamp. If a lock has expired, the status is lazily reset to `AVAILABLE` on subsequent queries, avoiding double-bookings under concurrent checkout requests.

## Running the Live HTTP Server (Web UI + API)

Prerequisite: JDK 17 or higher installed and available on `PATH`.

- **Windows (cmd)**: run the existing launcher `run.bat` which compiles and starts the embedded HTTP server on port 8080.

   ```bat
   run.bat
   ```

- **POSIX (macOS / Linux / Git Bash on Windows)**: use the included `build.sh` to compile and run the server.

   ```bash
   ./build.sh
   ```

- What it does: compiles `src` into `bin` and starts the Java HTTP server which serves the static frontend from the `web/` folder and exposes REST endpoints under `/api/*` (e.g., `/api/state`, `/api/book`).

- Files:
   - [run.bat](run.bat)
   - [src/com/busreservation/server/WebServer.java](src/com/busreservation/server/WebServer.java)
   - [web/index.html](web/index.html)

Open http://localhost:8080/ in your browser after launching.

## Deploying to Render

The repository includes `render.yaml` for deploying the Java web server as a Render Web Service. Connect the GitHub repository to Render and choose **Blueprint** deployment. Render supplies the `PORT` environment variable automatically, and the server uses it in production.

## Deploying the frontend to Netlify

The repository also includes `netlify.toml`. In Netlify, import this repository and deploy it with the default settings. Netlify publishes the `web/` folder and proxies `/api/*` requests to the Render backend at `bus-reservation-companion.onrender.com`.

## Optional: Serve only the static frontend with Node
If you prefer to run just the static frontend with automatic reloads, use the included Node dev setup.

Prerequisites: Node.js and npm installed.

1. Install dev dependencies:

```bash
npm install
```

2. Start the live-reloading static server (serves the `web/` folder on port 3000):

```bash
npm run start:web
```

Open http://localhost:3000/ in your browser.
