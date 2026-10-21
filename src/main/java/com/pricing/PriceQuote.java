package com.pricing;

import java.math.BigDecimal;

/** Immutable result of a price calculation. All amounts have two decimal places. */
public final class PriceQuote {

	private final BigDecimal hourlyRate;
	private final int durationMinutes;
	private final int quantity;
	private final BigDecimal subtotal;
	private final BigDecimal gst;
	private final BigDecimal total;

	public PriceQuote(BigDecimal hourlyRate, int durationMinutes, int quantity,
			BigDecimal subtotal, BigDecimal gst, BigDecimal total) {
		this.hourlyRate = hourlyRate;
		this.durationMinutes = durationMinutes;
		this.quantity = quantity;
		this.subtotal = subtotal;
		this.gst = gst;
		this.total = total;
	}

	/** Hourly rate after the charger discount, before GST. */
	public BigDecimal getHourlyRate() { return hourlyRate; }
	public int getDurationMinutes() { return durationMinutes; }
	public int getQuantity() { return quantity; }
	public BigDecimal getSubtotal() { return subtotal; }
	public BigDecimal getGst() { return gst; }
	/** Amount the customer pays: subtotal plus GST. */
	public BigDecimal getTotal() { return total; }
}