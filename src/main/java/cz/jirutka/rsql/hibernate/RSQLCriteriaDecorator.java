/*
 * The MIT License
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
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
package cz.jirutka.rsql.hibernate;

import cz.jirutka.commons.hibernate.criteria.AbstractCriteriaDecorator;
import org.hibernate.Criteria;

/**
 * Adapter class for Hibernate {@linkplain Criteria} which adds methods to
 * directly use RSQL when building your criteria query.
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RSQLCriteriaDecorator extends AbstractCriteriaDecorator<RSQLCriteriaDecorator> {
    
    private RSQL2CriteriaConverter rsqlConverter;
    private final Class<?> persistentClass;
    

    /**
     * Decorated criteria has to be created with the persistent class, not only
     * entity name (i.e. via {@linkplain 
     * org.hibernate.Session#createCriteria(Class) createCriteria(persistentClass)}
     * or {@linkplain org.hibernate.Session#createCriteria(Class, String) 
     * createCriteria(persistentClass, alias)})!
     * 
     * @param criteria the <tt>Criteria</tt> being decorated
     */
    public RSQLCriteriaDecorator(Criteria criteria) {
        super(criteria);
        String className = getRootEntityOrClassName();
        try {
            persistentClass = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Cannot find entity for class: " + className, ex);
        }
    }
    
    /**
     * Set a RSQL to Criteria converter to use with this Criteria query.
     * 
     * @param rsqlConverter RSQL converter
     * @return this (for method chaining)
     */
    public RSQLCriteriaDecorator setRSQLConverter(RSQL2CriteriaConverter rsqlConverter) {
        this.rsqlConverter = rsqlConverter;
        return this;
    }
    
    /**
     * Merge with {@linkplain RSQL2CriteriaConverter RSQL query}.
     * You MUST previously set the {@linkplain #setRSQLConverter RSQL converter}!
     * 
     * @param query RSQL query expression
     * @return this (for method chaining)
     * @throws RSQLException If some problem occured when converting RSQL query
     *         into criteria query.
     */
    public RSQLCriteriaDecorator mergeRSQLQuery(String query) {
        if (query != null) {
            rsqlConverter.extendCriteria(query, persistentClass, criteria);
        }
        return this;
    }  
    
    
    @Override
    protected RSQLCriteriaDecorator decorate(Criteria criteria) {
        this.criteria = criteria;
        return this;
    }

}
