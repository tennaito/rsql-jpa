package com.github.tennaito.rsql.jpa.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class Room extends AbstractTestEntity {

    @ManyToOne
    private Building building;

    @OneToMany(mappedBy = "homeroom")
    private Set<Person> students;
}
