package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bean.Pay;
import com.bean.User;
import com.connection.DBConnection;

public class UserDao {

	public boolean InsertUserData(User userbean) {

		String sql = "insert into tbl_user(name, address, mobile_no, email, password, vehicle_no, vehicle_name, vehicle_owner_name) "
				+ "values(?,?,?,?,?,?,?,?)";
		boolean flag = false;

		try (Connection con = DBConnection.getConnection()) {
			if (con == null) {
				return false;
			}
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, userbean.getName());
				ps.setString(2, userbean.getAddress());
				ps.setString(3, userbean.getMobile_no());
				ps.setString(4, userbean.getEmail());
				ps.setString(5, userbean.getPassword());
				ps.setString(6, userbean.getVehicle_no());
				ps.setString(7, userbean.getVehicle_name());
				ps.setString(8, userbean.getVehicle_owner_name());

				flag = ps.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;
	}

//	public boolean InsertPayData(Pay pay) {
//
//		String sql = "insert into tbl_payment(ownername, number, bat, amt, pay_mode, email, slot_id, start_time, end_time) "
//				+ "values(?,?,?,?,?,?,?,?,?)";
//		boolean flag = false;
//
//		try (Connection con = DBConnection.getConnection()) {
//			if (con == null) {
//				return false;
//			}
//			try (PreparedStatement ps = con.prepareStatement(sql)) {
//				ps.setString(1, pay.getOwnername());
//				ps.setString(2, pay.getNo());
//				ps.setString(3, pay.getBat());
//				ps.setString(4, pay.getAmt());
//				ps.setString(5, pay.getPayMode());
//				ps.setString(6, pay.getEmail());
//				ps.setInt(7, pay.getSlotId());
//				ps.setTimestamp(8, pay.getStartTime());
//				ps.setTimestamp(9, pay.getEndTime());
//
//				flag = ps.executeUpdate() > 0;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return flag;
//	}

	public boolean alreadyUser(String email) throws SQLException {

		String sql = "select * from tbl_user where email=?";

		try (Connection con = DBConnection.getConnection()) {
			if (con == null) {
				throw new SQLException("Database connection is not available");
			}
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, email);
				try (ResultSet rs = ps.executeQuery()) {
					return rs.next();
				}
			}
		}
	}

	/**
	 * Returns a User filled from the database when the email and password match.
	 * When there is no match (or the database is down) the returned User has null fields.
	 */
	public User CheckUser(String email, String password) {

		User bean = new User();
		String sql = "select * from tbl_user where email=? and password=?";

		try (Connection con = DBConnection.getConnection()) {
			if (con == null) {
				return bean;
			}
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setString(1, email);
				ps.setString(2, password);

				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						bean.setName(rs.getString(2));
						bean.setAddress(rs.getString(3));
						bean.setMobile_no(rs.getString(4));
						bean.setEmail(rs.getString(5));
						bean.setPassword(rs.getString(6));
						bean.setVehicle_no(rs.getString(7));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return bean;
	}

}