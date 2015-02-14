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
package br.tennaito.rsql.builder;

import br.tennaito.rsql.jpa.PredicateBuilderStrategy;
import br.tennaito.rsql.misc.ArgumentParser;
import br.tennaito.rsql.misc.Mapper;

/**
 * Facade Interface for miscelaneous utilities for operation definition.
 *
 * PropertyMapper for translation of properties aliases.
 * ArgumentParser for casting specific types.
 * PredicateBuilder for RSQL AST new nodes.
 *
 * @author AntonioRabelo
 * @since 2015-02-03
 */
public interface BuilderTools {

	/**
	 * Get a mapper for the properties.
	 *
	 * @return Mapper
	 */
	public Mapper getPropertiesMapper();

	/**
	 * Set a specific Mapper.
	 *
	 * @param mapper Mapper
	 */
	public void setPropertiesMapper(Mapper mapper);

	/**
	 * Get a argument parser for casting types.
	 *
	 * @return ArgumentParser
	 */
	public ArgumentParser getArgumentParser();

	/**
	 * Set a specific ArgumentParser.
	 *
	 * @param argumentParser ArgumentParser
	 */
	public void setArgumentParser(ArgumentParser argumentParser);

	/**
	 * Get a predicate strategy for parsing Node into Predicate.
	 *
	 * @return PredicateBuilderStrategy
	 */
	public PredicateBuilderStrategy getPredicateBuilder();

	/**
	 * Set a specific predicate strategy.
	 *
	 * @param predicateStrategy PredicateBuilderStrategy
	 */
	public void setPredicateBuilder(PredicateBuilderStrategy predicateStrategy);
}
