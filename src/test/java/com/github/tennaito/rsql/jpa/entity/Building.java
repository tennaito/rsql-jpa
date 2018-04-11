package com.github.tennaito.rsql.jpa.entity;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class Building extends AbstractTestEntity {

    @OneToMany(mappedBy = "building")
    private Set<Room> rooms;
}
