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
package com.github.tennaito.rsql.jpa;

import com.github.tennaito.rsql.builder.BuilderTools;
import com.github.tennaito.rsql.parser.ast.ComparisonOperatorProxy;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PredicateBuilder
 *
 * Classe with utility methods for Predicate creation from RSQL AST nodes.
 *
 * @author AntonioRabelo
 *
 * Based from CriterionBuilders of rsql-hibernate created by Jakub Jirutka <jakub@jirutka.cz>.
 *
 * @since 2015-02-05
 */
public final class PredicateBuilder {

	private static final Logger LOG = Logger.getLogger(PredicateBuilder.class.getName());

    public static final Character LIKE_WILDCARD = '*';

	private static final Date START_DATE = new Date(0L) ;

	private static final Date END_DATE = new Date(99999999999999999L);

    /**
     * Private constructor.
     */
    private PredicateBuilder(){
    	super();
    }

    /**
     * Create a Predicate from the RSQL AST node.
     *
     * @param node      RSQL AST node.
     * @param root      From that predicate expression paths depends on.
     * @param entity    The main entity of the query.
     * @param manager   JPA EntityManager.
     * @param misc      Facade with all necessary tools for predicate creation.
     * @return 			Predicate a predicate representation of the Node.
     */
    public static <T> Predicate createPredicate(Node node, From root, Class<T> entity, EntityManager manager, BuilderTools misc) {
        LOG.log(Level.INFO, "Creating Predicate for: {0}", node);

        if (node instanceof LogicalNode) {
            return createPredicate((LogicalNode)node, root, entity, manager, misc);
        }
        
        if (node instanceof ComparisonNode) {
            return createPredicate((ComparisonNode)node, root, entity, manager, misc);
        }

        throw new IllegalArgumentException("Unknown expression type: " + node.getClass());
    }

    /**
     * Create a Predicate from the RSQL AST logical node.
     *
     * @param logical        RSQL AST logical node.
     * @param root           From that predicate expression paths depends on. 
     * @param entity  		 The main entity of the query.
     * @param entityManager  JPA EntityManager.
     * @param misc      	 Facade with all necessary tools for predicate creation.
     * @return 				 Predicate a predicate representation of the Node.
     */
    public static <T> Predicate createPredicate(LogicalNode logical, From root, Class<T> entity, EntityManager entityManager, BuilderTools misc) {
        LOG.log(Level.INFO, "Creating Predicate for logical node: {0}", logical);

    	CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    	List<Predicate> predicates = new ArrayList<Predicate>();

    	LOG.log(Level.INFO, "Creating Predicates from all children nodes.");
    	for (Node node : logical.getChildren()) {
    		predicates.add(createPredicate(node, root, entity, entityManager, misc));
		}

        switch (logical.getOperator()) {
            case AND : return builder.and(predicates.toArray(new Predicate[predicates.size()]));
            case OR : return builder.or(predicates.toArray(new Predicate[predicates.size()]));
        }

        throw new IllegalArgumentException("Unknown operator: " + logical.getOperator());
    }

    /**
     * Create a Predicate from the RSQL AST comparison node.
     *
     * @param comparison	 RSQL AST comparison node.
     * @param startRoot      From that predicate expression paths depends on.
     * @param entity  		 The main entity of the query.
     * @param entityManager  JPA EntityManager.
     * @param misc      	 Facade with all necessary tools for predicate creation.
     * @return 				 Predicate a predicate representation of the Node.
     */
    public static <T> Predicate createPredicate(ComparisonNode comparison, From startRoot, Class<T> entity, EntityManager entityManager, BuilderTools misc) {
    	if (startRoot == null) {
    		String msg = "From root node was undefined.";
    		LOG.log(Level.SEVERE, msg);
    		throw new IllegalArgumentException(msg);
    	}
    	LOG.log(Level.INFO, "Creating Predicate for comparison node: {0}", comparison);

        LOG.log(Level.INFO, "Property graph path : {0}", comparison.getSelector());
        Expression propertyPath = findPropertyPath(comparison.getSelector(), startRoot, entityManager, misc);

		LOG.log(Level.INFO, "Cast all arguments to type {0}.", propertyPath.getJavaType().getName());
    	List<Object> castedArguments = misc.getArgumentParser().parse(comparison.getArguments(), propertyPath.getJavaType());

    	try {
    		// try to create a predicate
    		return PredicateBuilder.createPredicate(propertyPath, comparison.getOperator(), castedArguments, entityManager);
    	} catch (IllegalArgumentException e) {
    		// if operator dont exist try to delegate
            if (misc.getPredicateBuilder() != null) {
            	return misc.getPredicateBuilder().createPredicate(comparison, startRoot, entity, entityManager, misc);
            }
            // if no strategy was defined then there are no more operators.
            throw e;
    	}
    }

