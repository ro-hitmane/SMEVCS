package com.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bean.Pay;
import com.dao.UserDao;

@WebServlet("/PayController")
public class PayController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public PayController() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String ownername = clean(request.getParameter("ownername"));
		String number = clean(request.getParameter("number"));
		String bat = clean(request.getParameter("bat"));
		String amt = clean(request.getParameter("amt"));
		String payMode = clean(request.getParameter("pay_mode"));
		String email = clean(request.getParameter("email"));

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

		if (ownername.isEmpty() || number.isEmpty() || bat.isEmpty() || amt.isEmpty() || payMode.isEmpty()
				|| email.isEmpty() || slotId <= 0) {
			request.setAttribute("error", "Booking details are incomplete. Please complete all fields.");
			request.getRequestDispatcher("SearchSlot.jsp").forward(request, response);
			return;
		}

		Calendar start = Calendar.getInstance();
		String bookingType = clean(request.getParameter("booking_type"));
		String startParam = clean(request.getParameter("start_time"));
		if ("reserve".equals(bookingType) && !startParam.isEmpty()) {
			try {
				start.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(startParam));
			} catch (java.text.ParseException e) {
				request.setAttribute("error", "Invalid start date and time format.");
				request.getRequestDispatcher("SearchSlot.jsp").forward(request, response);
				return;
			}
		}
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.MINUTE, duration);

		Pay pay = new Pay();
		pay.setOwnername(ownername);
		pay.setNo(number);
		pay.setBat(bat);
		pay.setAmt(amt);
		pay.setPayMode(payMode);
		pay.setEmail(email);
		pay.setSlotId(slotId);
		pay.setStartTime(new Timestamp(start.getTimeInMillis()));
		pay.setEndTime(new Timestamp(end.getTimeInMillis()));

		if (new UserDao().InsertPayData(pay)) {
			response.sendRedirect("MyBookings.jsp?msg=" + URLEncoder.encode("Slot booked successfully!", "UTF-8"));
		} else {
			request.setAttribute("error", "Booking could not be finalized. Please try again.");
			request.getRequestDispatcher("SearchSlot.jsp").forward(request, response);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private static String clean(String v) {
		return v == null ? "" : v.trim();
	}
}