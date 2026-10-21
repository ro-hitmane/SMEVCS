package com.pricing;

/**
 * Decides what a booking costs. The server is the only place that calls this,
 * so the amount can never come from the browser. Other pricing rules (peak
 * hours, member discounts) can be added as new implementations.
 */
public interface PricingStrategy {

	/**
	 * @param vehicleType     value of tbl_slot.vehicle_type, for example "4-Wheeler-Fast"
	 * @param chargerType     value of tbl_slot.charger_type, for example "CCS2"
	 * @param durationMinutes length of the booking in minutes
	 * @param quantity        number of slots being priced (1 for a real booking)
	 * @throws IllegalArgumentException if any input cannot be priced
	 */
	PriceQuote quote(String vehicleType, String chargerType, int durationMinutes, int quantity);
}