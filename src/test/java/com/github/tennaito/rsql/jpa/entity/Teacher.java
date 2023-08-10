package com.github.tennaito.rsql.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

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

    /**
     * Added this method to assist with testing.
     *
     * When used with the DefaultArgumentParser, an object requires a valueOf method in order
     * to be converted from a string.
     *
     * @param s name of the teacher
     * @return Teacher with the name field set but nothing else
     */
    public static Teacher valueOf(String s) {
        Teacher teacher = new Teacher();
        teacher.setName(s);
        return teacher;
    }
}