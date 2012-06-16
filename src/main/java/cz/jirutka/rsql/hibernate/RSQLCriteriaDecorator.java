/*
 * Copyright (c) 2012 Jakub Jirutka <jakub@jirutka.cz>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the  GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
