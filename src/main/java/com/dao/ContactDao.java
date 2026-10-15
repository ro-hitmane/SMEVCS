package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bean.ContactQuery;
import com.connection.DBConnection;

public class ContactDao {

	public boolean insertQuery(ContactQuery q) {
		String sql = "INSERT INTO tbl_contact (name, email, subject, message) VALUES (?,?,?,?)";
		try (Connection con = DBConnection.getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, q.getName());
			ps.setString(2, q.getEmail());
			ps.setString(3, q.getSubject());
			ps.setString(4, q.getMessage());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Fetch ONLY active queries
	public List<ContactQuery> getAllQueries() {
		List<ContactQuery> list = new ArrayList<>();
		String sql = "SELECT id, name, email, subject, message, created_at FROM tbl_contact WHERE status = 'ACTIVE' ORDER BY created_at ASC";
		
		try (Connection con = DBConnection.getConnection();
			 PreparedStatement ps = con.prepareStatement(sql); 
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				ContactQuery q = new ContactQuery();
				q.setId(rs.getInt("id"));
				q.setName(rs.getString("name"));
				q.setEmail(rs.getString("email"));
				q.setSubject(rs.getString("subject"));
				q.setMessage(rs.getString("message"));
				q.setCreatedAt(rs.getTimestamp("created_at"));
				list.add(q);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	// Mark query as resolved
	public boolean resolveQuery(int id) {
		String sql = "UPDATE tbl_contact SET status = 'RESOLVED' WHERE id = ?";
		try (Connection con = DBConnection.getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}