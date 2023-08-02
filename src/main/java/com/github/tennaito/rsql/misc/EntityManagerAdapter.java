package com.github.tennaito.rsql.misc;

import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

public final class EntityManagerAdapter {

    Supplier<Metamodel> metamodelSupplier;

    Supplier<CriteriaBuilder> criteriaBuilderSupplier;

    public EntityManagerAdapter(EntityManager entityManager) {
        this(entityManager::getMetamodel, entityManager::getCriteriaBuilder);
    }

    public EntityManagerAdapter(Supplier<Metamodel> metamodelSupplier, Supplier<CriteriaBuilder> criteriaBuilderSupplier) {
        this.metamodelSupplier = metamodelSupplier;
        this.criteriaBuilderSupplier = criteriaBuilderSupplier;
    }


    public Metamodel getMetamodel() {
        return metamodelSupplier.get();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return criteriaBuilderSupplier.get();
    }
}