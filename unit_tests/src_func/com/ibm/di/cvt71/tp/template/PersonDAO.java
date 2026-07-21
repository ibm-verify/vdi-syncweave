package com.ibm.di.cvt71.tp.template;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class PersonDAO {

	private Connection conn;
	private String tableName;

	public PersonDAO(Connection conn, String tableName) throws Exception {
		this.conn = conn;
		this.tableName = tableName;
		createTable();
	}

	public PersonDAO add(Person... persons) throws Exception {
		for (Person p : persons) {
			add(p);
		}
		return this;
	}

	public PersonDAO add(Person person) throws Exception {
		final String insertString = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(insertString);
		try {
			stmt.setString(1, person.getFirstName());
			stmt.setString(2, person.getLastName());
			stmt.setInt(3, person.getAge());
			stmt.setInt(4, person.getHeight());
			stmt.executeUpdate();
			conn.commit();
		} finally {
			stmt.close();
		}

		return this;
	}

	public Set<Person> list() throws Exception {
		Set<Person> result;
		final String select = "SELECT * FROM " + tableName;
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery(select);
			try {
				result = createPersonSet(rs);
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
		return result;
	}

	private Set<Person> createPersonSet(ResultSet rs) throws Exception {
		Set<Person> result = new HashSet<Person>();
		while (rs.next()) {
			Person person = createPerson(rs);
			result.add(person);
		}
		return result;
	}

	private Person createPerson(ResultSet rs) throws Exception {
		String firstname = rs.getString("firstname");
		String lastname = rs.getString("lastname");
		int age = rs.getInt("age");
		int height = rs.getInt("height");
		return new Person(firstname, lastname, age, height);
	}

	public void destroy() throws Exception {
		dropTable();
	}

	private boolean tableExists() throws Exception {
		boolean exists = true;
		final String select = "SELECT * FROM " + tableName;
		Statement stmt = conn.createStatement();
		try {
			stmt.executeQuery(select);
		} catch (Exception ex) {
			exists = false;
		} finally {
			stmt.close();
		}
		return exists;
	}

	private void executeSQL(String sql) throws Exception {
		Statement stmt = conn.createStatement();
		try {
			stmt.executeUpdate(sql);
			conn.commit();
		} finally {
			stmt.close();
		}
	}

	private void createTable() throws Exception {
		if (tableExists()) {
			return;
		}
		final String createString = "CREATE TABLE " + tableName
				+ " (firstname VARCHAR(50), lastname VARCHAR(50), age INT, height INT)";
		executeSQL(createString);
	}

	private void dropTable() throws Exception {
		if (tableExists()) {
			final String dropString = "DROP TABLE " + tableName;
			executeSQL(dropString);
		}
	}
}
