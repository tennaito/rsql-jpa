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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    
    protected static final Character LIKE_WILDCARD = '*';
    protected static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd"); //ISO 8601
    protected static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //ISO 8601

    
    
    /**
     * This method is called by Criteria Builder to determine if this can handle 
     * given comparison (constraint).
     * 
     * @param property property name or path
     * @param operator comparison operator
     * @param parent reference to the parent <tt>CriteriaBuilder</tt>
     * @return <tt>true</tt> if this builder can handle given selector 
     *         and operator, otherwise <tt>false</tt>
     */
    public abstract boolean canAccept(String property, Comparison operator, CriteriaBuilder parent);
    
    /**
     * Create Hibernate <tt>Criterion</tt> for given comparison (constraint).
     * 
     * @param property property name or path
     * @param operator comparison operator
     * @param argument argument
     * @param parent reference to the parent <tt>CriteriaBuilder</tt>
     * @return Criterion
     * @throws ArgumentFormatException If given argument is not in suitable 
     *         format required by entity's property, i.e. cannot be cast to 
     *         property's type.
     * @throws UnknownSelectorException If cannot find property for given selector.
     */
    public abstract Criterion createCriterion(String property, Comparison operator, 
            String argument, CriteriaBuilder parent) 
            throws ArgumentFormatException, UnknownSelectorException;
    
    
    
    /**
     * Delegate creating of a Criterion to an appropriate method according to 
     * operator.
     * 
     * @param property Property name, may be prefixed with an alias.
     * @param operator comparison operator
     * @param argument argument
     * @return Criterion
     */
    protected Criterion createCriterion(String property, Comparison operator, Object argument) {
        LOG.trace("Creating criterion: {} {} {}", 
                new Object[]{property, operator, argument});
        
        switch (operator) {
            case EQUAL : {
                if (containWildcard(argument)) {
                    return createLike(property, argument);
                } else {
                    return createEqual(property, argument);
                }
            }
            case NOT_EQUAL : {
                if (containWildcard(argument)) {
                    return createNotLike(property, argument);
                } else {
                    return createNotEqual(property, argument);
                }
            }
            case GREATER_THAN : return createGreaterThan(property, argument);
            case GREATER_EQUAL : return createGreaterEqual(property, argument);
            case LESS_THAN : return createLessThan(property, argument);
            case LESS_EQUAL : return createLessEqual(property, argument);
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }
    
    /**
     * Apply an "equal" constraint to the named property.
     * 
     * @param property Property name, may be prefixed with an alias.
     * @param argument value
     * @return Criterion
     */
    protected Criterion createEqual(String property, Object argument) {
        return Restrictions.eq(property, argument);
    }
    
    /**
     * Apply a case-insensitive "like" constraint to the named property. Value
     * should contains wildcards "*" (% in SQL) and "_".
     * 
     * @param property Property name, may be prefixed with an alias.
     * @param argument value
     * @return Criterion
     */
    protected Criterion createLike(String property, Object argument) {
        String like = (String)argument;
        like = like.replace(LIKE_WILDCARD, '%');
        
        return Restrictions.ilike(property, like);
    }    
    
    /**
     * Apply a "not equal" constraint to the named property.
     * 
     * @param property Property name, may be prefixed with an alias.
     * @param argument value
     * @return Criterion
     */
    protected Criterion createNotEqual(String property, Object argument) {
        return Restrictions.ne(property, argument);
    }
    
    /**
     * Apply a negative case-insensitive "like" constraint to the named property. 
     * Value should contains wildcards "*" (% in SQL) and "_".
     * 
     * @param property Property name, may be prefixed with an alias.
     * @param argument Value with wildcards.
     * @return Criterion
     */
    protected Criterion createNotLike(String property, Object argument) {        
        return Restrictions.not(createLike(property, argument));
    }
    
    /**
     * Apply a "greater than" constraint to the named property.
     * 
     * @param property Property name, may be prefixed with an alias.
     * @param argument value
     * @return Criterion
     */
    protected Criterion createGreaterThan(String property, Object argument) {
        return Restrictions.gt(property, argument);
    }
    
    /**
     * Apply a "greater than or equal" constraint to the named property.
     * 
     * @param property Property name, may be prefixed with an alias.
     * @param argument value
     * @return Criterion
     */
    protected Criterion createGreaterEqual(String property, Object argument) {
        return Restrictions.ge(property, argument);
    }
    
    /**
     * Apply a "less than" constraint to the named property.
     * 
     * @param property Property name, may be prefixed with an alias.
     * @param argument value
     * @return Criterion
     */
    protected Criterion createLessThan(String property, Object argument) {
        return Restrictions.lt(property, argument);
    }
    
    /**
     * Apply a "less than or equal" constraint to the named property.
     * 
     * @param property Property name, may be prefixed with an alias.
     * @param argument value
     * @return Criterion
     */
    protected Criterion createLessEqual(String property, Object argument) {
        return Restrictions.le(property, argument);
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
     * Parse given string argument as the specified class type. Supported types 
     * are String, Integer, Long, Float, Boolean, Enum and Date. If neither one 
     * of them match, try to invoke valueOf(String s) method via reflection on
     * the type's class.
     * 
     * @param <T> class type
     * @param argument string argument
     * @param type class type
     * @return The instance of the given argument in the specified type.
     * @throws ArgumentFormatException If the given argument is not parseable 
     *         to the specified type.
     * @throws IllegalArgumentException If the specified type is not supported.
     */
    protected <T> T parseArgument(String argument, Class<T> type) 
            throws ArgumentFormatException, IllegalArgumentException {

        LOG.trace("Parsing argument {} as type {}", argument, type.getSimpleName());
        
        // common types
        try {
            if (type.equals(String.class)) return (T) argument;
            if (type.equals(Integer.class)) return (T) Integer.valueOf(argument);
            if (type.equals(Boolean.class)) return (T) Boolean.valueOf(argument);
            if (type.isEnum()) return (T) Enum.valueOf((Class<Enum>)type, argument);
            if (type.equals(Float.class)) return (T) Float.valueOf(argument);
            if (type.equals(Long.class)) return (T) Long.valueOf(argument);
        } catch (IllegalArgumentException ex) {
            throw new ArgumentFormatException(argument, type);
        }
        
        // date
        if (type.equals(Date.class)) {
            try {
                return (T) DATE_TIME_FORMATTER.parse(argument);
            } catch (ParseException ex) {}
            try {
                return (T) DATE_FORMATTER.parse(argument);
            } catch (ParseException ex1) {
                throw new ArgumentFormatException(argument, type);
            }
        }
        
        // try to parse via valueOf(String s) method
        try {
            LOG.trace("Trying to get and invoke valueOf(String s) method on {}", type);
            Method method = type.getMethod("valueOf", String.class);
            return (T) method.invoke(type, argument);
            
        } catch (NoSuchMethodException ex) {
            LOG.warn("{} does not have method valueOf(String s)", type);
        } catch (InvocationTargetException ex) {
            throw new ArgumentFormatException(argument, type);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        throw new IllegalArgumentException("Cannot parse argument type " + type);
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

}
