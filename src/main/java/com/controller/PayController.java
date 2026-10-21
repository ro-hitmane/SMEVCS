package com.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bean.Pay;
import com.bean.Slot;
import com.pricing.PriceQuote;
import com.pricing.PricingStrategy;
import com.pricing.StandardPricingStrategy;
import com.dao.BookingDao;
import com.dao.BookingResult;

@WebServlet("/PayController")
public class PayController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final PricingStrategy pricing = new StandardPricingStrategy();

	public PayController() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		String ownername = clean(request.getParameter("ownername"));
		String number = clean(request.getParameter("number"));
		String bat = clean(request.getParameter("bat"));
		String payMode = clean(request.getParameter("pay_mode"));
		// The booking belongs to the logged-in user; any "email" sent by the browser is ignored.
		javax.servlet.http.HttpSession userSession = request.getSession(false);
		String email = (userSession != null && userSession.getAttribute("email") != null)
				? clean(String.valueOf(userSession.getAttribute("email"))) : "";

		int slotId;
		int duration;
		try {
			slotId = Integer.parseInt(clean(request.getParameter("slot_id")));
		} catch (NumberFormatException e) {
			slotId = 0;
		}
		try {
			duration = Integer.parseInt(clean(request.getParameter("duration")));
		} catch (NumberFormatException e) {
			duration = 60;
		}
		if (duration <= 0) {
			duration = 60;
		}

		if (ownername.isEmpty() || number.isEmpty() || bat.isEmpty() || payMode.isEmpty()
				|| email.isEmpty() || slotId <= 0) {
			respond(out, "Booking details are incomplete. Please try again.", "history.back();");
			return;
		}

		// Book now = starts immediately, Reserve = starts at the chosen date and time
		Calendar start = Calendar.getInstance();
		String bookingType = clean(request.getParameter("booking_type"));
		String startParam = clean(request.getParameter("start_time"));
		if ("reserve".equals(bookingType) && !startParam.isEmpty()) {
			try {
				// datetime-local sends e.g. 2026-10-14T18:30
				start.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(startParam));
			} catch (java.text.ParseException e) {
				respond(out, "Invalid start date and time.", "history.back();");
				return;
			}
		}
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.MINUTE, duration);

		// The price is always calculated here from the slot stored in the database.
		// Any "amt" sent by the browser is ignored.
		BookingDao bookingDao = new BookingDao();
		Slot slot = bookingDao.getSlotById(slotId);
		if (slot == null) {
			respond(out, "The selected slot was not found.", "history.back();");
			return;
		}

		PriceQuote quote;
		try {
			quote = pricing.quote(slot.getVehicleType(), slot.getChargerType(), duration, 1);
		} catch (IllegalArgumentException e) {
			respond(out, "Could not calculate the price for this booking.", "history.back();");
			return;
		}

		Pay pay = new Pay();
		pay.setOwnername(ownername);
		pay.setNo(number);
		pay.setBat(bat);
		pay.setAmt(quote.getTotal().toPlainString());
		pay.setPayMode(payMode);
		pay.setEmail(email);
		pay.setSlotId(slotId);
		pay.setStartTime(new Timestamp(start.getTimeInMillis()));
		pay.setEndTime(new Timestamp(end.getTimeInMillis()));

		// Overlap check and insert happen in one transaction with the slot row locked.
		BookingResult result = bookingDao.bookSlot(pay);

		switch (result) {
		case BOOKED:
			respond(out, "Slot booked successfully", "location='MyBookings.jsp';");
			break;
		case TIME_CONFLICT:
			respond(out, "Sorry, this slot was just booked for an overlapping time. Please choose another time or slot.",
					"location='Search.jsp';");
			break;
		case SLOT_NOT_AVAILABLE:
			respond(out, "This slot is not available for booking right now. Please choose another slot.",
					"location='Search.jsp';");
			break;
		case SLOT_NOT_FOUND:
			respond(out, "The selected slot was not found.", "location='Search.jsp';");
			break;
		default:
			respond(out, "Booking failed. Please try again.", "history.back();");
			break;
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private static String clean(String v) {
		return v == null ? "" : v.trim();
	}

	private static void respond(PrintWriter out, String message, String action) {
		out.println("<script type=\"text/javascript\">");
		out.println("alert('" + message + "');");
		out.println(action);
		out.println("</script>");
	}
}