package com.siberhus.sample.domain;

import java.math.BigDecimal;
import java.util.Date;

public class Lead {

	private String firstName;
	private String lastName;
	
	private Integer salary;
	private BigDecimal annulaRevenue;
	
	private Date birthdate;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Integer getSalary() {
		return salary;
	}

	public void setSalary(Integer salary) {
		this.salary = salary;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public BigDecimal getAnnulaRevenue() {
		return annulaRevenue;
	}

	public void setAnnulaRevenue(BigDecimal annulaRevenue) {
		this.annulaRevenue = annulaRevenue;
	}
}
