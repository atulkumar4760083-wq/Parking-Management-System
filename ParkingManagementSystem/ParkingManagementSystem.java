import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/* ============================================================================
 *                       PARKING MANAGEMENT SYSTEM
 * ============================================================================
 * Core Features:
 *  1. Core Parking Operations:
 *      - Vehicle Entry: Automatic slot assignment, ticket generation
 *      - Vehicle Exit: Duration calculation, dynamic billing, slot release
 *      - Real-time Slot Allocation: Dedicated slots for Cars & Bikes
 *  2. Object-Oriented Principles:
 *      - Abstraction: Vehicle abstract class defining common behavior
 *      - Inheritance: Car and Bike extend Vehicle
 *      - Polymorphism: Dynamic fee calculation overridden per vehicle type
 *      - Encapsulation: Private fields with public getters/setters
 *  3. File Handling (Data Persistence):
 *      - Saves active parked vehicles and completed history using BufferedReader & FileWriter
 *      - Automatically reloads data and restores slot states on program restart
 *  4. Robustness:
 *      - Input validation (regex format check, duplicate detection, empty input check)
 *      - Graceful exception handling (try-catch) to prevent runtime crashes
 * ============================================================================
 */

// ============================================================================
// 1. ENUMS & CONSTANTS
// ============================================================================

enum VehicleType {
    CAR,
    BIKE;

    public static VehicleType fromString(String text) {
        if (text == null) return null;
        for (VehicleType vt : VehicleType.values()) {
            if (vt.name().equalsIgnoreCase(text.trim())) {
                return vt;
            }
        }
        return null;
    }
}

enum SlotStatus {
    AVAILABLE,
    OCCUPIED
}

// ============================================================================
// 2. OOP ABSTRACTION & INHERITANCE (Vehicle, Car, Bike)
// ============================================================================

/**
 * Abstract Vehicle class demonstrating Abstraction and Encapsulation.
 */
abstract class Vehicle {
    private String vehicleNumber;
    private String ownerName;
    private VehicleType type;

    public Vehicle(String vehicleNumber, String ownerName, VehicleType type) {
        this.vehicleNumber = vehicleNumber.toUpperCase().trim();
        this.ownerName = ownerName.trim();
        this.type = type;
    }

    // Encapsulation: Getters and Setters
    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    /**
     * Polymorphic method to calculate parking fee based on duration.
     * Each vehicle subclass overrides this to define custom rates.
     *
     * @param durationMinutes Total parked duration in minutes.
     * @return Total calculated parking fee in INR (Rs.).
     */
    public abstract double calculateFee(long durationMinutes);

    /**
     * Returns a human-readable rate card description.
     */
    public abstract String getRateCard();

    @Override
    public String toString() {
        return String.format("[%s] %s | Owner: %s", type, vehicleNumber, ownerName);
    }
}

/**
 * Car class extending Vehicle (Inheritance & Polymorphism)
 * Pricing: Rs. 30 for the first hour (base rate), then Rs. 20 per hour.
 */
class Car extends Vehicle {
    public static final double BASE_RATE = 30.0;    // First hour
    public static final double HOURLY_RATE = 20.0;  // Per subsequent hour

    public Car(String vehicleNumber, String ownerName) {
        super(vehicleNumber, ownerName, VehicleType.CAR);
    }

    @Override
    public double calculateFee(long durationMinutes) {
        if (durationMinutes <= 0) durationMinutes = 1; // Minimum charge for 1 minute
        long hours = (long) Math.ceil((double) durationMinutes / 60.0);
        if (hours <= 1) {
            return BASE_RATE;
        }
        return BASE_RATE + (hours - 1) * HOURLY_RATE;
    }

    @Override
    public String getRateCard() {
        return "Rs. 30 (1st hr) + Rs. 20/hr thereafter";
    }
}

/**
 * Bike class extending Vehicle (Inheritance & Polymorphism)
 * Pricing: Rs. 15 for the first hour (base rate), then Rs. 10 per hour.
 */
class Bike extends Vehicle {
    public static final double BASE_RATE = 15.0;    // First hour
    public static final double HOURLY_RATE = 10.0;  // Per subsequent hour

    public Bike(String vehicleNumber, String ownerName) {
        super(vehicleNumber, ownerName, VehicleType.BIKE);
    }

