package com.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pricing.PriceQuote;
import com.pricing.PricingStrategy;
import com.pricing.StandardPricingStrategy;

/**
 * Returns a price estimate as JSON for the search page preview, so the
 * rates live only in the pricing package and not in JavaScript.
 *
 * GET /PriceQuote?vehicleType=4-Wheeler-Fast&portType=CCS2&duration=60&quantity=1
 */
@WebServlet("/PriceQuote")
public class PriceQuoteController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final PricingStrategy pricing = new StandardPricingStrategy();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-store");
		PrintWriter out = response.getWriter();

		try {
			int duration = Integer.parseInt(valueOrDefault(request.getParameter("duration"), "60"));
			int quantity = Integer.parseInt(valueOrDefault(request.getParameter("quantity"), "1"));

			PriceQuote q = pricing.quote(request.getParameter("vehicleType"), request.getParameter("portType"),
					duration, quantity);

			out.print("{\"hourlyRate\":\"" + q.getHourlyRate().toPlainString()
					+ "\",\"durationMinutes\":" + q.getDurationMinutes()
					+ ",\"quantity\":" + q.getQuantity()
					+ ",\"subtotal\":\"" + q.getSubtotal().toPlainString()
					+ "\",\"gst\":\"" + q.getGst().toPlainString()
					+ "\",\"total\":\"" + q.getTotal().toPlainString() + "\"}");
		} catch (IllegalArgumentException e) {
			// NumberFormatException is an IllegalArgumentException too
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print("{\"error\":\"Invalid pricing request\"}");
		}
	}

	private static String valueOrDefault(String value, String fallback) {
		return (value == null || value.trim().isEmpty()) ? fallback : value.trim();
	}
}