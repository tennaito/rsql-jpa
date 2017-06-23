package com.github.tennaito.rsql.jpa.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class PersonDependent {

    @EmbeddedId
    private PersonDependentPK id = new PersonDependentPK();

    private Integer age;

    public PersonDependentPK getId() {
        return id;
    }

    public void setId(PersonDependentPK id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

}