    @Override
    public double calculateFee(long durationMinutes) {
        if (durationMinutes <= 0) durationMinutes = 1; // Minimum charge
        long hours = (long) Math.ceil((double) durationMinutes / 60.0);
        if (hours <= 1) {
            return BASE_RATE;
        }
        return BASE_RATE + (hours - 1) * HOURLY_RATE;
    }

    @Override
    public String getRateCard() {
        return "Rs. 15 (1st hr) + Rs. 10/hr thereafter";
    }
}

// ============================================================================
// 3. PARKING SLOT & TICKET
// ============================================================================

/**
 * Represents an individual parking slot in the lot.
 */
class ParkingSlot {
    private int slotNumber;
    private VehicleType supportedType;
    private SlotStatus status;
    private Vehicle parkedVehicle;

    public ParkingSlot(int slotNumber, VehicleType supportedType) {
        this.slotNumber = slotNumber;
        this.supportedType = supportedType;
        this.status = SlotStatus.AVAILABLE;
        this.parkedVehicle = null;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public VehicleType getSupportedType() {
        return supportedType;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public boolean isAvailable() {
        return this.status == SlotStatus.AVAILABLE;
    }

    public void park(Vehicle vehicle) {
        this.parkedVehicle = vehicle;
        this.status = SlotStatus.OCCUPIED;
    }

    public void vacate() {
        this.parkedVehicle = null;
        this.status = SlotStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        String formattedSlot = (supportedType == VehicleType.CAR ? "C-" : "B-") + slotNumber;
        if (status == SlotStatus.AVAILABLE) {
            return String.format("Slot %-5s | Type: %-4s | Status: [AVAILABLE]", formattedSlot, supportedType);
        } else {
            return String.format("Slot %-5s | Type: %-4s | Status: [OCCUPIED] by %s (%s)",
                    formattedSlot, supportedType, parkedVehicle.getVehicleNumber(), parkedVehicle.getOwnerName());
        }
    }
}

/**
 * Represents a Parking Ticket issued upon entry and settled upon exit.
 */
class Ticket {
    private String ticketId;
    private String vehicleNumber;
    private String ownerName;
    private VehicleType vehicleType;
    private int slotNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double feePaid;
    private boolean active;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Constructor for a new entry
    public Ticket(String ticketId, Vehicle vehicle, int slotNumber, LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.vehicleNumber = vehicle.getVehicleNumber();
        this.ownerName = vehicle.getOwnerName();
        this.vehicleType = vehicle.getType();
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
        this.exitTime = null;
        this.feePaid = 0.0;
        this.active = true;
    }

    // Constructor for loading from file storage
    public Ticket(String ticketId, String vehicleNumber, String ownerName, VehicleType vehicleType,
                  int slotNumber, LocalDateTime entryTime, LocalDateTime exitTime, double feePaid, boolean active) {
        this.ticketId = ticketId;
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.vehicleType = vehicleType;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.feePaid = feePaid;
        this.active = active;
    }

    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getOwnerName() { return ownerName; }
    public VehicleType getVehicleType() { return vehicleType; }
    public int getSlotNumber() { return slotNumber; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    public double getFeePaid() { return feePaid; }
    public void setFeePaid(double feePaid) { this.feePaid = feePaid; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getDurationMinutes(LocalDateTime currentOrExitTime) {
        if (entryTime == null || currentOrExitTime == null) return 0;
        return Duration.between(entryTime, currentOrExitTime).toMinutes();
    }

    public String getFormattedEntryTime() {
        return entryTime != null ? entryTime.format(FORMATTER) : "N/A";
    }

    public String getFormattedExitTime() {
        return exitTime != null ? exitTime.format(FORMATTER) : "Active";
    }

    /**
     * Converts ticket to a comma-separated format for persistence.
     */
    public String toCsvLine() {
        String exitStr = (exitTime != null) ? exitTime.format(FORMATTER) : "NULL";
        return String.join(",",
                ticketId,
                vehicleNumber,
                ownerName,
                vehicleType.name(),
                String.valueOf(slotNumber),
                entryTime.format(FORMATTER),
                exitStr,
                String.format(Locale.US, "%.2f", feePaid),
                String.valueOf(active)
        );
    }

    /**
     * Parses a CSV line back into a Ticket object.
     */
    public static Ticket fromCsvLine(String line) {
        try {
            String[] parts = line.split(",", -1);
            if (parts.length < 9) return null;

            String ticketId = parts[0].trim();
            String vehicleNumber = parts[1].trim();
            String ownerName = parts[2].trim();
            VehicleType type = VehicleType.fromString(parts[3].trim());
            int slotNumber = Integer.parseInt(parts[4].trim());
            LocalDateTime entry = LocalDateTime.parse(parts[5].trim(), FORMATTER);
            LocalDateTime exit = parts[6].trim().equalsIgnoreCase("NULL") ? null : LocalDateTime.parse(parts[6].trim(), FORMATTER);
            double fee = Double.parseDouble(parts[7].trim());
            boolean active = Boolean.parseBoolean(parts[8].trim());

            return new Ticket(ticketId, vehicleNumber, ownerName, type, slotNumber, entry, exit, fee, active);
        } catch (Exception e) {
            return null; // Ignore corrupted line
        }
    }
}

// ============================================================================
// 4. PARKING LOT MANAGEMENT
// ============================================================================

/**
 * Manages physical parking slots and automatic slot allocation.
 */
class ParkingLot {
    private final Map<Integer, ParkingSlot> carSlots;
    private final Map<Integer, ParkingSlot> bikeSlots;
    private final int totalCarSlots;
    private final int totalBikeSlots;

    public ParkingLot(int totalCarSlots, int totalBikeSlots) {
        this.totalCarSlots = totalCarSlots;
        this.totalBikeSlots = totalBikeSlots;
        this.carSlots = new LinkedHashMap<>();
        this.bikeSlots = new LinkedHashMap<>();

        // Initialize Car slots (1 to totalCarSlots)
        for (int i = 1; i <= totalCarSlots; i++) {
            carSlots.put(i, new ParkingSlot(i, VehicleType.CAR));
        }

        // Initialize Bike slots (1 to totalBikeSlots)
        for (int i = 1; i <= totalBikeSlots; i++) {
            bikeSlots.put(i, new ParkingSlot(i, VehicleType.BIKE));
        }
    }

    /**
     * Finds and allocates the first available slot matching the vehicle type.
     */
    public synchronized ParkingSlot allocateSlot(VehicleType type) {
        Map<Integer, ParkingSlot> targetMap = (type == VehicleType.CAR) ? carSlots : bikeSlots;
        for (ParkingSlot slot : targetMap.values()) {
            if (slot.isAvailable()) {
                return slot;
            }
        }
        return null; // All slots full
    }

    public ParkingSlot getSlot(VehicleType type, int slotNumber) {
        if (type == VehicleType.CAR) {
            return carSlots.get(slotNumber);
        } else {
            return bikeSlots.get(slotNumber);
        }
    }

    public synchronized boolean freeSlot(VehicleType type, int slotNumber) {
        ParkingSlot slot = getSlot(type, slotNumber);
        if (slot != null && !slot.isAvailable()) {
            slot.vacate();
            return true;
        }
        return false;
    }

    public int getAvailableCount(VehicleType type) {
        Map<Integer, ParkingSlot> targetMap = (type == VehicleType.CAR) ? carSlots : bikeSlots;
        int count = 0;
        for (ParkingSlot slot : targetMap.values()) {
            if (slot.isAvailable()) count++;
        }
        return count;
    }

    public int getOccupiedCount(VehicleType type) {
        return getTotalSlots(type) - getAvailableCount(type);
    }

    public int getTotalSlots(VehicleType type) {
        return (type == VehicleType.CAR) ? totalCarSlots : totalBikeSlots;
    }

    public Collection<ParkingSlot> getAllSlots(VehicleType type) {
        return (type == VehicleType.CAR) ? carSlots.values() : bikeSlots.values();
    }
}

// ============================================================================
// 5. BILLING & RECEIPT GENERATOR
// ============================================================================

/**
 * Handles parking receipt generation and invoice display.
 */
class Billing {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    public static void printInvoice(Ticket ticket, Vehicle vehicle, LocalDateTime exitTime, double fee, long durationMinutes) {
        long hours = durationMinutes / 60;
        long mins = durationMinutes % 60;

        System.out.println("\n+-------------------------------------------------------------+");
        System.out.println("|                     PARKING RECEIPT / BILL                  |");
        System.out.println("+-------------------------------------------------------------+");
        System.out.printf("| Ticket ID     : %-43s |\n", ticket.getTicketId());
        System.out.printf("| Vehicle Number: %-43s |\n", ticket.getVehicleNumber());
        System.out.printf("| Owner Name    : %-43s |\n", ticket.getOwnerName());
        System.out.printf("| Vehicle Type  : %-43s |\n", ticket.getVehicleType());
        System.out.printf("| Slot Allocated: %-43s |\n", (ticket.getVehicleType() == VehicleType.CAR ? "C-" : "B-") + ticket.getSlotNumber());
        System.out.println("+-------------------------------------------------------------+");
        System.out.printf("| Entry Time    : %-43s |\n", ticket.getEntryTime().format(TIME_FMT));
        System.out.printf("| Exit Time     : %-43s |\n", exitTime.format(TIME_FMT));
        System.out.printf("| Duration      : %-43s |\n", String.format("%d hr %d min (Total %d mins)", hours, mins, durationMinutes));
        System.out.printf("| Rate Applied  : %-43s |\n", vehicle.getRateCard());
        System.out.println("+-------------------------------------------------------------+");
        System.out.printf("| TOTAL FEE     : Rs. %-39.2f |\n", fee);
        System.out.println("+-------------------------------------------------------------+");
        System.out.println("|            Thank you for parking with us! Drive Safe!       |");
        System.out.println("+-------------------------------------------------------------+\n");
    }

    public static void printTicketReceipt(Ticket ticket) {
        System.out.println("\n+-------------------------------------------------------------+");
        System.out.println("|                     PARKING ENTRY TICKET                    |");
        System.out.println("+-------------------------------------------------------------+");
        System.out.printf("| Ticket ID     : %-43s |\n", ticket.getTicketId());
        System.out.printf("| Vehicle Number: %-43s |\n", ticket.getVehicleNumber());
        System.out.printf("| Owner Name    : %-43s |\n", ticket.getOwnerName());
        System.out.printf("| Vehicle Type  : %-43s |\n", ticket.getVehicleType());
        System.out.printf("| Assigned Slot : %-43s |\n", (ticket.getVehicleType() == VehicleType.CAR ? "C-" : "B-") + ticket.getSlotNumber());
        System.out.printf("| Entry Time    : %-43s |\n", ticket.getEntryTime().format(TIME_FMT));
        System.out.println("+-------------------------------------------------------------+");
        System.out.println("| Please keep this ticket safe until exit. Have a nice day!   |");
        System.out.println("+-------------------------------------------------------------+\n");
    }
}

// ============================================================================
// 6. FILE HANDLING (BufferedReader, FileWriter, Scanner)
// ============================================================================

/**
 * Handles persistent data storage using Java I/O streams.
 */
class FileHandler {
    private final String activeFile;
    private final String historyFile;

    public FileHandler(String activeFile, String historyFile) {
        this.activeFile = activeFile;
        this.historyFile = historyFile;
        initializeFiles();
    }

    private void initializeFiles() {
        try {
            File f1 = new File(activeFile);
            File f2 = new File(historyFile);
            if (f1.getParentFile() != null) f1.getParentFile().mkdirs();
            if (!f1.exists()) f1.createNewFile();
            if (!f2.exists()) f2.createNewFile();
        } catch (IOException e) {
            System.err.println("[Warning] Failed to initialize storage files: " + e.getMessage());
        }
    }

    /**
     * Loads currently active tickets from file using BufferedReader
     * and automatically restores slot statuses in the ParkingLot.
     */
    public Map<String, Ticket> loadActiveTickets(ParkingLot parkingLot) {
        Map<String, Ticket> activeTickets = new HashMap<>();
        File file = new File(activeFile);
        if (!file.exists()) return activeTickets;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Ticket ticket = Ticket.fromCsvLine(line);
                if (ticket != null && ticket.isActive()) {
                    activeTickets.put(ticket.getVehicleNumber(), ticket);

                    // Reconstruct vehicle instance & occupy corresponding slot
                    Vehicle vehicle;
                    if (ticket.getVehicleType() == VehicleType.CAR) {
                        vehicle = new Car(ticket.getVehicleNumber(), ticket.getOwnerName());
                    } else {
                        vehicle = new Bike(ticket.getVehicleNumber(), ticket.getOwnerName());
                    }

                    ParkingSlot slot = parkingLot.getSlot(ticket.getVehicleType(), ticket.getSlotNumber());
                    if (slot != null) {
                        slot.park(vehicle);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[Error] Failed to read active parking data: " + e.getMessage());
        }
        return activeTickets;
    }

    /**
     * Writes all currently parked vehicles to file using FileWriter / BufferedWriter.
     */
    public synchronized void saveActiveTickets(Collection<Ticket> tickets) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(activeFile, false))) {
            for (Ticket ticket : tickets) {
                if (ticket.isActive()) {
                    writer.write(ticket.toCsvLine());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("[Error] Failed to save active tickets: " + e.getMessage());
        }
    }

    /**
     * Appends a completed parking session to history file.
     */
    public synchronized void appendHistory(Ticket ticket) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile, true))) {
            writer.write(ticket.toCsvLine());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("[Error] Failed to save history entry: " + e.getMessage());
        }
    }

