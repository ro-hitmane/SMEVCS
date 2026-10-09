package com.bean;

import java.sql.Timestamp;

public class ContactQuery {

	private int id;
	private String name, email, subject, message;
	private Timestamp createdAt;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getSubject() { return subject; }
	public void setSubject(String subject) { this.subject = subject; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public Timestamp getCreatedAt() { return createdAt; }
	public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}