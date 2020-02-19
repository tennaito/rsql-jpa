package com.github.tennaito.rsql.jpa;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * Created by ivelin on 8/15/17.
 */
public class TestEntityManagerBuilder {

    public EntityManager buildEntityManager(String persistenceUnit) {
        final EntityManager entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
        return entityManager;
    }


}