    /**
     * Reads all historical parking sessions from file using BufferedReader.
     */
    public List<Ticket> loadHistory() {
        List<Ticket> history = new ArrayList<>();
        File file = new File(historyFile);
        if (!file.exists()) return history;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Ticket ticket = Ticket.fromCsvLine(line);
                if (ticket != null) {
                    history.add(ticket);
                }
            }
        } catch (IOException e) {
            System.err.println("[Error] Failed to read history data: " + e.getMessage());
        }
        return history;
    }
}

// ============================================================================
// 7. ADMIN & SERVICE CONTROLLER
// ============================================================================

/**
 * Admin class for administrative insights, revenue metrics, and overview.
 */
class Admin {
    private String adminName;

    public Admin(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminName() {
        return adminName;
    }

    public void displaySystemSummary(ParkingLot lot, int activeCount, List<Ticket> history) {
        double totalRevenue = 0.0;
        for (Ticket t : history) {
            totalRevenue += t.getFeePaid();
        }

        System.out.println("\n=======================================================");
        System.out.println("             ADMIN SYSTEM OVERVIEW & METRICS           ");
        System.out.println("=======================================================");
        System.out.println(" Administrator        : " + adminName);
        System.out.printf(" Car Slots Capacity   : %d (Occupied: %d, Available: %d)\n",
                lot.getTotalSlots(VehicleType.CAR), lot.getOccupiedCount(VehicleType.CAR), lot.getAvailableCount(VehicleType.CAR));
        System.out.printf(" Bike Slots Capacity  : %d (Occupied: %d, Available: %d)\n",
                lot.getTotalSlots(VehicleType.BIKE), lot.getOccupiedCount(VehicleType.BIKE), lot.getAvailableCount(VehicleType.BIKE));
        System.out.println(" Currently Parked     : " + activeCount + " vehicle(s)");
        System.out.println(" Total Completed Exits: " + history.size());
        System.out.printf(" Total Revenue Earned : Rs. %.2f\n", totalRevenue);
        System.out.println("=======================================================\n");
    }
}

/**
 * Central Parking Service coordinating operations between Lot, Persistence, and Billing.
 */
class ParkingService {
    private final ParkingLot parkingLot;
    private final FileHandler fileHandler;
    private final Map<String, Ticket> activeTickets; // Key: vehicleNumber (Uppercase)
    private final Admin admin;

