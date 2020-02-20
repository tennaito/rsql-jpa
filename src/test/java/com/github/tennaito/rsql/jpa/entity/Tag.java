/*
 * Copyright (c) Axway Software, 2017. All Rights Reserved.
 *
 */

package com.github.tennaito.rsql.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by ivelin on 2/24/17.
 */
@Entity
public class Tag extends AbstractTestEntity {

    @Column(name = "TAG",
            nullable = false)
    private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
