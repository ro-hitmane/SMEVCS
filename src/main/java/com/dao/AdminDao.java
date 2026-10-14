package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import com.bean.Station;
import com.bean.Slot;
import com.bean.Booking;
import com.connection.DBConnection;

public class AdminDao {

	String sql;
	PreparedStatement ps;
	ResultSet rs;

	Connection con;
	boolean flag = false;
	public List<Station> getStations() {
	    List<Station> list = new ArrayList<>();
	    String sql = "select id, station_name, address, area, city, state, country from tbl_station order by station_name";
	    Connection con = DBConnection.getConnection();
	    if (con == null) return list;
	    try (PreparedStatement ps = con.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            Station s = new Station();
	            s.setId(rs.getInt("id"));
	            s.setStationName(rs.getString("station_name"));
	            s.setAddress(rs.getString("address"));
	            s.setArea(rs.getString("area"));
	            s.setCity(rs.getString("city"));
	            s.setState(rs.getString("state"));
	            s.setCountry(rs.getString("country"));
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
	
	public boolean addStation(Station station) {
	    String sql = "insert into tbl_station(station_name, address, area, city, state, country) values(?,?,?,?,?,?)";
	    Connection con = DBConnection.getConnection();
	    if (con == null) {
	        System.out.println("DBConnection returned null");
	        return false;
	    }
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setString(1, station.getStationName());
	        ps.setString(2, station.getAddress());
	        ps.setString(3, station.getArea());
	        ps.setString(4, station.getCity());
	        ps.setString(5, station.getState());
	        ps.setString(6, station.getCountry());
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        try { con.close(); } catch (SQLException ignored) {}
	    }
	}

	public Station getStationById(int id) {
	    String sql = "select id, station_name, address, area, city, state, country from tbl_station where id = ?";
	    Connection con = DBConnection.getConnection();
	    if (con == null) {
	        System.out.println("DBConnection returned null");
	        return null;
	    }
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, id);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                Station s = new Station();
	                s.setId(rs.getInt("id"));
	                s.setStationName(rs.getString("station_name"));
	                s.setAddress(rs.getString("address"));
	                s.setArea(rs.getString("area"));
	                s.setCity(rs.getString("city"));
	                s.setState(rs.getString("state"));
	                s.setCountry(rs.getString("country"));
	                return s;
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        try { con.close(); } catch (SQLException ignored) {}
	    }
	    return null;
	}

	public List<Slot> getSlotsByStation(int stationId) {
	    List<Slot> list = new ArrayList<>();
	    String sql = "select id, station_id, slot_label, vehicle_type, charger_type, status "
	            + "from tbl_slot where station_id = ? order by id";
	    Connection con = DBConnection.getConnection();
	    if (con == null) {
	        System.out.println("DBConnection returned null");
	        return list;
	    }
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, stationId);
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                Slot sl = new Slot();
	                sl.setId(rs.getInt("id"));
	                sl.setStationId(rs.getInt("station_id"));
	                sl.setSlotLabel(rs.getString("slot_label"));
	                sl.setVehicleType(rs.getString("vehicle_type"));
	                sl.setChargerType(rs.getString("charger_type"));
	                sl.setStatus(rs.getString("status"));
	                list.add(sl);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        try { con.close(); } catch (SQLException ignored) {}
	    }
	    return list;
	}

	public boolean updateSlot(Slot slot) {
	    String sql = "update tbl_slot set slot_label = ?, vehicle_type = ?, charger_type = ?, status = ? where id = ? and station_id = ?";
	    Connection con = DBConnection.getConnection();
	    if (con == null) {
	        System.out.println("DBConnection returned null");
	        return false;
	    }
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setString(1, slot.getSlotLabel());
	        ps.setString(2, slot.getVehicleType());
	        ps.setString(3, slot.getChargerType());
	        ps.setString(4, slot.getStatus());
	        ps.setInt(5, slot.getId());
	        ps.setInt(6, slot.getStationId());
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        try { con.close(); } catch (SQLException ignored) {}
	    }
	}

	public boolean deleteSlot(int slotId, int stationId) {
	    String sql = "delete from tbl_slot where id = ? and station_id = ?";
	    Connection con = DBConnection.getConnection();
	    if (con == null) {
	        System.out.println("DBConnection returned null");
	        return false;
	    }
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, slotId);
	        ps.setInt(2, stationId);
	        return ps.executeUpdate() > 0;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    } finally {
	        try { con.close(); } catch (SQLException ignored) {}
	    }
	}

	/** Number of bookings ever made on a slot, or -1 if the database is unreachable. */
	public int countBookingsForSlot(int slotId) {
	    String sql = "select count(*) from tbl_payment where slot_id = ?";
	    Connection con = DBConnection.getConnection();
	    if (con == null) return -1;
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, slotId);
	        try (ResultSet rs = ps.executeQuery()) {
	            return rs.next() ? rs.getInt(1) : 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return -1;
	    } finally {
	        try { con.close(); } catch (SQLException ignored) {}
	    }
	}

	public List<Booking> getBookingsByStation(int stationId) {
	    List<Booking> list = new ArrayList<>();
	    String sql = "select p.id, p.ownername, p.number, p.bat, p.amt, p.pay_mode, p.email, p.start_time, p.end_time, "
	            + "sl.id as slot_id, sl.slot_label, sl.vehicle_type, sl.charger_type "
	            + "from tbl_payment p join tbl_slot sl on sl.id = p.slot_id "
	            + "where sl.station_id = ? order by p.id desc";
	    Connection con = DBConnection.getConnection();
	    if (con == null) {
	        System.out.println("DBConnection returned null");
	        return list;
	    }
	    try (PreparedStatement ps = con.prepareStatement(sql)) {
	        ps.setInt(1, stationId);
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                Booking b = new Booking();
	                b.setId(rs.getInt("id"));
	                b.setOwnerName(rs.getString("ownername"));
	                b.setVehicleNumber(rs.getString("number"));
	                b.setBatteryCapacity(rs.getString("bat"));
	                b.setAmount(rs.getString("amt"));
	                b.setPayMode(rs.getString("pay_mode"));
	                b.setEmail(rs.getString("email"));
	                b.setStartTime(rs.getTimestamp("start_time"));
	                b.setEndTime(rs.getTimestamp("end_time"));
	                b.setSlotId(rs.getInt("slot_id"));
	                b.setSlotLabel(rs.getString("slot_label"));
	                b.setVehicleType(rs.getString("vehicle_type"));
	                b.setChargerType(rs.getString("charger_type"));
	                list.add(b);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        try { con.close(); } catch (SQLException ignored) {}
	    }
	    return list;
	}
	}