    public ParkingService(int carCapacity, int bikeCapacity, String activeFile, String historyFile) {
        this.parkingLot = new ParkingLot(carCapacity, bikeCapacity);
        this.fileHandler = new FileHandler(activeFile, historyFile);
        this.activeTickets = fileHandler.loadActiveTickets(this.parkingLot);
        this.admin = new Admin("SuperAdmin");
    }

    /**
     * Records new vehicle entry.
     */
    public synchronized boolean parkVehicle(String vehicleNumber, String ownerName, VehicleType type) {
        vehicleNumber = vehicleNumber.toUpperCase().trim();

        // 1. Validation: Prevent duplicate active parking
        if (activeTickets.containsKey(vehicleNumber)) {
            System.out.printf("\n[Error] Vehicle '%s' is ALREADY PARKED in the lot!\n", vehicleNumber);
            return false;
        }

        // 2. Allocate Slot
        ParkingSlot availableSlot = parkingLot.allocateSlot(type);
        if (availableSlot == null) {
            System.out.printf("\n[Sorry] Parking Full! No available slots for %s at this moment.\n", type);
            return false;
        }

        // 3. Create Vehicle instance (Polymorphism)
        Vehicle vehicle;
        if (type == VehicleType.CAR) {
            vehicle = new Car(vehicleNumber, ownerName);
        } else {
            vehicle = new Bike(vehicleNumber, ownerName);
        }

        // 4. Mark slot as occupied
        availableSlot.park(vehicle);

        // 5. Generate Ticket
        String ticketId = "TCK-" + System.currentTimeMillis() % 1000000;
        LocalDateTime entryTime = LocalDateTime.now();
        Ticket ticket = new Ticket(ticketId, vehicle, availableSlot.getSlotNumber(), entryTime);

        // 6. Update in-memory & file storage
        activeTickets.put(vehicleNumber, ticket);
        fileHandler.saveActiveTickets(activeTickets.values());

        // 7. Print Ticket Receipt
        System.out.println("\n[Success] Vehicle Parked Successfully!");
        Billing.printTicketReceipt(ticket);
        return true;
    }

