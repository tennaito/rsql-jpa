/*
 * The MIT License
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.tennaito.rsql.misc.ArgumentParser;
import br.tennaito.rsql.misc.DefaultArgumentParser;
import br.tennaito.rsql.parser.ast.ComparisonOperatorProxy;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;

public class PredicateBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(PredicateBuilder.class);

    public static final Character LIKE_WILDCARD = '*';
    public static final String NULL_ARGUMENT = "NULL";

    public static <T> Predicate createPredicate(Node node, Class<T> entity, EntityManager manager) {

        LOG.trace("Creating Predicate for: {}", node);

        if (node instanceof LogicalNode) {
            return createPredicate((LogicalNode)node, entity, manager);
        }
        if (node instanceof ComparisonNode) {
            return createPredicate((ComparisonNode)node, entity, manager);
        }

        throw new IllegalArgumentException("Unknown expression type: " + node.getClass());
    }

    public static <T> Predicate createPredicate(LogicalNode logical, Class<T> entity, EntityManager entityManager) {

    	javax.persistence.criteria.CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    	List<Predicate> predicates = new ArrayList<Predicate>();

    	for (Node node : logical.getChildren()) {
    		predicates.add(createPredicate(node, entity, entityManager));
		}

        switch (logical.getOperator()) {
            case AND : return builder.and(predicates.toArray(new Predicate[predicates.size()]));
            case OR : return builder.or(predicates.toArray(new Predicate[predicates.size()]));
        }

        throw new IllegalArgumentException("Unknown operator: " + logical.getOperator());
    }

    public static <T> Predicate createPredicate(ComparisonNode comparison, Class<T> entity, EntityManager entityManager) {
    	Metamodel metaModel = entityManager.getMetamodel();
    	ManagedType<?> classMetadata = metaModel.managedType(entity);

    	CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    	CriteriaQuery<T> criteria = builder.createQuery(entity);
    	From root = criteria.from(entity);

    	Class<?> argumentType = null;
    	Expression propertyPath = null;
    	String[] graph = comparison.getSelector().split("\\.");
    	for (String property : graph) {
    		if (hasPropertyName(property, classMetadata)) {
    			argumentType = findPropertyType(property, classMetadata);
    			if (isAssociationType(property, classMetadata)) {
    				classMetadata = metaModel.managedType(argumentType);
    				root = root.join(property);
    			} else {
    				propertyPath = root.get(property).as(argumentType);
    				break;
    			}
    		} else {
    			throw new IllegalArgumentException("Unknown property: " + property + " from entity " + classMetadata.getJavaType().getSimpleName());
    		}
		}

    	List<Object> castedArguments = createCastedArguments(comparison.getArguments(), argumentType);

    	return PredicateBuilder.createPredicate(propertyPath, comparison.getOperator(), castedArguments, entityManager);
    }

    ///////////////  TEMPLATE METHODS  ///////////////

    private static Predicate createPredicate(Expression propertyPath, ComparisonOperator operator, List<Object> arguments, EntityManager manager) {
        LOG.trace("Creating predicate: {} {} {}",
                new Object[]{propertyPath.getAlias(), operator, arguments});

        switch (ComparisonOperatorProxy.asEnum(operator)) {
            case EQUAL : {
            	Object argument = arguments.get(0);
                if (argument instanceof String) {
                    return createLike(propertyPath, (String) argument, manager);
                } else if (isNullArgument(argument)) {
                    return createIsNull(propertyPath, manager);
                } else {
                    return createEqual(propertyPath, argument, manager);
                }
            }
            case NOT_EQUAL : {
            	Object argument = arguments.get(0);
                if (argument instanceof String) {
                    return createNotLike(propertyPath, (String) argument, manager);
                } else if (isNullArgument(argument)) {
                    return createIsNotNull(propertyPath, manager);
                } else {
                    return createNotEqual(propertyPath, argument, manager);
                }
            }
            case GREATER_THAN : {
            	Object argument = arguments.get(0);
            	return createGreaterThan(propertyPath, (Number)argument, manager);
            }
            case GREATER_THAN_OR_EQUAL : {
            	Object argument = arguments.get(0);
            	return createGreaterEqual(propertyPath, (Number)argument, manager);
            }
            case LESS_THAN : {
            	Object argument = arguments.get(0);
            	return createLessThan(propertyPath, (Number)argument, manager);
            }
            case LESS_THAN_OR_EQUAL : {
            	Object argument = arguments.get(0);
            	return createLessEqual(propertyPath, (Number)argument, manager);
            }
            case IN : return createIn(propertyPath, arguments, manager);
            case NOT_IN : return createNotIn(propertyPath, arguments, manager);
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
    public static Predicate createEqual(Expression<?> propertyPath, Object argument, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
    	return builder.equal(propertyPath, argument);
    }

    public static Predicate createIn(Expression<?> propertyPath, List<?> arguments, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
    	return builder.in(propertyPath.in(arguments));
    }

    public static Predicate createNotIn(Expression<?> propertyPath, List<?> arguments, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
    	return builder.not(createIn(propertyPath,arguments, manager));
    }

    /**
     * Apply a case-insensitive "like" constraint to the named property. Value
     * should contains wildcards "*" (% in SQL) and "_".
     *
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    public static Predicate createLike(Expression<String> propertyPath, String argument, EntityManager manager) {
        String like = argument.replace(LIKE_WILDCARD, '%');
        javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.like(builder.lower(propertyPath), like.toLowerCase());
    }

    /**
     * Apply an "is null" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @return Criterion
     */
    public static Predicate createIsNull(Expression<?> propertyPath, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
    	return builder.isNull(propertyPath);
    }

    /**
     * Apply a "not equal" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    public static Predicate createNotEqual(Expression<?> propertyPath, Object argument, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.notEqual(propertyPath, argument);
    }

    /**
     * Apply a negative case-insensitive "like" constraint to the named property.
     * Value should contains wildcards "*" (% in SQL) and "_".
     *
     * @param propertyPath property name prefixed with an association alias
     * @param argument Value with wildcards.
     * @return Criterion
     */
    public static Predicate createNotLike(Expression<String> propertyPath, String argument, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.not(createLike(propertyPath, argument, manager));
    }

    /**
     * Apply an "is not null" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @return Criterion
     */
    public static Predicate createIsNotNull(Expression<?> propertyPath, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.isNotNull(propertyPath);
    }

    /**
     * Apply a "greater than" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    public static Predicate createGreaterThan(Expression<? extends Number> propertyPath, Number argument, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.ge(propertyPath, argument);
    }

    /**
     * Apply a "greater than or equal" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    public static Predicate createGreaterEqual(Expression<? extends Number> propertyPath, Number argument, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.ge(propertyPath, argument);
    }

    /**
     * Apply a "less than" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    public static Predicate createLessThan(Expression<? extends Number> propertyPath, Number argument, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.lt(propertyPath, argument);
    }

    /**
     * Apply a "less than or equal" constraint to the named property.
     *
     * @param propertyPath property name prefixed with an association alias
     * @param argument value
     * @return Criterion
     */
    public static Predicate createLessEqual(Expression<? extends Number> propertyPath, Number argument, EntityManager manager) {
    	javax.persistence.criteria.CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.le(propertyPath, argument);
    }

    private static <T> boolean  hasPropertyName(String property, ManagedType<T> classMetadata) {
        Set<Attribute<? super T, ?>> names = classMetadata.getAttributes();
        for (Attribute<? super T, ?> name : names) {
            if (name.getName().equals(property)) return true;
        }
        return false;
    }

    private static <T> boolean isAssociationType(String property, ManagedType<T> classMetadata){
    	return classMetadata.getAttribute(property).isAssociation();
    }

    private static <T> Class<?> findPropertyType(String property, ManagedType<T> classMetadata) {
        return classMetadata.getAttribute(property).getJavaType();
    }

    /**
     * @param argument
     * @return <tt>true</tt> if argument is null, <tt>false</tt> otherwise
     */
    private static boolean isNullArgument(Object argument) {
        return NULL_ARGUMENT.equals(argument);
    }

    private static <T> List<Object> createCastedArguments(List<String> arguments, Class<?> castType) {
    	ArgumentParser parser = new DefaultArgumentParser();
    	List<Object> castedArguments = new ArrayList<Object>(arguments.size());
    	for (String argument : arguments) {
    		castedArguments.add(parser.parse(argument, castType));
    	}
    	return castedArguments;
    }
}
