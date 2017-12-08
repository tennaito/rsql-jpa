package com.github.tennaito.rsql.jpa.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class PersonDependentPK implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @ManyToOne
    private Person person;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

}