    /**
     * Records vehicle exit, frees slot, computes dynamic fee, and updates history.
     */
    public synchronized boolean exitVehicle(String vehicleNumber) {
        vehicleNumber = vehicleNumber.toUpperCase().trim();

        // 1. Check if vehicle is parked
        if (!activeTickets.containsKey(vehicleNumber)) {
            System.out.printf("\n[Error] No active parked vehicle found with number '%s'.\n", vehicleNumber);
            return false;
        }

        Ticket ticket = activeTickets.get(vehicleNumber);
        LocalDateTime exitTime = LocalDateTime.now();
        ticket.setExitTime(exitTime);

        // Calculate duration in minutes
        long durationMinutes = ticket.getDurationMinutes(exitTime);

        // Instantiate vehicle for polymorphic fee calculation
        Vehicle vehicle;
        if (ticket.getVehicleType() == VehicleType.CAR) {
            vehicle = new Car(ticket.getVehicleNumber(), ticket.getOwnerName());
        } else {
            vehicle = new Bike(ticket.getVehicleNumber(), ticket.getOwnerName());
        }

        double fee = vehicle.calculateFee(durationMinutes);
        ticket.setFeePaid(fee);
        ticket.setActive(false);

        // 2. Free the parking slot
        parkingLot.freeSlot(ticket.getVehicleType(), ticket.getSlotNumber());

        // 3. Update persistence
        activeTickets.remove(vehicleNumber);
        fileHandler.saveActiveTickets(activeTickets.values());
        fileHandler.appendHistory(ticket);

        // 4. Print Billing Invoice
        Billing.printInvoice(ticket, vehicle, exitTime, fee, durationMinutes);
        return true;
    }

