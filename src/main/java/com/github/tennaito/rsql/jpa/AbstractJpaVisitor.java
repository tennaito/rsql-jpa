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

import jakarta.persistence.EntityManager;

import com.github.tennaito.rsql.builder.BuilderTools;
import com.github.tennaito.rsql.builder.SimpleBuilderTools;

import cz.jirutka.rsql.parser.ast.RSQLVisitor;

/**
 * AbstractQueryVisitor
 *
 * Abstract Visitor class for parsing RSQL AST Nodes.
 *
 * @author AntonioRabelo
 *
 * @param <T> Result type
 * @param <E> Entity type
 */
public abstract class AbstractJpaVisitor<T, E> implements RSQLVisitor<T, EntityManager> {

	protected Class<E> entityClass;

	protected BuilderTools builderTools;

	/**
	 * Construtor with template varargs for entityClass discovery.
	 *
	 * @param e not for usage
	 */
	public AbstractJpaVisitor(E... e) {
		// getting class from template... :P
		if (e.length == 0) {
			entityClass = (Class<E>)e.getClass().getComponentType();
		} else {
			entityClass = (Class<E>) e[0].getClass();
		}		
	}

	/**
	 * Set the entity class explicitly, needed when the entity type is itself a generic
	 *
	 * @param clazz Class to set.
     */
	public void setEntityClass(Class<E> clazz) {
		entityClass = clazz;
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
}
