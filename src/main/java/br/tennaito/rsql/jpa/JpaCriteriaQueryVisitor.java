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
package br.tennaito.rsql.jpa;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import br.tennaito.rsql.builder.BuilderTools;
import br.tennaito.rsql.builder.SimpleBuilderTools;
import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

/**
 * JpaCriteriaQueryVisitor
 *
 * Visitor class for Criteria Query creation from RSQL AST Nodes.
 *
 * @author AntonioRabelo
 *
 * @param <T> Entity type
 */
public class JpaCriteriaQueryVisitor<T> implements RSQLVisitor<CriteriaQuery<T>, EntityManager> {

	private static final Logger LOG = Logger.getLogger(JpaCriteriaQueryVisitor.class.getName());

	protected Class<T> entityClass;

	protected BuilderTools builderTools;

	/**
	 * Construtor with template varargs for entityClass discovery.
	 *
	 * @param t not for usage
	 */
	public JpaCriteriaQueryVisitor(T... t) {
		// getting class from template... :P
		entityClass = (Class<T>)t.getClass().getComponentType();
	}

	/**
	 * Get builder tools.
	 *
	 * @return BuilderTools.
	 */
	public BuilderTools getBuilderTools() {
		if (this.builderTools == null) {
			this.builderTools = new SimpleBuilderTools();
		}
		return this.builderTools;
	}

	/**
	 * Set a predicate strategy.
	 *
	 * @param delegate PredicateBuilderStrategy.
	 */
	public void setBuilderTools(BuilderTools delegate) {
		this.builderTools = delegate;
	}

	/* (non-Javadoc)
	 * @see cz.jirutka.rsql.parser.ast.RSQLVisitor#visit(cz.jirutka.rsql.parser.ast.AndNode, java.lang.Object)
	 */
	public CriteriaQuery<T> visit(AndNode node, EntityManager entityManager) {
		LOG.log(Level.INFO, "Creating Predicate for AndNode: {0}", node);
    	javax.persistence.criteria.CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    	CriteriaQuery<T> criteria = builder.createQuery(entityClass);
		return criteria.where(PredicateBuilder.<T>createPredicate(node, entityClass, entityManager, getBuilderTools()));
	}

	/* (non-Javadoc)
	 * @see cz.jirutka.rsql.parser.ast.RSQLVisitor#visit(cz.jirutka.rsql.parser.ast.OrNode, java.lang.Object)
	 */
	public CriteriaQuery<T> visit(OrNode node, EntityManager entityManager) {
		LOG.log(Level.INFO, "Creating Predicate for OrNode: {0}", node);
    	javax.persistence.criteria.CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    	CriteriaQuery<T> criteria = builder.createQuery(entityClass);
		return criteria.where(PredicateBuilder.<T>createPredicate(node, entityClass, entityManager, getBuilderTools()));
	}

	/* (non-Javadoc)
	 * @see cz.jirutka.rsql.parser.ast.RSQLVisitor#visit(cz.jirutka.rsql.parser.ast.ComparisonNode, java.lang.Object)
	 */
	public CriteriaQuery<T> visit(ComparisonNode node, EntityManager entityManager) {
		LOG.log(Level.INFO, "Creating Predicate for ComparisonNode: {0}", node);
    	javax.persistence.criteria.CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    	CriteriaQuery<T> criteria = builder.createQuery(entityClass);
    	return criteria.where(PredicateBuilder.<T>createPredicate(node, entityClass, entityManager, getBuilderTools()));
	}
}
