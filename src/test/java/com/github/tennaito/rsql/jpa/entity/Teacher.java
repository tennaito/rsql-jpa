package com.github.tennaito.rsql.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Teacher extends AbstractTestEntity {

    @Column
    private String specialtyDescription;

    public String getSpecialtyDescription() {
        return specialtyDescription;
    }

    public void setSpecialtyDescription(String specialtyDescription) {
        this.specialtyDescription = specialtyDescription;
    }


}