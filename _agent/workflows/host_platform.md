---
description: How to compile and start the local web hosting server for the Bus Reservation platform
---

This workflow facilitates running the Java HTTP server and launching the companion web dashboard.

### Steps:

1. Compile the Java server and domain modules:
```bash
javac -d bin -sourcepath src src/com/busreservation/server/WebServer.java
```

// turbo
2. Start the local server launcher:
```bash
java -cp bin com.busreservation.server.WebServer
```

3. Open your browser and navigate to the web dashboard:
`http://localhost:8080/`
