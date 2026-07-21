package com.ibm.di.cvt71.tp.template;

import com.ibm.di.entry.Entry;
import static org.junit.Assert.*;

public class Person {

	private String firstname;
	private String lastname;
	private int age;
	private int height;

	public Person(String firstname, String lastname, int age, int height) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.age = age;
		this.height = height;
	}

	public Person(Entry e) {

		assertNotNull(e.getString("firstname"));
		assertNotNull(e.getString("lastname"));
		assertNotNull(e.getString("age"));
		assertNotNull(e.getString("height"));
		assertEquals(4, e.size());

		this.firstname = e.getString("firstname");
		this.lastname = e.getString("lastname");
		this.age = Integer.parseInt(e.getString("age"));
		this.height = Integer.parseInt(e.getString("height"));
	}
	
	public Entry toEntry() {
		Entry e = new Entry();
		e.setAttribute("firstname", this.firstname);
		e.setAttribute("lastname", this.lastname);
		e.setAttribute("age", ""+this.age);
		e.setAttribute("height", ""+this.height);
		return e;
	}

	public String getFirstName() {
		return firstname;
	}

	public String getLastName() {
		return lastname;
	}

	public int getAge() {
		return age;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result;
		if (obj instanceof Person) {
			Person other = (Person) obj;
			result = this.firstname.equals(other.firstname) && this.lastname.equals(other.lastname) && this.age == other.age
					&& this.height == other.height;
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode() {
		return this.firstname.hashCode() + this.lastname.hashCode();
	}

	@Override
	public String toString() {
		return "person { firstname:" + firstname + ", lastname:" + lastname + ", age:" + age + ", height:" + height + "}";
	}

}