    /**
     * Displays real-time available slots count and detailed slot map.
     */
    public void displayAvailableSlots() {
        System.out.println("\n=======================================================");
        System.out.println("               REAL-TIME PARKING SLOTS STATUS          ");
        System.out.println("=======================================================");
        System.out.printf("  CAR SLOTS  : Available: %d / %d  |  Occupied: %d\n",
                parkingLot.getAvailableCount(VehicleType.CAR),
                parkingLot.getTotalSlots(VehicleType.CAR),
                parkingLot.getOccupiedCount(VehicleType.CAR));
        System.out.printf("  BIKE SLOTS : Available: %d / %d  |  Occupied: %d\n",
                parkingLot.getAvailableCount(VehicleType.BIKE),
                parkingLot.getTotalSlots(VehicleType.BIKE),
                parkingLot.getOccupiedCount(VehicleType.BIKE));
        System.out.println("-------------------------------------------------------");
        System.out.println("--- CAR SLOTS DETAIL ---");
        for (ParkingSlot slot : parkingLot.getAllSlots(VehicleType.CAR)) {
            System.out.println("  " + slot);
        }
        System.out.println("\n--- BIKE SLOTS DETAIL ---");
        for (ParkingSlot slot : parkingLot.getAllSlots(VehicleType.BIKE)) {
            System.out.println("  " + slot);
        }
        System.out.println("=======================================================\n");
    }

