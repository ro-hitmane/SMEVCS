package com.dao;

/** Outcome of an attempt to book a slot. */
public enum BookingResult {

	/** The booking was saved. */
	BOOKED,

	/** No slot with that id exists. */
	SLOT_NOT_FOUND,

	/** The slot exists but its status is not Available (reserved, offline or in maintenance). */
	SLOT_NOT_AVAILABLE,

	/** Another booking already covers part of the requested time. */
	TIME_CONFLICT,

	/** Database problem (connection lost, deadlock, lock wait timeout). Nothing was saved. */
	ERROR
}