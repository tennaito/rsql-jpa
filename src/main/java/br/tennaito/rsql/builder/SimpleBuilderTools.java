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
import br.tennaito.rsql.misc.DefaultArgumentParser;
import br.tennaito.rsql.misc.Mapper;
import br.tennaito.rsql.misc.SimpleMapper;

/**
 * Simple Builder Tools
 *
 * A facade for all tools that are used inside the build.
 *
 * @author AntonioRabelo
 * @since 2015-02-13
 */
public class SimpleBuilderTools implements BuilderTools {

	private Mapper mapper;
	private ArgumentParser argumentParser;
	private PredicateBuilderStrategy delegate;


	/* (non-Javadoc)
	 * @see br.tennaito.rsql.builder.BuilderTools#setPropertiesMapper(br.tennaito.rsql.misc.Mapper)
	 */
	@Override
	public void setPropertiesMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	/* (non-Javadoc)
	 * @see br.tennaito.rsql.builder.BuilderTools#setPropertiesMapper(br.tennaito.rsql.misc.ArgumentParser)
	 */
	@Override
	public void setPropertiesMapper(ArgumentParser argumentParser) {
		this.argumentParser = argumentParser;
	}

	/* (non-Javadoc)
	 * @see br.tennaito.rsql.builder.BuilderTools#setPredicateBuilder(br.tennaito.rsql.jpa.PredicateBuilderStrategy)
	 */
	@Override
	public void setPredicateBuilder(PredicateBuilderStrategy predicateStrategy) {
		this.delegate = predicateStrategy;
	}

	/* (non-Javadoc)
	 * @see br.tennaito.rsql.builder.BuilderTools#getPropertiesMapper()
	 */
	@Override
	public Mapper getPropertiesMapper() {
		if (this.mapper == null) {
			this.mapper = new SimpleMapper();
		}
		return this.mapper;
	}

	/* (non-Javadoc)
	 * @see br.tennaito.rsql.builder.BuilderTools#getArgumentParser()
	 */
	@Override
	public ArgumentParser getArgumentParser() {
		if (this.argumentParser == null) {
			this.argumentParser = new DefaultArgumentParser();
		}
		return this.argumentParser;
	}

	/* (non-Javadoc)
	 * @see br.tennaito.rsql.builder.BuilderTools#getPredicateBuilder()
	 */
	@Override
	public PredicateBuilderStrategy getPredicateBuilder() {
		return this.delegate;
	}
}