    /**
     * Displays all currently parked vehicles with elapsed time.
     */
    public void displayParkedVehicles() {
        System.out.println("\n=========================================================================================================");
        System.out.println("                                      CURRENTLY PARKED VEHICLES                                          ");
        System.out.println("=========================================================================================================");
        if (activeTickets.isEmpty()) {
            System.out.println("                     No vehicles are currently parked in the parking lot.                                ");
            System.out.println("=========================================================================================================\n");
            return;
        }

        System.out.printf("%-10s | %-14s | %-16s | %-6s | %-6s | %-20s | %-14s\n",
                "Ticket ID", "Vehicle No", "Owner Name", "Type", "Slot", "Entry Time", "Parked For");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        LocalDateTime now = LocalDateTime.now();
        for (Ticket t : activeTickets.values()) {
            long mins = t.getDurationMinutes(now);
            String durationStr = String.format("%d hr %d min", mins / 60, mins % 60);
            String slotStr = (t.getVehicleType() == VehicleType.CAR ? "C-" : "B-") + t.getSlotNumber();
            System.out.printf("%-10s | %-14s | %-16s | %-6s | %-6s | %-20s | %-14s\n",
                    t.getTicketId(), t.getVehicleNumber(), t.getOwnerName(), t.getVehicleType(), slotStr, t.getFormattedEntryTime(), durationStr);
        }
        System.out.println("=========================================================================================================\n");
    }

    /**
     * Displays completed parking history from file.
     */
    public void displayParkingHistory() {
        List<Ticket> history = fileHandler.loadHistory();
        System.out.println("\n============================================================================================================================");
        System.out.println("                                                    PARKING TRANSACTION HISTORY                                             ");
        System.out.println("============================================================================================================================");
        if (history.isEmpty()) {
            System.out.println("                                                No past parking records found.                                              ");
            System.out.println("============================================================================================================================\n");
            return;
        }

        System.out.printf("%-10s | %-13s | %-15s | %-5s | %-5s | %-19s | %-19s | %-11s\n",
                "Ticket ID", "Vehicle No", "Owner Name", "Type", "Slot", "Entry Time", "Exit Time", "Fee Paid");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------");

        for (Ticket t : history) {
            String slotStr = (t.getVehicleType() == VehicleType.CAR ? "C-" : "B-") + t.getSlotNumber();
            System.out.printf("%-10s | %-13s | %-15s | %-5s | %-5s | %-19s | %-19s | Rs. %-7.2f\n",
                    t.getTicketId(), t.getVehicleNumber(), t.getOwnerName(), t.getVehicleType(), slotStr,
                    t.getFormattedEntryTime(), t.getFormattedExitTime(), t.getFeePaid());
        }
        System.out.println("============================================================================================================================\n");
    }

    /**
     * Admin view summary.
     */
    public void displayAdminReport() {
        List<Ticket> history = fileHandler.loadHistory();
        admin.displaySystemSummary(parkingLot, activeTickets.size(), history);
    }
}

// ============================================================================
// 8. MAIN CLASS & CONSOLE USER INTERFACE
// ============================================================================

public class ParkingManagementSystem {

    // Regex for Vehicle Registration Number (Allows alphanumeric formats 4-15 chars)
    private static final String VEHICLE_REGEX = "^[A-Z0-9 -]{4,15}$";

