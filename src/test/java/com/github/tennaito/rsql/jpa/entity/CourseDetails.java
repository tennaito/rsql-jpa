package com.github.tennaito.rsql.jpa.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToOne;

@Embeddable
public class CourseDetails {

	private String description;

	@OneToOne
	private Teacher teacher;

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

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}
}
