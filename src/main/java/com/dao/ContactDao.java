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

		String sql = "insert into tbl_contact (name, email, subject, message) values (?,?,?,?)";

		Connection con = DBConnection.getConnection();
		if (con == null) {
			return false;
		}

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, q.getName());
			ps.setString(2, q.getEmail());
			ps.setString(3, q.getSubject());
			ps.setString(4, q.getMessage());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			try { con.close(); } catch (SQLException ignore) { }
		}
	}

	public List<ContactQuery> getAllQueries() {

		List<ContactQuery> list = new ArrayList<ContactQuery>();
		String sql = "select id, name, email, subject, message, created_at from tbl_contact order by created_at desc";

		Connection con = DBConnection.getConnection();
		if (con == null) {
			return list;
		}

		try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
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
		} finally {
			try { con.close(); } catch (SQLException ignore) { }
		}
		return list;
	}
}