    /**
     * Find a property path in the graph from startRoot
     *
     * @param propertyPath   The property path to find.
     * @param startRoot      From that property path depends on.
     * @param entityManager  JPA EntityManager.
     * @param misc           Facade with all necessary tools for predicate creation.
     * @return               The Path for the property path
     * @throws               IllegalArgumentException if attribute of the given property name does not exist
     */
    public static <T> Path<?> findPropertyPath(String propertyPath, From startRoot, EntityManager entityManager,  BuilderTools misc) {
        String[] graph = propertyPath.split("\\.");

        Metamodel metaModel = entityManager.getMetamodel();
        ManagedType<?> classMetadata = metaModel.managedType(startRoot.getJavaType());

        Path<?> root = startRoot;

        for (String property : graph) {
            String mappedProperty = misc.getPropertiesMapper().translate(property, classMetadata.getJavaType());
            if (!hasPropertyName(mappedProperty, classMetadata)) {
				throw new IllegalArgumentException("Unknown property: " + mappedProperty + " from entity " + classMetadata.getJavaType().getName());
			}

			if (isAssociationType(mappedProperty, classMetadata)) {
				Class<?> associationType = findPropertyType(mappedProperty, classMetadata);
				String previousClass = classMetadata.getJavaType().getName();
				classMetadata = metaModel.managedType(associationType);
				LOG.log(Level.INFO, "Create a join between {0} and {1}.", new Object[] {previousClass, classMetadata.getJavaType().getName()});
				root = ((From) root).join(mappedProperty);
			} else {
				LOG.log(Level.INFO, "Create property path for type {0} property {1}.", new Object[] {classMetadata.getJavaType().getName(), mappedProperty});
				root = root.get(mappedProperty);

				if (isEmbeddedType(mappedProperty, classMetadata)) {
					Class<?> embeddedType = findPropertyType(mappedProperty, classMetadata);
					classMetadata = metaModel.managedType(embeddedType);
				}
			}
        }

        return root;
    }

    ///////////////  TEMPLATE METHODS  ///////////////

    /**
     * Create Predicate for comparison operators.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param operator      Comparison operator.
     * @param arguments     Arguments (1 for binary comparisons, n for multi-value comparisons [in, not in (out)])
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createPredicate(Expression propertyPath, ComparisonOperator operator, List<Object> arguments, EntityManager manager) {
    	LOG.log(Level.INFO, "Creating predicate: propertyPath {0} {1}", new Object[]{operator, arguments});

    	if (ComparisonOperatorProxy.asEnum(operator) != null) {
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
	    			Predicate predicate;
	    			if (argument instanceof Date){
	    				predicate = createBetweenThan(propertyPath, (Date)argument, END_DATE, manager);
	    			}else{
	    				predicate = createGreaterEqual(propertyPath, (Number)argument, manager);
	    			}
	    			return predicate;
	    		}
	    		case LESS_THAN : {
	    			Object argument = arguments.get(0);
	    			return createLessThan(propertyPath, (Number)argument, manager);
	    		}
	    		case LESS_THAN_OR_EQUAL : {
	    			Object argument = arguments.get(0);

	    			Predicate predicate;
	    			if (argument instanceof Date){
	    				predicate = createBetweenThan(propertyPath,START_DATE, (Date)argument, manager);
	    			}else{
	    				predicate = createLessEqual(propertyPath, (Number)argument, manager);
	    			}
	    			return predicate;
	    		}
	    		case IN : return createIn(propertyPath, arguments, manager);
	    		case NOT_IN : return createNotIn(propertyPath, arguments, manager);
    		}
    	}
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }

    /**
     * Creates the between than.
     *
     * @param propertyPath the property path
     * @param startDate the start date
     * @param argument the argument
     * @param manager the manager
     * @return the predicate
     */
    private static Predicate createBetweenThan(Expression propertyPath, Date start, Date end, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
    	return builder.between(propertyPath, start, end);
	}

