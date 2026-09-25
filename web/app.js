// Global State
let serverState = {};
// Localization (Kerala region)
const LOCALE = 'en-IN';
const CURRENCY = 'INR';
const TIMEZONE = 'Asia/Kolkata';
function formatCurrencyINR(amount) {
    try {
        return new Intl.NumberFormat(LOCALE, { style: 'currency', currency: CURRENCY }).format(amount);
    } catch (e) {
        return '₹' + Number(amount).toFixed(2);
    }
}
function formatDateIndian(isoDateStr) {
    if (!isoDateStr) return '';
    const dt = new Date(isoDateStr);
    return dt.toLocaleString(LOCALE, { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit', timeZone: TIMEZONE });
}
function formatPhoneIndia(phone) {
    if (!phone) return '';
    // if already includes +, return as-is
    if (phone.startsWith('+')) return phone;
    const digits = phone.replace(/[^0-9]/g, '');
    if (digits.length === 10) {
        return `+91-${digits.slice(0,5)}-${digits.slice(5)}`;
    }
    return phone;
}

function mapRouteToKerala(route) {
    // Map demo routes to Kerala cities for regional flavor
    if (!route) return route;
    const map = {
        'New York': 'Kochi',
        'Boston': 'Thiruvananthapuram',
        'NYC': 'Kochi',
        'BOS': 'Thiruvananthapuram'
    };
    const source = map[route.source] || route.source;
    const destination = map[route.destination] || route.destination;
    return { ...route, source, destination };
}

let activeUserId = "";
let selectedScheduleId = "SCH-1001";
let selectedSeatNumber = null;
let currentDelayStrategy = "historical";
let trackingScheduleId = "SCH-1001";

// DOM Elements
const userSelect = document.getElementById("user-select");
const scheduleSelect = document.getElementById("schedule-select");
const activeUserName = document.getElementById("active-user-name");
const activeUserContact = document.getElementById("active-user-contact");
const activeRewards = document.getElementById("active-rewards");
const scheduleDetails = document.getElementById("schedule-details");
const seatGrid = document.getElementById("seat-grid");
const trackingScheduleSelect = document.getElementById("tracking-schedule-select");
const trackingStatus = document.getElementById("tracking-status");
const trackingLocation = document.getElementById("tracking-location");
const trackingBusRoute = document.getElementById("tracking-bus-route");
const trackingArrival = document.getElementById("tracking-arrival");
const trackingLastUpdated = document.getElementById("tracking-last-updated");
const trackingOrigin = document.getElementById("tracking-origin");
const trackingMidpoint = document.getElementById("tracking-midpoint");
const trackingDestination = document.getElementById("tracking-destination");
const trackingBusMarker = document.getElementById("tracking-bus-marker");
const trackingRouteProgress = document.getElementById("tracking-route-progress");
const trackingNote = document.getElementById("tracking-note");

// Checkout Panel elements
const checkoutPanel = document.getElementById("checkout-panel");
const btnCancelCheckout = document.getElementById("btn-cancel-checkout");
const summarySeatNum = document.getElementById("summary-seat-num");
const summaryMultiplier = document.getElementById("summary-multiplier");
const summaryFinalPrice = document.getElementById("summary-final-price");
const paymentMethod = document.getElementById("payment-method");
const paymentDetailContainer = document.getElementById("payment-detail-container");
const paymentDetailLabel = document.getElementById("payment-detail-label");
const paymentDetailInput = document.getElementById("payment-detail-input");
const btnConfirmPayment = document.getElementById("btn-confirm-payment");
const receiptDisplay = document.getElementById("receipt-display");

// Drawers & Modals
const drawerRegister = document.getElementById("drawer-register");
const btnRegisterDrawer = document.getElementById("btn-register-drawer");
const btnRemoveUser = document.getElementById("btn-remove-user");
const btnManageUsers = document.getElementById("btn-manage-users");
const btnCloseDrawer = document.getElementById("btn-close-drawer");
const registerForm = document.getElementById("register-form");
const regType = document.getElementById("reg-type");
const regId = document.getElementById("reg-id");
const btnGenerateId = document.getElementById("btn-generate-id");
const driverFields = document.getElementById("driver-fields");
const drawerUsers = document.getElementById("drawer-users");
const btnCloseUsers = document.getElementById("btn-close-users");
const userManagementList = document.getElementById("user-management-list");

const drawerUpload = document.getElementById("drawer-upload");
const btnShowUpload = document.getElementById("btn-show-upload");
const btnCloseUploadDrawer = document.getElementById("btn-close-upload-drawer");
const uploadPhotoForm = document.getElementById("upload-photo-form");
const uploadUrlInput = document.getElementById("upload-url");

// Prediction elements
const btnPredictDelay = document.getElementById("btn-predict-delay");
const predictTrafficRange = document.getElementById("predict-traffic");
const trafficValLabel = document.getElementById("traffic-val");
const predictWeather = document.getElementById("predict-weather");
const btnStratHistorical = document.getElementById("strat-historical");
const btnStratWeather = document.getElementById("strat-weather");
const predictionResult = document.getElementById("prediction-result");
const predictedMinutes = document.getElementById("predicted-minutes");
const predictedStrategyName = document.getElementById("predicted-strategy-name");

// Rescue / Reset elements
const btnTriggerRescue = document.getElementById("btn-trigger-rescue");
const btnResetState = document.getElementById("btn-reset-state");

// Comfort form
const comfortRatingForm = document.getElementById("comfort-rating-form");

// Toast Container
const toastContainer = document.getElementById("toast-container");

// Generate random User ID based on type
function generateUserId() {
    const type = regType.value;
    const randomNum = Math.floor(Math.random() * 9000) + 100;
    let prefix = "USR-PL-"; // Passenger default
    
    if (type === "driver") {
        prefix = "USR-DR-";
    } else if (type === "admin") {
        prefix = "USR-AD-";
    }
    
    regId.value = prefix + randomNum;
}

// Page initialization
document.addEventListener("DOMContentLoaded", () => {
    setupPageNavigation();
    fetchState(true); // Initial fetch and set default active user
    setupEventListeners();
    generateUserId(); // Generate initial user ID
    setInterval(updateTrackingDisplay, 5000);

    // Restore persisted ticket receipt if exists
    const cachedReceipt = localStorage.getItem("lastBookingReceiptHTML");
    if (cachedReceipt) {
        receiptDisplay.innerHTML = cachedReceipt;
        receiptDisplay.classList.remove("hidden");
    }
});

function setupPageNavigation() {
    const pageLinks = document.querySelectorAll(".nav-link[data-page]");
    const applyPage = () => {
        const requestedPage = window.location.hash.replace("#", "");
        const page = ["reservation", "predictions", "emergency", "comfort", "gallery", "tracking"].includes(requestedPage)
            ? requestedPage
            : "reservation";

        document.body.dataset.page = page;
        pageLinks.forEach(link => {
            link.classList.toggle("active", link.dataset.page === page);
        });
    };

    window.addEventListener("hashchange", applyPage);
    applyPage();
}

// Event Listeners setup
function setupEventListeners() {
    // User change
    userSelect.addEventListener("change", (e) => {
        activeUserId = e.target.value;
        updateActiveUserProfile();
        updateAdminRemoveButton();
        renderSeatGrid();
    });

    // Schedule change
    scheduleSelect.addEventListener("change", (e) => {
        selectedScheduleId = e.target.value;
        trackingScheduleId = e.target.value;
        renderScheduleDetails();
        renderSeatGrid();
        populateTrackingScheduleSelector();
        updateTrackingDisplay();
        checkoutPanel.classList.add("hidden");
    });

    trackingScheduleSelect.addEventListener("change", (e) => {
        trackingScheduleId = e.target.value;
        updateTrackingDisplay();
    });

    // Registrations drawer toggle
    btnRegisterDrawer.addEventListener("click", () => {
        drawerRegister.classList.add("active");
        generateUserId(); // Generate new ID when drawer opens
    });
    btnRemoveUser.addEventListener("click", handleRemoveSelectedUser);
    btnCloseDrawer.addEventListener("click", () => drawerRegister.classList.remove("active"));
    btnManageUsers.addEventListener("click", () => {
        renderUserManagementList();
        drawerUsers.classList.add("active");
    });
    btnCloseUsers.addEventListener("click", () => drawerUsers.classList.remove("active"));
    userManagementList.addEventListener("click", handleUserManagementAction);

    regType.addEventListener("change", (e) => {
        generateUserId(); // Auto-generate new ID when type changes
        if (e.target.value === "driver") {
            driverFields.classList.remove("hidden");
        } else {
            driverFields.classList.add("hidden");
        }
    });

    // Generate new User ID button
    btnGenerateId.addEventListener("click", (e) => {
        e.preventDefault();
        generateUserId();
    });

    registerForm.addEventListener("submit", handleRegistration);

    // Upload drawer toggle
    btnShowUpload.addEventListener("click", () => drawerUpload.classList.add("active"));
    btnCloseUploadDrawer.addEventListener("click", () => drawerUpload.classList.remove("active"));
    uploadPhotoForm.addEventListener("submit", handlePhotoUpload);

    // Image chip listeners for quick templates
    document.querySelectorAll(".sample-img-chip").forEach(chip => {
        chip.addEventListener("click", () => {
            uploadUrlInput.value = chip.getAttribute("data-url");
        });
    });

    // Reset baseline state
    btnResetState.addEventListener("click", () => {
        sendPost("/api/reset", {}, (res) => {
            if (res.success) {
                showToast("System state reset to simulator baseline successfully!", "success");
                fetchState(true);
                localStorage.removeItem("lastBookingReceiptHTML");
                receiptDisplay.innerHTML = "";
                receiptDisplay.classList.add("hidden");
                checkoutPanel.classList.add("hidden");
                predictionResult.classList.add("hidden");
            }
        });
    });

    // Delay Strategy toggles
    btnStratHistorical.addEventListener("click", () => {
        currentDelayStrategy = "historical";
        btnStratHistorical.classList.add("active");
        btnStratWeather.classList.remove("active");
    });
    btnStratWeather.addEventListener("click", () => {
        currentDelayStrategy = "weather";
        btnStratWeather.classList.add("active");
        btnStratHistorical.classList.remove("active");
    });

    predictTrafficRange.addEventListener("input", (e) => {
        trafficValLabel.textContent = `${e.target.value}x`;
    });

    btnPredictDelay.addEventListener("click", queryDelayPrediction);

    // Cancel checkout
    btnCancelCheckout.addEventListener("click", () => {
        checkoutPanel.classList.add("hidden");
        selectedSeatNumber = null;
        renderSeatGrid();
    });

    // Payment strategies change values
    paymentMethod.addEventListener("change", (e) => {
        const val = e.target.value;
        if (val === "upi") {
            paymentDetailLabel.textContent = "UPI ID (VPA Address)";
            paymentDetailInput.placeholder = "e.g. alice@okhdfcbank";
        } else if (val === "card") {
            paymentDetailLabel.textContent = "Card Details (16-Digit Card No.)";
            paymentDetailInput.placeholder = "e.g. 4000123456789010";
        } else if (val === "wallet") {
            paymentDetailLabel.textContent = "Wallet Contact Phone Number";
            paymentDetailInput.placeholder = "e.g. +91-98765-43210";
        }
    });

    // Confirm Payment
    document.getElementById("payment-form").addEventListener("submit", handleCheckoutConfirm);

    // Comfort ratings
    comfortRatingForm.addEventListener("submit", handleComfortRatingSubmit);

    // Rescue Breakdown Sim
    btnTriggerRescue.addEventListener("click", triggerBreakdownRescue);
}

// REST GET System State
function fetchState(setDefaultUser = false) {
    fetch("/api/state")
        .then(res => {
            if (!res.ok) {
                throw new Error(`Backend returned HTTP ${res.status}`);
            }
            return res.json();
        })
        .then(data => {
            serverState = data;
            populateUserSelector(setDefaultUser);
            populateScheduleSelector();
            populateTrackingScheduleSelector();
            renderScheduleDetails();
            renderSeatGrid();
            renderGalleryStream();
            updateActiveUserProfile();
            updateAdminRemoveButton();
            updateTrackingDisplay();
        })
        .catch(err => {
            console.error("Error fetching state:", err);
            activeUserName.textContent = "Backend unavailable";
            activeUserContact.textContent = "The Java service has not been deployed yet. Complete the Render deployment to load live data.";
            activeRewards.textContent = "--";
            scheduleDetails.innerHTML = `<div class="backend-status-message"><i class="fa-solid fa-server"></i><strong>Waiting for backend service</strong><span>Frontend is online, but the reservation API is not reachable yet.</span></div>`;
            trackingStatus.textContent = "Backend unavailable";
            trackingLocation.textContent = "--";
            trackingNote.textContent = "Deploy the Render Java service, then refresh this page.";
        });
}

function populateTrackingScheduleSelector() {
    if (!trackingScheduleSelect) return;
    const currentValue = trackingScheduleId || selectedScheduleId;
    trackingScheduleSelect.innerHTML = "";
    (serverState.schedules || []).forEach(schedule => {
        const option = document.createElement("option");
        option.value = schedule.scheduleId;
        option.textContent = `${schedule.scheduleId} : ${schedule.route.source} to ${schedule.route.destination}`;
        trackingScheduleSelect.appendChild(option);
    });
    trackingScheduleId = (serverState.schedules || []).some(schedule => schedule.scheduleId === currentValue)
        ? currentValue
        : serverState.schedules?.[0]?.scheduleId;
    trackingScheduleSelect.value = trackingScheduleId || "";
}

function updateTrackingDisplay() {
    const schedule = (serverState.schedules || []).find(item => item.scheduleId === trackingScheduleId);
    if (!schedule || !trackingStatus) return;

    const departure = new Date(schedule.departureTime);
    const arrival = new Date(schedule.arrivalTime);
    const now = Date.now();
    const duration = Math.max(1, arrival.getTime() - departure.getTime());
    const progress = Math.max(0, Math.min(1, (now - departure.getTime()) / duration));
    const isUpcoming = now < departure.getTime();
    const isComplete = now >= arrival.getTime();

    trackingScheduleSelect.value = schedule.scheduleId;
    trackingOrigin.textContent = schedule.route.source;
    trackingMidpoint.textContent = `${schedule.route.source} to ${schedule.route.destination}`;
    trackingDestination.textContent = schedule.route.destination;
    trackingBusRoute.textContent = `${schedule.busId} | ${schedule.route.routeId}`;
    trackingArrival.textContent = formatDateIndian(schedule.arrivalTime);
    trackingLastUpdated.textContent = new Date().toLocaleTimeString(LOCALE);
    trackingRouteProgress.style.width = `${Math.max(2, progress * 100)}%`;
    trackingBusMarker.style.left = `${Math.max(2, Math.min(96, progress * 100))}%`;

    if (isUpcoming) {
        trackingStatus.textContent = "Scheduled";
        trackingLocation.textContent = schedule.route.source;
        trackingNote.textContent = `Estimated position: ${schedule.route.source}. Departure is ${formatDateIndian(schedule.departureTime)}. No GPS signal is available.`;
    } else if (isComplete) {
        trackingStatus.textContent = "Arrived";
        trackingLocation.textContent = schedule.route.destination;
        trackingNote.textContent = "Estimated position: destination reached according to the schedule. No GPS signal is available.";
    } else {
        trackingStatus.textContent = "In transit";
        trackingLocation.textContent = `Between ${schedule.route.source} and ${schedule.route.destination}`;
        trackingNote.textContent = "Estimated position is calculated from elapsed schedule time, not a GPS device.";
    }
}

// Populate User Selector
function populateUserSelector(setDefault = false) {
    const origVal = userSelect.value || activeUserId;
    userSelect.innerHTML = "";

    serverState.passengers.forEach(p => {
        const opt = document.createElement("option");
        opt.value = p.userId;
        opt.textContent = `${p.name} (Passenger)`;
        userSelect.appendChild(opt);
    });

    serverState.drivers.forEach(d => {
        const opt = document.createElement("option");
        opt.value = d.userId;
        opt.textContent = `${d.name} (Driver)`;
        userSelect.appendChild(opt);
    });

    const admins = serverState.admins || [];
    admins.forEach(a => {
        const adminOpt = document.createElement("option");
        adminOpt.value = a.userId;
        adminOpt.textContent = `${a.name} (Admin)`;
        userSelect.appendChild(adminOpt);
    });

    if (setDefault || !origVal) {
        const defaultAdmin = admins.length ? admins[0].userId : "USR-AD-301";
        userSelect.value = defaultAdmin;
        activeUserId = defaultAdmin;
    } else if (origVal) {
        userSelect.value = origVal;
        activeUserId = origVal;
    }
}

// Populate Schedule Selector
function populateScheduleSelector() {
    const origVal = scheduleSelect.value || selectedScheduleId;
    scheduleSelect.innerHTML = "";

    serverState.schedules.forEach(s => {
        const opt = document.createElement("option");
        opt.value = s.scheduleId;
        const localizedRoute = mapRouteToKerala(s.route);
        opt.textContent = `${s.scheduleId} : ${localizedRoute.source} ➔ ${localizedRoute.destination} (${formatCurrencyINR(s.baseFare)})`;
        scheduleSelect.appendChild(opt);
    });

    if (origVal) {
        scheduleSelect.value = origVal;
        selectedScheduleId = origVal;
    }
}

// Render schedule details card
function renderScheduleDetails() {
    const sched = serverState.schedules.find(s => s.scheduleId === selectedScheduleId);
    if (!sched) return;

    const bus = serverState.buses.find(b => b.busId === sched.busId);
    if (!bus) return;

    let featuresHtml = bus.features.map(f => `<span class="feature-chip">${f}</span>`).join("");
    let stopsHtml = sched.route.restStops.map(stop => `
        <div class="stop-row">
            <strong>${stop.name}</strong> 
            <span style="color:var(--accent-amber);">★ ${stop.hygieneRating}</span> | 
            <span style="color:var(--accent-cyan); font-size:11px;">Dietaries: ${stop.dietaryOptions.join(", ")}</span>
            <div style="font-style: italic; color: var(--text-muted); margin-top:2px;">"${stop.passengerReviews[0] || 'Clean rest checkstop.'}"</div>
        </div>
    `).join("");

    const depDate = formatDateIndian(sched.departureTime);
    const arrDate = formatDateIndian(sched.arrivalTime);

        const localizedRoute = mapRouteToKerala(sched.route);
    scheduleDetails.innerHTML = `
        <div class="schedule-route-h">
                <span class="sch-station"><i class="fa-solid fa-map-pin"></i> ${localizedRoute.source}</span>
            <span class="arrow-divider"><i class="fa-solid fa-arrow-right"></i></span>
                <span class="sch-station"><i class="fa-solid fa-location-arrow"></i> ${localizedRoute.destination}</span>
        </div>
        <div class="details-grid">
            <div class="detail-item">
                <span class="detail-lbl">Operator:</span>
                <span>${bus.operatorName}</span>
            </div>
            <div class="detail-item">
                <span class="detail-lbl">Comfort Index:</span>
                <span style="font-weight:600; color:var(--accent-cyan)">${bus.comfortScore === 0 ? 'No ratings yet' : '★ ' + bus.comfortScore.toFixed(1) + ' / 5.0'}</span>
            </div>
            <div class="detail-item">
                <span class="detail-lbl">Departure:</span>
                <span>${depDate}</span>
            </div>
            <div class="detail-item">
                <span class="detail-lbl">Arrival:</span>
                <span>${arrDate}</span>
            </div>
            <div class="detail-item">
                <span class="detail-lbl">Driver Assigned:</span>
                <span>${bus.driver ? `${bus.driver.name} (Exp: ${bus.driver.yearsOfExperience} yrs)` : 'None'}</span>
            </div>
            <div class="detail-item">
                <span class="detail-lbl">Base Price:</span>
                <span style="font-weight:600; color:var(--accent-emerald)">${formatCurrencyINR(sched.baseFare)}</span>
            </div>
        </div>
        <div class="features-chips">
            ${featuresHtml}
        </div>
        <div class="stops-subbox">
            <h4>Planned Food & Rest Stops (${sched.route.restStops.length})</h4>
            <div class="stops-list">${stopsHtml}</div>
        </div>
    `;
}

// Update Active User Banner
function updateActiveUserProfile() {
    activeRewards.textContent = "N/A";

    const admin = (serverState.admins || []).find(x => x.userId === activeUserId);
    if (admin) {
        activeUserName.textContent = admin.name;
        activeUserContact.textContent = `${admin.email} | System Administrator Control Mode`;
        return;
    }

    // Try finding in passengers
    const p = serverState.passengers.find(x => x.userId === activeUserId);
    if (p) {
        activeUserName.textContent = p.name;
        activeUserContact.textContent = `Email: ${p.email} | Mobile: ${formatPhoneIndia(p.phone)} (Passenger Profile)`;
        activeRewards.textContent = p.rewardPoints;
        return;
    }

    // Try driver
    const d = serverState.drivers.find(x => x.userId === activeUserId);
    if (d) {
        activeUserName.textContent = d.name;
        activeUserContact.textContent = `License: ${d.licenseNumber} | Safety: ★ ${d.safetyRating} / 5.0 (Bus Driver)`;
        return;
    }
}

function isCurrentUserAdmin() {
    return (serverState.admins || []).some(a => a.userId === activeUserId);
}

function updateAdminRemoveButton() {
    const selectedUserId = userSelect.value;
    const isAdminView = isCurrentUserAdmin();
    const isSelfAdmin = selectedUserId === activeUserId && isAdminView;
    const isRemovable = isAdminView && selectedUserId && !isSelfAdmin && selectedUserId !== "";
    btnRemoveUser.classList.toggle("hidden", !isRemovable);
    btnManageUsers.classList.toggle("hidden", !isAdminView);
}

function getManagedUsers() {
    return [
        ...(serverState.passengers || []).map(user => ({ ...user, role: "Passenger" })),
        ...(serverState.drivers || []).map(user => ({ ...user, role: "Driver" })),
        ...(serverState.admins || []).map(user => ({ ...user, role: "Admin" }))
    ];
}

function renderUserManagementList() {
    userManagementList.innerHTML = "";
    const users = getManagedUsers();

    if (!users.length) {
        userManagementList.textContent = "No registered users found.";
        return;
    }

    users.forEach(user => {
        const row = document.createElement("div");
        row.className = "user-management-row";

        const details = document.createElement("div");
        details.className = "user-management-details";
        const name = document.createElement("strong");
        name.textContent = user.name || user.userId;
        const meta = document.createElement("span");
        meta.textContent = `${user.role} | ${user.userId}`;
        details.append(name, meta);

        const action = document.createElement("button");
        action.className = "btn btn-danger-outline btn-user-remove";
        action.dataset.userId = user.userId;
        action.disabled = user.userId === activeUserId;
        action.innerHTML = user.userId === activeUserId
            ? "<i class=\"fa-solid fa-lock\"></i> Current"
            : "<i class=\"fa-solid fa-user-slash\"></i> Remove";

        row.append(details, action);
        userManagementList.appendChild(row);
    });
}

function handleUserManagementAction(event) {
    const action = event.target.closest(".btn-user-remove");
    if (!action || action.disabled) return;

    const targetUserId = action.dataset.userId;
    if (!confirm(`Remove user ${targetUserId}? This action is only allowed for admins.`)) return;

    sendPost("/api/remove-user", { adminUserId: activeUserId, targetUserId }, (res) => {
        if (res.success) {
            showToast(res.message || "User removed successfully.", "success");
            fetchState(false);
            renderUserManagementList();
        } else {
            showToast(res.error || "User removal failed.", "error");
        }
    });
}

// Render Seat Grid
function renderSeatGrid() {
    const sched = serverState.schedules.find(s => s.scheduleId === selectedScheduleId);
    if (!sched) return;

    const bus = serverState.buses.find(b => b.busId === sched.busId);
    if (!bus) return;

    seatGrid.innerHTML = "";

    // Group seats by row
    const seatsByRow = {};
    bus.seats.forEach(s => {
        if (!seatsByRow[s.row]) {
            seatsByRow[s.row] = [];
        }
        seatsByRow[s.row].push(s);
    });

    // Get sorted rows
    const sortedRows = Object.keys(seatsByRow)
        .map(r => parseInt(r))
        .sort((a, b) => a - b);

    // Render each row
    sortedRows.forEach(rowNum => {
        const rowSeats = seatsByRow[rowNum].sort((a, b) => a.column - b.column);
        
        // Create row container
        const rowEl = document.createElement("div");
        rowEl.className = "seat-row";

        // Left side (columns 0-1: window + aisle)
        const leftSide = document.createElement("div");
        leftSide.style.display = "flex";
        leftSide.style.gap = "8px";
        leftSide.style.justifyContent = "flex-end";
        
        // Right side (columns 2-3: aisle + window)
        const rightSide = document.createElement("div");
        rightSide.style.display = "flex";
        rightSide.style.gap = "8px";
        rightSide.style.justifyContent = "flex-start";

        // Create seat elements and assign to left/right
        rowSeats.forEach(s => {
            const seatEl = document.createElement("div");
            seatEl.className = "seat";

            // Determine status classes
            let status = s.status;
            if (status === "LOCKED") {
                if (s.lockedByPassengerId === activeUserId) {
                    seatEl.classList.add("locked-by-me");
                } else {
                    seatEl.classList.add("locked");
                }
            } else if (status === "BOOKED") {
                seatEl.classList.add("booked");
            } else {
                seatEl.classList.add("available");
            }

            // Selected highlights
            if (selectedSeatNumber === s.seatNumber && s.status === "AVAILABLE") {
                seatEl.classList.add("locked-by-me");
            }

            seatEl.innerHTML = `
                <div>${s.seatNumber}</div>
                <span class="seat-type-indicator">${s.seatType.charAt(0)}</span>
            `;
            seatEl.style.width = "50px";
            seatEl.style.minHeight = "50px";

            // Click Handler
            seatEl.addEventListener("click", () => {
                if (status === "BOOKED") {
                    showToast(`Seat ${s.seatNumber} is booked and unavailable!`, "error");
                    return;
                }
                if (status === "LOCKED" && s.lockedByPassengerId !== activeUserId) {
                    showToast(`Seat ${s.seatNumber} is locked by another passenger.`, "error");
                    return;
                }

                // Lock the seat
                selectedSeatNumber = s.seatNumber;
                triggerLockSeat(s);
            });

            // Assign to left or right side
            if (s.column < 2) {
                // Left side seats (columns 0-1)
                leftSide.appendChild(seatEl);
            } else {
                // Right side seats (columns 2-3)
                rightSide.appendChild(seatEl);
            }
        });

        // Add left and right sides to row
        rowEl.appendChild(leftSide);
        rowEl.appendChild(rightSide);
        
        // Add row to grid
        seatGrid.appendChild(rowEl);
    });
}

// API Post to Lock Seat
function triggerLockSeat(seat) {
    const body = {
        scheduleId: selectedScheduleId,
        seatNumber: seat.seatNumber,
        userId: activeUserId
    };

    sendPost("/api/lock", body, (res) => {
        if (res.success) {
            showToast(res.message || `Seat ${seat.seatNumber} locked successfully!`, "success");

            // Calculate final seat price for checkout display
            const sched = serverState.schedules.find(s => s.scheduleId === selectedScheduleId);
            let multiplier = 1.0;
            if (seat.seatType === "WINDOW") multiplier = 1.15;
            else if (seat.seatType === "SLEEPER") multiplier = 1.50;

            const finalP = sched.baseFare * multiplier;

            summarySeatNum.textContent = seat.seatNumber;
            summaryMultiplier.textContent = `${multiplier}x (${seat.seatType})`;
            summaryFinalPrice.textContent = formatCurrencyINR(finalP);

            checkoutPanel.classList.remove("hidden");

            // Refresh states
            fetchState();
        } else {
            showToast(res.error || "Lock seat runtime exception occurred.", "error");
        }
    });
}

// handle checkout payment confirm
function handleCheckoutConfirm(e) {
    e.preventDefault();
    if (!selectedSeatNumber) return;

    const body = {
        userId: activeUserId,
        scheduleId: selectedScheduleId,
        seatNumber: selectedSeatNumber,
        paymentMethod: paymentMethod.value.toUpperCase(),
        paymentDetail: paymentDetailInput.value
    };

    sendPost("/api/book", body, (res) => {
        if (res.success) {
            showToast(`Booking ${res.bookingId} fully confirmed and paid!`, "success");
            checkoutPanel.classList.add("hidden");

            // Show Premium Visual Invoice Receipt
            receiptDisplay.classList.remove("hidden");
            receiptDisplay.innerHTML = `
                <div class="receipt-title"><i class="fa-solid fa-circle-check"></i> TransitFlow Ticket Issued</div>
                <div class="price-row">
                    <span>Invoice Ref:</span>
                    <strong>${res.bookingId}</strong>
                </div>
                <div class="price-row">
                    <span>Passenger User:</span>
                    <span>${activeUserId}</span>
                </div>
                <div class="price-row">
                    <span>Route Trip:</span>
                    <span>${selectedScheduleId}</span>
                </div>
                <div class="price-row">
                    <span>Assigned Seat:</span>
                    <strong>${selectedSeatNumber}</strong>
                </div>
                <div class="receipt-divider"></div>
                <div class="price-row">
                    <span>Payment Method:</span>
                    <span>${paymentMethod.value.toUpperCase()} strategy</span>
                </div>
                <div class="price-row">
                    <span>Gateway Transaction ID:</span>
                    <span style="font-size:11px; font-family:monospace; color:var(--text-secondary);">${res.paymentTransactionId}</span>
                </div>
                <div class="price-row" style="margin-top:10px;">
                    <span style="font-weight:600; color:var(--accent-emerald)">Final Fare Paid:</span>
                    <strong style="color:var(--accent-emerald)">${formatCurrencyINR(res.finalPrice)}</strong>
                </div>
            `;

            // Persist invoice receipt HTML locally
            localStorage.setItem("lastBookingReceiptHTML", receiptDisplay.innerHTML);

            selectedSeatNumber = null;
            fetchState();
        } else {
            showToast(res.error || "Booking confirmation failed.", "error");
        }
    });
}

// handle User Register
function handleRegistration(e) {
    e.preventDefault();
    const body = {
        type: regType.value,
        userId: document.getElementById("reg-id").value,
        name: document.getElementById("reg-name").value,
        email: document.getElementById("reg-email").value,
        phone: document.getElementById("reg-phone").value,
        credentials: document.getElementById("reg-creds").value,
        license: document.getElementById("reg-license").value,
        experience: document.getElementById("reg-exp").value
    };

    sendPost("/api/register", body, (res) => {
        if (res.success) {
            showToast(res.message, "success");
            activeUserId = res.userId || body.userId;
            drawerRegister.classList.remove("active");
            registerForm.reset();
            fetchState(false);
        } else {
            showToast(res.error || "Registration exception.", "error");
        }
    });
}

function handleRemoveSelectedUser() {
    const targetUserId = userSelect.value;
    if (!targetUserId || targetUserId === activeUserId) {
        showToast("Select another user to remove.", "error");
        return;
    }

    if (!confirm(`Remove user ${targetUserId}? This action is only allowed for admins.`)) {
        return;
    }

    sendPost("/api/remove-user", { adminUserId: activeUserId, targetUserId }, (res) => {
        if (res.success) {
            showToast(res.message || "User removed successfully.", "success");
            fetchState(true);
        } else {
            showToast(res.error || "User removal failed.", "error");
        }
    });
}

// handle Photo upload
function handlePhotoUpload(e) {
    e.preventDefault();
    const body = {
        userId: activeUserId,
        scheduleId: selectedScheduleId,
        caption: document.getElementById("upload-caption").value,
        url: uploadUrlInput.value
    };

    sendPost("/api/upload", body, (res) => {
        if (res.success) {
            showToast("Successfully posted photo to memory stream gallery!", "success");
            drawerUpload.classList.remove("active");
            uploadPhotoForm.reset();
            fetchState();
        } else {
            showToast(res.error || "Photo sharing failed.", "error");
        }
    });
}

// render photos gallery
function renderGalleryStream() {
    const listGrid = document.getElementById("media-stream-grid");
    listGrid.innerHTML = "";

    if (!serverState.photos || serverState.photos.length === 0) {
        listGrid.innerHTML = `<div class="notif-empty" style="grid-column: 1/-1;">No trip memories uploaded yet. Passenger Alice can upload after simulated departures.</div>`;
        return;
    }

    serverState.photos.forEach(photo => {
        const card = document.createElement("div");
        card.className = "photo-card";

        card.innerHTML = `
            <div class="photo-img" style="background-image: url('${photo.photoUrl}')"></div>
            <div class="photo-content">
                <div class="photo-caption">"${photo.caption}"</div>
                <div class="photo-meta">
                    <span class="photo-author"><i class="fa-solid fa-user"></i> ${photo.passengerId}</span>
                    <div class="photo-actions">
                        <span style="font-size:10px;"><i class="fa-solid fa-bus"></i> ${photo.tripId}</span>
                        <button class="like-btn" onclick="likePhoto('${photo.photoId}')">
                            <i class="fa-solid fa-heart"></i> <span class="like-count">${photo.likeCount}</span>
                        </button>
                    </div>
                </div>
            </div>
        `;
        listGrid.appendChild(card);
    });
}

// Like query
window.likePhoto = function (photoId) {
    sendPost("/api/like", { photoId }, (res) => {
        if (res.success) {
            fetchState();
        }
    });
};

// Comfort index rating submit
function handleComfortRatingSubmit(e) {
    e.preventDefault();

    // helper to extract radio checked values
    const getRadioVal = (name) => {
        const rad = document.getElementsByName(name);
        for (let i = 0; i < rad.length; i++) {
            if (rad[i].checked) return rad[i].value;
        }
        return "1";
    };

    const body = {
        userId: activeUserId,
        scheduleId: selectedScheduleId,
        acRating: getRadioVal("ac-rating"),
        seatSpacingRating: getRadioVal("spacing-rating"),
        cleanlinessRating: getRadioVal("clean-rating")
    };

    sendPost("/api/rate", body, (res) => {
        if (res.success) {
            showToast(`Comfort Feedback received! Aggregated Index score: ★ ${res.comfortScore.toFixed(2)}/5.0`, "success");
            fetchState();
        } else {
            showToast(res.error || "Security validation failed: customer has no booking record on this bus.", "error");
        }
    });
}

// Delay prediction queries
function queryDelayPrediction() {
    const routeSelect = document.getElementById("predict-route").value;
    const traffic = predictTrafficRange.value;
    const weather = predictWeather.value;

    fetch(`/api/predict?routeId=${routeSelect}&trafficFactor=${traffic}&weather=${weather}&strategy=${currentDelayStrategy}`)
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                predictionResult.classList.remove("hidden");
                predictedMinutes.textContent = data.predictedDelay;
                predictedStrategyName.textContent = `Strategy Active: ${data.strategyUsed}`;
            } else {
                showToast(data.error || "Delay calculation error.", "error");
            }
        });
}