    public static void main(String[] args) {
        // Capacity Configuration: 10 Car slots, 10 Bike slots
        final int CAR_CAPACITY = 10;
        final int BIKE_CAPACITY = 10;
        final String ACTIVE_DATA_FILE = "parking_active_data.txt";
        final String HISTORY_DATA_FILE = "parking_history_data.txt";

        ParkingService service = new ParkingService(CAR_CAPACITY, BIKE_CAPACITY, ACTIVE_DATA_FILE, HISTORY_DATA_FILE);
        Scanner scanner = new Scanner(System.in);

        System.out.println("===============================================================");
        System.out.println("        WELCOME TO SMART PARKING MANAGEMENT SYSTEM            ");
        System.out.println("===============================================================");
        System.out.println(" Initialized with " + CAR_CAPACITY + " Car slots and " + BIKE_CAPACITY + " Bike slots.");
        System.out.println(" Persistent storage connected: " + ACTIVE_DATA_FILE + ", " + HISTORY_DATA_FILE);

        boolean running = true;
        while (running) {
            try {
                printMainMenu();
                System.out.print("Enter your choice (1-7): ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("[!] Please enter a valid option.\n");
                    continue;
                }

                int choice;
                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("[!] Invalid choice! Please enter a number between 1 and 7.\n");
                    continue;
                }

                switch (choice) {
                    case 1:
                        handleVehicleEntry(scanner, service);
                        break;
                    case 2:
                        handleVehicleExit(scanner, service);
                        break;
                    case 3:
                        service.displayAvailableSlots();
                        break;
                    case 4:
                        service.displayParkedVehicles();
                        break;
                    case 5:
                        service.displayParkingHistory();
                        break;
                    case 6:
                        service.displayAdminReport();
                        break;
                    case 7:
                        System.out.println("\nThank you for using Smart Parking Management System. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("[!] Invalid option! Please select between 1 and 7.\n");
                        break;
                }
            } catch (Exception e) {
                System.out.println("[Exception] An unexpected error occurred: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }

        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("\n+-------------------------------------------------------------+");
        System.out.println("|                     PARKING MAIN MENU                       |");
        System.out.println("+-------------------------------------------------------------+");
        System.out.println("|  1. Vehicle Entry  (Park Vehicle)                           |");
        System.out.println("|  2. Vehicle Exit   (Unpark & Generate Bill)                 |");
        System.out.println("|  3. View Available Slots                                    |");
        System.out.println("|  4. View All Parked Vehicles                                |");
        System.out.println("|  5. View Parking History                                    |");
        System.out.println("|  6. View Admin Analytics & Summary                          |");
        System.out.println("|  7. Exit Program                                            |");
        System.out.println("+-------------------------------------------------------------+");
    }

    private static void handleVehicleEntry(Scanner scanner, ParkingService service) {
        System.out.println("\n--- [1] VEHICLE ENTRY ---");

        // 1. Vehicle Number Validation
        String vehicleNumber = "";
        while (true) {
            System.out.print("Enter Vehicle Number (e.g., DL01AB1234): ");
            vehicleNumber = scanner.nextLine().trim();
            if (vehicleNumber.isEmpty()) {
                System.out.println("[!] Vehicle number cannot be empty. Try again.");
                continue;
            }
            if (!vehicleNumber.toUpperCase().matches(VEHICLE_REGEX)) {
                System.out.println("[!] Invalid vehicle number format. Alphanumeric characters only (4-15 chars).");
                continue;
            }
            break;
        }

        // 2. Vehicle Type Validation
        VehicleType type = null;
        while (type == null) {
            System.out.print("Enter Vehicle Type (1 for CAR, 2 for BIKE): ");
            String typeInput = scanner.nextLine().trim();
            if (typeInput.equals("1") || typeInput.equalsIgnoreCase("CAR")) {
                type = VehicleType.CAR;
            } else if (typeInput.equals("2") || typeInput.equalsIgnoreCase("BIKE")) {
                type = VehicleType.BIKE;
            } else {
                System.out.println("[!] Invalid vehicle type. Please enter 1 (Car) or 2 (Bike).");
            }
        }

        // 3. Owner Name Validation
        String ownerName = "";
        while (true) {
            System.out.print("Enter Owner Name: ");
            ownerName = scanner.nextLine().trim();
            if (ownerName.isEmpty()) {
                System.out.println("[!] Owner name cannot be empty. Try again.");
                continue;
            }
            break;
        }

        // 4. Park Vehicle
        service.parkVehicle(vehicleNumber, ownerName, type);
    }

    private static void handleVehicleExit(Scanner scanner, ParkingService service) {
        System.out.println("\n--- [2] VEHICLE EXIT & BILLING ---");
        System.out.print("Enter Vehicle Number to exit: ");
        String vehicleNumber = scanner.nextLine().trim();

        if (vehicleNumber.isEmpty()) {
            System.out.println("[!] Vehicle number cannot be empty.");
            return;
        }

        service.exitVehicle(vehicleNumber);
    }
}