	/**
     * Apply a case-insensitive "like" constraint to the property path. Value
     * should contains wildcards "*" (% in SQL) and "_".
     *
     * @param propertyPath  Property path that we want to compare.
     * @param argument      Argument with/without wildcards
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createLike(Expression<String> propertyPath, String argument, EntityManager manager) {
        String like = argument.replace(LIKE_WILDCARD, '%');
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.like(builder.lower(propertyPath), like.toLowerCase());
    }

    /**
     * Apply an "is null" constraint to the property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createIsNull(Expression<?> propertyPath, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
    	return builder.isNull(propertyPath);
    }

    /**
     * Apply an "equal" constraint to property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param argument      Argument
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createEqual(Expression<?> propertyPath, Object argument, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
    	return builder.equal(propertyPath, argument);
    }

    /**
     * Apply a "not equal" constraint to the property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param argument      Argument
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createNotEqual(Expression<?> propertyPath, Object argument, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.notEqual(propertyPath, argument);
    }

    /**
     * Apply a negative case-insensitive "like" constraint to the property path.
     * Value should contains wildcards "*" (% in SQL) and "_".
     *
     * @param propertyPath  Property path that we want to compare.
     * @param argument      Argument with/without wildcards
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createNotLike(Expression<String> propertyPath, String argument, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.not(createLike(propertyPath, argument, manager));
    }

    /**
     * Apply an "is not null" constraint to the property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createIsNotNull(Expression<?> propertyPath, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.isNotNull(propertyPath);
    }

    /**
     * Apply a "greater than" constraint to the property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param argument      Argument number.
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createGreaterThan(Expression<? extends Number> propertyPath, Number argument, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.gt(propertyPath, argument);
    }

    /**
     * Apply a "greater than or equal" constraint to the property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param argument      Argument number.
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createGreaterEqual(Expression<? extends Number> propertyPath, Number argument, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.ge(propertyPath, argument);
    }

    /**
     * Apply a "less than" constraint to the property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param argument      Argument number.
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createLessThan(Expression<? extends Number> propertyPath, Number argument, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.lt(propertyPath, argument);
    }

    /**
     * Apply a "less than or equal" constraint to the property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param argument      Argument number.
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createLessEqual(Expression<? extends Number> propertyPath, Number argument, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
        return builder.le(propertyPath, argument);
    }

    /**
     * Apply a "in" constraint to the property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param arguments     List of arguments.
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createIn(Expression<?> propertyPath, List<?> arguments, EntityManager manager) {
    	return propertyPath.in(arguments);
    }

    /**
     * Apply a "not in" (out) constraint to the property path.
     *
     * @param propertyPath  Property path that we want to compare.
     * @param arguments     List of arguments.
     * @param manager       JPA EntityManager.
     * @return              Predicate a predicate representation.
     */
    private static Predicate createNotIn(Expression<?> propertyPath, List<?> arguments, EntityManager manager) {
    	CriteriaBuilder builder = manager.getCriteriaBuilder();
    	return builder.not(createIn(propertyPath,arguments, manager));
    }

    /**
     * Verify if a property is an Association type.
     *
     * @param property       Property to verify.
     * @param classMetadata  Metamodel of the class we want to check.
     * @return               <tt>true</tt> if the property is an associantion, <tt>false</tt> otherwise.
     */
    private static <T> boolean isAssociationType(String property, ManagedType<T> classMetadata){
    	return classMetadata.getAttribute(property).isAssociation();
    }

    /**
     * Verify if a property is an Embedded type.
     *
     * @param property       Property to verify.
     * @param classMetadata  Metamodel of the class we want to check.
     * @return               <tt>true</tt> if the property is an embedded attribute, <tt>false</tt> otherwise.
     */
    private static <T> boolean isEmbeddedType(String property, ManagedType<T> classMetadata){
        return classMetadata.getAttribute(property).getPersistentAttributeType() == PersistentAttributeType.EMBEDDED;
    }

    /**
     * Verifies if a class metamodel has the specified property.
     *
     * @param property       Property name.
     * @param classMetadata  Class metamodel that may hold that property.
     * @return               <tt>true</tt> if the class has that property, <tt>false</tt> otherwise.
     */
    private static <T> boolean  hasPropertyName(String property, ManagedType<T> classMetadata) {
        Set<Attribute<? super T, ?>> names = classMetadata.getAttributes();
        for (Attribute<? super T, ?> name : names) {
            if (name.getName().equals(property)) return true;
        }
        return false;
    }

    /**
     * Get the property Type out of the metamodel.
     *
     * @param property       Property name for type extraction.
     * @param classMetadata  Reference class metamodel that holds property type.
     * @return               Class java type for the property, 
     * 						 if the property is a pluralAttribute it will take the bindable java type of that collection.
     */
    private static <T> Class<?> findPropertyType(String property, ManagedType<T> classMetadata) {
    	Class<?> propertyType = null;
    	if (classMetadata.getAttribute(property).isCollection()) {
    		propertyType = ((PluralAttribute)classMetadata.getAttribute(property)).getBindableJavaType();
    	} else {
    		propertyType = classMetadata.getAttribute(property).getJavaType();
    	}
        return propertyType;
    }

    /**
     * Verifies if the argument is null.
     *
     * @param argument
     * @return <tt>true</tt> if argument is null, <tt>false</tt> otherwise
     */
    private static boolean isNullArgument(Object argument) {
        return argument == null;
    }
}