// Emergency Breakdown Rescue Trigger
function triggerBreakdownRescue() {
    if (!confirm("Are you sure you want to trigger a breakdown on SCH-1001? This relocates Alice & Bob to SCH-1002-RESCUE immediately.")) {
        return;
    }

    sendPost("/api/rescue", { scheduleId: "SCH-1001" }, (res) => {
        if (res.success) {
            showToast(`COMPLETED! Relocated ${res.rescuedCount} occupants to rescue Volvo backup bus!`, "success");

            // Screen Shake Effect
            document.body.style.animation = "screen-shake 0.5s ease-in-out";
            setTimeout(() => {
                document.body.style.animation = "";
            }, 600);

            // Trigger Red alert overlay effect temporarily
            const alertFlash = document.createElement("div");
            alertFlash.style.position = "fixed";
            alertFlash.style.top = "0";
            alertFlash.style.left = "0";
            alertFlash.style.width = "100%";
            alertFlash.style.height = "100%";
            alertFlash.style.backgroundColor = "rgba(239, 68, 68, 0.2)";
            alertFlash.style.pointerEvents = "none";
            alertFlash.style.zIndex = "999";
            alertFlash.style.transition = "opacity 0.6s ease";
            document.body.appendChild(alertFlash);
            setTimeout(() => {
                alertFlash.style.opacity = "0";
                setTimeout(() => alertFlash.remove(), 600);
            }, 500);

            // Change selected schedule to rescue schedule to show the changes!
            selectedScheduleId = "SCH-1002-RESCUE";
            scheduleSelect.value = "SCH-1002-RESCUE";

            fetchState();
        } else {
            showToast(res.error || "Rescue command execution exception.", "error");
        }
    });
}

