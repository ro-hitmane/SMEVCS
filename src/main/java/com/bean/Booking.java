package com.bean;

import java.sql.Timestamp;

public class Booking {
	private int id;
	private String ownerName;
	private String vehicleNumber;
	private String batteryCapacity;
	private String amount;
	private String payMode;
	private String email;
	private int slotId;
	private String slotLabel;
	private String vehicleType;
	private String chargerType;
	private Timestamp startTime;
	private Timestamp endTime;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public String getOwnerName() { return ownerName; }
	public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
	public String getVehicleNumber() { return vehicleNumber; }
	public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
	public String getBatteryCapacity() { return batteryCapacity; }
	public void setBatteryCapacity(String batteryCapacity) { this.batteryCapacity = batteryCapacity; }
	public String getAmount() { return amount; }
	public void setAmount(String amount) { this.amount = amount; }
	public String getPayMode() { return payMode; }
	public void setPayMode(String payMode) { this.payMode = payMode; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public int getSlotId() { return slotId; }
	public void setSlotId(int slotId) { this.slotId = slotId; }
	public String getSlotLabel() { return slotLabel; }
	public void setSlotLabel(String slotLabel) { this.slotLabel = slotLabel; }
	public String getVehicleType() { return vehicleType; }
	public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
	public String getChargerType() { return chargerType; }
	public void setChargerType(String chargerType) { this.chargerType = chargerType; }
	public Timestamp getStartTime() { return startTime; }
	public void setStartTime(Timestamp startTime) { this.startTime = startTime; }
	public Timestamp getEndTime() { return endTime; }
	public void setEndTime(Timestamp endTime) { this.endTime = endTime; }
}