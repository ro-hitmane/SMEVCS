package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import com.bean.Station;
import com.bean.Slot;
import com.connection.DBConnection;

public class AdminDao {

	String sql;
	PreparedStatement ps;
	ResultSet rs;

	Connection con;
	boolean flag = false;
	public List<Station> getStations() {
	    List<Station> list = new ArrayList<>();
	    String sql = "select id, station_name, area from tbl_station order by station_name";
	    Connection con = DBConnection.getConnection();
	    if (con == null) return list;
	    try (PreparedStatement ps = con.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            Station s = new Station();
	            s.setId(rs.getInt("id"));
	            s.setStationName(rs.getString("station_name"));
	            s.setArea(rs.getString("area"));
	            list.add(s);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        try { con.close(); } catch (SQLException ignored) {}
	    }
	    return list;
	}

	public boolean Manageslots(Slot slot) {
	    String sql = "insert into tbl_slot(station_id, slot_label, vehicle_type, charger_type, status) values(?,?,?,?,?)";
	    Connection con = DBConnection.getConnection();
	    if (con == null) {
	        System.out.println("DBConnection returned null");
	        return false;
	    }
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, slot.getStationId());
	        ps.setString(2, slot.getSlotLabel());
	        ps.setString(3, slot.getVehicleType());
	        ps.setString(4, slot.getChargerType());
	        ps.setString(5, slot.getStatus());
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        try { con.close(); } catch (SQLException ignored) {}
	    }
	}
	}

