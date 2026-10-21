package com.pricing;

import java.math.BigDecimal;

/**
 * Vehicle categories and their base hourly rate (in rupees, before any
 * charger discount and before GST). The label matches the value stored in
 * tbl_slot.vehicle_type.
 */
public enum VehicleType {

	TWO_WHEELER("2-Wheeler", "25.00"),
	FOUR_WHEELER_NORMAL("4-Wheeler-Normal", "75.00"),
	FOUR_WHEELER_FAST("4-Wheeler-Fast", "200.00");

	private final String label;
	private final BigDecimal hourlyRate;

	VehicleType(String label, String hourlyRate) {
		this.label = label;
		this.hourlyRate = new BigDecimal(hourlyRate);
	}

	public String getLabel() {
		return label;
	}

	public BigDecimal getHourlyRate() {
		return hourlyRate;
	}

	public static VehicleType fromLabel(String label) {
		if (label != null) {
			for (VehicleType type : values()) {
				if (type.label.equalsIgnoreCase(label.trim())) {
					return type;
				}
			}
		}
		throw new IllegalArgumentException("Unknown vehicle type");
	}
}