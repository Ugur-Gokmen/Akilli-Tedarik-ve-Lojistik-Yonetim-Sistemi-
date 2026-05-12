package com.project.domain.facility;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class Facility {

    @Id
    private String id;
    private String name;
    private String location;
    private String type; // e.g., "Depot", "Port", "Hub"
    private int currentCapacity;
    private int maxCapacity;
    private String managerName;
    private String status; // "OPTIMAL", "NEAR_CAPACITY", "MAINTENANCE"

    public Facility() {
        this.id = UUID.randomUUID().toString();
    }

    public Facility(String name, String location, String type, int currentCapacity, int maxCapacity, String managerName, String status) {
        this();
        this.name = name;
        this.location = location;
        this.type = type;
        this.currentCapacity = currentCapacity;
        this.maxCapacity = maxCapacity;
        this.managerName = managerName;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getCurrentCapacity() { return currentCapacity; }
    public void setCurrentCapacity(int currentCapacity) { this.currentCapacity = currentCapacity; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getUsagePercentage() {
        if (maxCapacity == 0) return 0;
        return (int) ((currentCapacity / (double) maxCapacity) * 100);
    }
}
