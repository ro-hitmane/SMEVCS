package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bean.Pay;
import com.bean.Slot;
import com.connection.DBConnection;

/**
 * Booking writes that must never create two overlapping bookings on one slot.
 */
public class BookingDao {

	/** Reads a slot (used to price a booking from the slot's own vehicle and charger type). */
	public Slot getSlotById(int slotId) {

		String sql = "select id, station_id, slot_label, vehicle_type, charger_type, status from tbl_slot where id = ?";

		try (Connection con = DBConnection.getConnection()) {
			if (con == null) {
				return null;
			}
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setInt(1, slotId);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						Slot slot = new Slot();
						slot.setId(rs.getInt("id"));
						slot.setStationId(rs.getInt("station_id"));
						slot.setSlotLabel(rs.getString("slot_label"));
						slot.setVehicleType(rs.getString("vehicle_type"));
						slot.setChargerType(rs.getString("charger_type"));
						slot.setStatus(rs.getString("status"));
						return slot;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public BookingResult bookSlot(Pay pay) {

		Connection con = DBConnection.getConnection();
		if (con == null) {
			System.out.println("DBConnection returned null");
			return BookingResult.ERROR;
		}

		int originalIsolation = Connection.TRANSACTION_REPEATABLE_READ;
		try {
			originalIsolation = con.getTransactionIsolation();
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			con.setAutoCommit(false);

			// 1. Lock the slot row. Any other booking for this slot waits here.
			String status;
			try (PreparedStatement ps = con.prepareStatement("select status from tbl_slot where id = ? for update")) {
				ps.setInt(1, pay.getSlotId());
				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next()) {
						con.rollback();
						return BookingResult.SLOT_NOT_FOUND;
					}
					status = rs.getString(1);
				}
			}

			// An empty status counts as Available, the same rule the search page uses.
			if (status != null && !status.trim().isEmpty() && !"Available".equalsIgnoreCase(status.trim())) {
				con.rollback();
				return BookingResult.SLOT_NOT_AVAILABLE;
			}

			// 2. Two time ranges overlap when each one starts before the other ends.
			//    A booking that ends exactly when another starts is allowed.
			String overlapSql = "select count(*) from tbl_payment where slot_id = ? and start_time < ? and end_time > ?";
			try (PreparedStatement ps = con.prepareStatement(overlapSql)) {
				ps.setInt(1, pay.getSlotId());
				ps.setTimestamp(2, pay.getEndTime());
				ps.setTimestamp(3, pay.getStartTime());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next() && rs.getInt(1) > 0) {
						con.rollback();
						return BookingResult.TIME_CONFLICT;
					}
				}
			}

			// 3. The slot is free for the whole range, so save the booking.
			String insertSql = "insert into tbl_payment(ownername, number, bat, amt, pay_mode, email, slot_id, start_time, end_time) "
					+ "values(?,?,?,?,?,?,?,?,?)";
			try (PreparedStatement ps = con.prepareStatement(insertSql)) {
				ps.setString(1, pay.getOwnername());
				ps.setString(2, pay.getNo());
				ps.setString(3, pay.getBat());
				ps.setString(4, pay.getAmt());
				ps.setString(5, pay.getPayMode());
				ps.setString(6, pay.getEmail());
				ps.setInt(7, pay.getSlotId());
				ps.setTimestamp(8, pay.getStartTime());
				ps.setTimestamp(9, pay.getEndTime());
				ps.executeUpdate();
			}

			// 4. Commit. This also releases the row lock so the next waiting booking can continue.
			con.commit();
			return BookingResult.BOOKED;

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException ignored) {
			}
			return BookingResult.ERROR;
		} finally {
			try {
				con.setAutoCommit(true);
				con.setTransactionIsolation(originalIsolation);
			} catch (SQLException ignored) {
			}
			try {
				con.close();
			} catch (SQLException ignored) {
			}
		}
	}
}