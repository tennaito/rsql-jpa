/*
 * Copyright (c) 2011 Jakub Jirutka <jakub@jirutka.cz>
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

import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass of all Criterion Builders.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public abstract class AbstractCriterionBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCriterionBuilder.class);

    public static final Character LIKE_WILDCARD = '*';
    public static final String NULL_ARGUMENT = "NULL";
    
    
    
    ///////////////  ABSTRACT METHODS  ///////////////
    
    /**
     * This method is called by Criteria Builder to determine if this builder 
     * can handle given comparison (constraint).
     * 
     * @param property property name or path
     * @param entityClass Class of entity that holds given property.
     * @param parent Reference to the parent <tt>CriteriaBuilder</tt>.
     * @return <tt>true</tt> if this builder can handle given property of entity
     *         class, otherwise <tt>false</tt>
     */
    public abstract boolean accept(String property, Class<?> entityClass, CriteriaBuilder parent);
    
    /**
     * Create <tt>Criterion</tt> for given comparison (constraint).
     * 
     * @param property property name or path
     * @param operator comparison operator
     * @param argument argument
     * @param entityClass Class of entity that holds given property.
     * @param alias Association alias (incl. dot) which must be used to prefix 
     *        property name!
     * @param parent Reference to the parent <tt>CriteriaBuilder</tt>.
     * @return Criterion
     * @throws ArgumentFormatException If given argument is not in suitable 
     *         format required by entity's property, i.e. cannot be cast to 
     *         property's type.
     * @throws UnknownSelectorException If such property does not exist.
     */
    public abstract Criterion createCriterion(String property, Comparison operator, String argument, 
            Class<?> entityClass, String alias, CriteriaBuilder parent) 
            throws ArgumentFormatException, UnknownSelectorException;
    
    
    
    
    ///////////////  TEMPLATE METHODS  ///////////////
    
    /**
     * Delegate creating of a Criterion to an appropriate method according to 
     * operator. 
     * 
     * Property name MUST be prefixed with an association alias!
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param operator comparison operator
     * @param argument argument
     * @return Criterion
     */
    protected Criterion createCriterion(String propertyPath, Comparison operator, Object argument) {
        LOG.trace("Creating criterion: {} {} {}", 
                new Object[]{propertyPath, operator, argument});
        
        switch (operator) {
            case EQUAL : {
                if (containWildcard(argument)) {
                    return createLike(propertyPath, argument);
                } else if (isNullArgument(argument)) {
                    return createIsNull(propertyPath);
                } else {
                    return createEqual(propertyPath, argument);
                }
            }
            case NOT_EQUAL : {
                if (containWildcard(argument)) {
                    return createNotLike(propertyPath, argument);
                } else if (isNullArgument(argument)) {
                    return createIsNotNull(propertyPath);
                } else {
                    return createNotEqual(propertyPath, argument);
                }
            }
            case GREATER_THAN : return createGreaterThan(propertyPath, argument);
            case GREATER_EQUAL : return createGreaterEqual(propertyPath, argument);
            case LESS_THAN : return createLessThan(propertyPath, argument);
            case LESS_EQUAL : return createLessEqual(propertyPath, argument);
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }
    
    /**
     * Apply an "equal" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createEqual(String propertyPath, Object argument) {
        return Restrictions.eq(propertyPath, argument);
    }
    
    /**
     * Apply a case-insensitive "like" constraint to the named property. Value
     * should contains wildcards "*" (% in SQL) and "_".
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createLike(String propertyPath, Object argument) {
        String like = (String)argument;
        like = like.replace(LIKE_WILDCARD, '%');
        
        return Restrictions.ilike(propertyPath, like);
    }

    /**
     * Apply an "is null" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @return Criterion
     */
    protected Criterion createIsNull(String propertyPath) {
        return Restrictions.isNull(propertyPath);
    }
    
    /**
     * Apply a "not equal" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createNotEqual(String propertyPath, Object argument) {
        return Restrictions.ne(propertyPath, argument);
    }
    
    /**
     * Apply a negative case-insensitive "like" constraint to the named property. 
     * Value should contains wildcards "*" (% in SQL) and "_".
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument Value with wildcards.
     * @return Criterion
     */
    protected Criterion createNotLike(String propertyPath, Object argument) {        
        return Restrictions.not(createLike(propertyPath, argument));
    }

    /**
     * Apply an "is not null" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @return Criterion
     */
    protected Criterion createIsNotNull(String propertyPath) {
        return Restrictions.isNotNull(propertyPath);
    }
    
    /**
     * Apply a "greater than" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createGreaterThan(String propertyPath, Object argument) {
        return Restrictions.gt(propertyPath, argument);
    }
    
    /**
     * Apply a "greater than or equal" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createGreaterEqual(String propertyPath, Object argument) {
        return Restrictions.ge(propertyPath, argument);
    }
    
    /**
     * Apply a "less than" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createLessThan(String propertyPath, Object argument) {
        return Restrictions.lt(propertyPath, argument);
    }
    
    /**
     * Apply a "less than or equal" constraint to the named property.
     * 
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    protected Criterion createLessEqual(String propertyPath, Object argument) {
        return Restrictions.le(propertyPath, argument);
    }
    
    /**
     * Check if given argument contains wildcard.
     * 
     * @param argument
     * @return Return <tt>true</tt> if argument contains wildcard 
     *         {@link #LIKE_WILDCHAR}.
     */
    protected boolean containWildcard(Object argument) {
        if (!(argument instanceof String)) {
            return false;
        }
        
        String casted = (String) argument;
        if (casted.contains(LIKE_WILDCARD.toString())) {
            return true;
        }
        
        return false;
    }   
    
    /**
     * Check if entity of specified class metadata contains given property.
     * 
     * @param property property name
     * @param classMetadata entity metadata
     * @return <tt>true</tt> if specified class metadata contains given property,
     *         otherwise <tt>false</tt>.
     */
    protected boolean isPropertyName(String property, ClassMetadata classMetadata) {
        String[] names = classMetadata.getPropertyNames();
        for (String name : names) {
            if (name.equals(property)) return true;
        }
        return false;
    }
    
    /**
     * Find the java type class of given named property in entity's metadata.
     * 
     * @param property property name
     * @param classMetadata entity metadata
     * @return The java type class of given property.
     * @throws HibernateException If entity does not contain such property.
     */
    protected Class<?> findPropertyType(String property, ClassMetadata classMetadata) 
            throws HibernateException {
        return classMetadata.getPropertyType(property).getReturnedClass();
    }

    /**
     * @param argument
     * @return <tt>true</tt> if argument is null, <tt>false</tt> otherwise
     */
    protected boolean isNullArgument(Object argument) {
        return NULL_ARGUMENT.equals(argument);
    }

}