// Helper Send Post request
function sendPost(url, body, callback) {
    fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    })
        .then(res => res.json())
        .then(callback)
        .catch(err => {
            console.error("Post error:", err);
            showToast("Server communication error.", "error");
        });
}

// Helper Show Toast message
function showToast(message, type = "info") {
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;

    let icon = '<i class="fa-solid fa-circle-info"></i>';
    if (type === "success") icon = '<i class="fa-solid fa-circle-check"></i>';
    if (type === "error") icon = '<i class="fa-solid fa-circle-exclamation"></i>';

    toast.innerHTML = `${icon} <span>${message}</span>`;
    toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = "toast-fade-in 0.3s reverse forwards";
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Add CSS keyframes dynamically for breakdown screen shake
const shakeStyle = document.createElement('style');
shakeStyle.innerHTML = `
    @keyframes screen-shake {
        0% { transform: translate(1px, 1px) rotate(0deg); }
        10% { transform: translate(-1px, -2px) rotate(-1deg); }
        20% { transform: translate(-3px, 0px) rotate(1deg); }
        30% { transform: translate(3px, 2px) rotate(0deg); }
        40% { transform: translate(1px, -1px) rotate(1deg); }
        55% { transform: translate(-1px, 2px) rotate(-1deg); }
        70% { transform: translate(-3px, 1px) rotate(0deg); }
        85% { transform: translate(3px, 1px) rotate(-1deg); }
        100% { transform: translate(1px, -2px) rotate(0deg); }
    }
`;
document.head.appendChild(shakeStyle);
