package com.github.tennaito.rsql.jpa.entity;

import javax.persistence.Embeddable;

@Embeddable
public class CourseDetails {

	private String description;
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public static CourseDetails of(String description) {
		CourseDetails details = new CourseDetails();
		details.setDescription(description);
		return details;
	}
}
