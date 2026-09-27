package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bean.Slot;
import com.connection.DBConnection;

public class AdminDao {
	
	String sql;
	PreparedStatement ps;
	ResultSet rs;

	Connection con;
	boolean flag = false;
	public boolean Manageslots(Slot slot) {
		sql = "insert into tbl_slot(slotname,address,area,city,state,country) values(?,?,?,?,?,?)";

		con = DBConnection.getConnection();

		try {
			ps = con.prepareStatement(sql);

			ps.setString(1, slot.getSlotname());
			ps.setString(2, slot.getSlotaddress());
			ps.setString(3, slot.getSlotarea());
			ps.setString(4, slot.getCity());
			ps.setString(5, slot.getState());
			ps.setString(6, slot.getCountry());
			
			
			int index = ps.executeUpdate();

			if (index > 0) {
				flag = true;
			} else {
				flag = false;
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return flag;
	}

}
