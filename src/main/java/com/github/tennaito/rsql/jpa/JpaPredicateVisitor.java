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
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

/**
 * JpaPredicateVisitor
 *
 * Visitor class for Predicate creation from RSQL AST Nodes.
 *
 * @author AntonioRabelo
 *
 * @param <T> Entity type
 */
public class JpaPredicateVisitor<T> extends AbstractJpaVisitor<Predicate, T>  implements RSQLVisitor<Predicate, EntityManager> {

	/**
	 * Logger.
	 */
	private static final Logger LOG = Logger.getLogger(JpaPredicateVisitor.class.getName());
	
	/**
	 * Root.
	 */
	private From root;

	/**
	 * Construtor with template varargs for entityClass discovery.
	 *
	 * @param t not for usage
	 */
	public JpaPredicateVisitor(T... t) {
		super(t);
	}
	
	/**
	 * Define the From node.
	 * @param root From node that expressions path depends on.
	 * @return Fluent interface.
	 */
	public JpaPredicateVisitor<T> defineRoot(From root) {
		this.root = root;
		return this;
	}

	/* (non-Javadoc)
	 * @see cz.jirutka.rsql.parser.ast.RSQLVisitor#visit(cz.jirutka.rsql.parser.ast.AndNode, java.lang.Object)
	 */
	public Predicate visit(AndNode node, EntityManager entityManager) {
		LOG.log(Level.INFO, "Creating Predicate for AndNode: {0}", node);
		return PredicateBuilder.<T>createPredicate(node, root, entityClass, entityManager, getBuilderTools());
	}

	/* (non-Javadoc)
	 * @see cz.jirutka.rsql.parser.ast.RSQLVisitor#visit(cz.jirutka.rsql.parser.ast.OrNode, java.lang.Object)
	 */
	public Predicate visit(OrNode node, EntityManager entityManager) {
		LOG.log(Level.INFO, "Creating Predicate for OrNode: {0}", node);
		return PredicateBuilder.<T>createPredicate(node, root, entityClass, entityManager, getBuilderTools());
	}

	/* (non-Javadoc)
	 * @see cz.jirutka.rsql.parser.ast.RSQLVisitor#visit(cz.jirutka.rsql.parser.ast.ComparisonNode, java.lang.Object)
	 */
	public Predicate visit(ComparisonNode node, EntityManager entityManager) {
		LOG.log(Level.INFO, "Creating Predicate for ComparisonNode: {0}", node);
    	return PredicateBuilder.<T>createPredicate(node, root, entityClass, entityManager, getBuilderTools());
	}
}
