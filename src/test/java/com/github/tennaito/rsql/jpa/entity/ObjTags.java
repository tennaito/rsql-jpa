/*
 * Copyright (c) Axway Software, 2017. All Rights Reserved.
 *
 */

package com.github.tennaito.rsql.jpa.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;


@Entity
public class ObjTags extends AbstractTestEntity{

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Tag> tags;

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }
}
