package com.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Current Plugy pricing:
 * <ul>
 *   <li>base hourly rate by vehicle type (see {@link VehicleType})</li>
 *   <li>10% off the hourly rate for GB/T chargers</li>
 *   <li>price = rate x hours x quantity, then 18% GST on top</li>
 * </ul>
 * Money uses BigDecimal and is rounded to two decimals (half up) at each step,
 * so the total always equals subtotal + GST exactly as shown to the customer.
 */
public class StandardPricingStrategy implements PricingStrategy {

	public static final int[] ALLOWED_DURATIONS_MINUTES = { 30, 60, 120 };
	public static final int MAX_QUANTITY = 10;

	private static final BigDecimal GST_RATE = new BigDecimal("0.18");
	private static final BigDecimal GBT_RATE_FACTOR = new BigDecimal("0.90");
	private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal("60");

	@Override
	public PriceQuote quote(String vehicleType, String chargerType, int durationMinutes, int quantity) {

		VehicleType vehicle = VehicleType.fromLabel(vehicleType);

		if (!isAllowedDuration(durationMinutes)) {
			throw new IllegalArgumentException("Unsupported duration");
		}
		if (quantity < 1 || quantity > MAX_QUANTITY) {
			throw new IllegalArgumentException("Unsupported quantity");
		}

		BigDecimal rate = vehicle.getHourlyRate();
		if (chargerType != null && "GB/T".equalsIgnoreCase(chargerType.trim())) {
			rate = rate.multiply(GBT_RATE_FACTOR);
		}
		rate = rate.setScale(2, RoundingMode.HALF_UP);

		BigDecimal hours = BigDecimal.valueOf(durationMinutes).divide(MINUTES_PER_HOUR, 4, RoundingMode.HALF_UP);

		BigDecimal subtotal = rate.multiply(hours).multiply(BigDecimal.valueOf(quantity))
				.setScale(2, RoundingMode.HALF_UP);
		BigDecimal gst = subtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
		BigDecimal total = subtotal.add(gst);

		return new PriceQuote(rate, durationMinutes, quantity, subtotal, gst, total);
	}

	private static boolean isAllowedDuration(int minutes) {
		for (int allowed : ALLOWED_DURATIONS_MINUTES) {
			if (allowed == minutes) {
				return true;
			}
		}
		return false;
	}
}