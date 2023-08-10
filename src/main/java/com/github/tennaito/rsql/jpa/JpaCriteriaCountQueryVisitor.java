/*
 * The MIT License
 *
 * Copyright 2015 Antonio Rabelo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.tennaito.rsql.jpa;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

/**
 * JpaCriteriaQueryVisitor
 *
 * Visitor class for Criteria Query count creation from RSQL AST Nodes.
 *
 * @author sza
 *
 * @param <T> Entity type
 */
public class JpaCriteriaCountQueryVisitor<T> extends AbstractJpaVisitor<CriteriaQuery<Long>, T>  implements RSQLVisitor<CriteriaQuery<Long>, EntityManager> {

    private static final Logger LOG = Logger.getLogger(JpaCriteriaCountQueryVisitor.class.getName());

    private final JpaPredicateVisitor<T> predicateVisitor;

    private Root<T> root;

    /**
     * Construtor with template varargs for entityClass discovery.
     *
     * @param t not for usage
     */
    @SafeVarargs
    public JpaCriteriaCountQueryVisitor(T... t) {
        super(t);
        this.predicateVisitor = new JpaPredicateVisitor<T>(t);
    }

    /**
     * Get the Predicate Visitor instance.
     *
     * @return Return the Predicate Visitor.
     */
    protected JpaPredicateVisitor<T> getPredicateVisitor() {
        this.predicateVisitor.setBuilderTools(this.getBuilderTools());
        return this.predicateVisitor;
    }

    /* (non-Javadoc)
     * @see cz.jirutka.rsql.parser.ast.RSQLVisitor#visit(cz.jirutka.rsql.parser.ast.AndNode, java.lang.Object)
     */
    public CriteriaQuery<Long> visit(AndNode node, EntityManager entityManager) {
        LOG.log(Level.INFO, "Creating CriteriaQuery for AndNode: {0}", node);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        root = cq.from(entityClass);
        cq.select(cb.countDistinct(root));
        cq.where(this.getPredicateVisitor().defineRoot(root).visit(node, entityManager));

        return cq;
    }

    /* (non-Javadoc)
     * @see cz.jirutka.rsql.parser.ast.RSQLVisitor#visit(cz.jirutka.rsql.parser.ast.OrNode, java.lang.Object)
     */
    public CriteriaQuery<Long> visit(OrNode node, EntityManager entityManager) {
        LOG.log(Level.INFO, "Creating CriteriaQuery for OrNode: {0}", node);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        root = cq.from(entityClass);
        cq.select(cb.countDistinct(root));
        root = cq.from(entityClass);
        cq.where(this.getPredicateVisitor().defineRoot(root).visit(node, entityManager));
        return cq;
    }

    /* (non-Javadoc)
     * @see cz.jirutka.rsql.parser.ast.RSQLVisitor#visit(cz.jirutka.rsql.parser.ast.ComparisonNode, java.lang.Object)
     */
    public CriteriaQuery<Long> visit(ComparisonNode node, EntityManager entityManager) {
        LOG.log(Level.INFO, "Creating CriteriaQuery for ComparisonNode: {0}", node);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        root = cq.from(entityClass);
        cq.select(cb.countDistinct(root));
        cq.where(this.getPredicateVisitor().defineRoot(root).visit(node, entityManager));
        return cq;
    }

    public Root<T> getRoot() {
        return root;
    }

    public void setRoot(Root<T> root) {
        this.root = root;
    }


}