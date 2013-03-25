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

import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.metadata.ClassMetadata;

/**
 * Interface of stateful builder class related to {@link RSQLCriteriaBuilder} 
 * that is used by {@linkplain AbstractCriterionBuilder Criterion Builders}.
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public interface CriteriaBuilder {
    
    /**
     * Create an alias for given association path. In other words, create JOIN 
     * with entity that is associated by specified property and return its alias. 
     * If such association alias already exists, only return alias. Default join
     * type is INNER_JOIN.
     * 
     * @param associationPath A dot-separated path of the association property.
     * @return association alias
     * @throws AssociationsLimitException If allowed number of associations 
     *         was exceeded.
     */
    String createAssociationAlias(String associationPath) throws AssociationsLimitException;
    
    /**
     * Create an alias for given association path. In other words, create JOIN 
     * with entity that is associated by specified property and return its alias. 
     * If such association alias already exists, only return alias.
     * 
     * @param associationPath A dot-separated path of the association property.
     * @param joinType {@link Criteria#INNER_JOIN}, {@link Criteria#LEFT_JOIN}
     *        or {@link Criteria#FULL_JOIN}.
     * @return association alias
     * @throws AssociationsLimitException If allowed number of associations 
     *         was exceeded.
     */
    String createAssociationAlias(String associationPath, int joinType) throws AssociationsLimitException;
    
    /**
     * Delegate given comparison to builder that can handle it.
     * 
     * It iterates over Criterion builders stack to find builder that can handle
     * given expression (its accept() method returns <tt>true</tt>) and then 
     * delegate to it.
     * 
     * @param property property name or path
     * @param operator comparison operator
     * @param argument argument
     * @param entityClass Class of entity that holds given property.
     * @param alias Alias (incl. dot) that will be used to prefix propery name.
     * @return Criterion generated from given comparison expression.
     * @throws ArgumentFormatException If argument is not in suitable format
     *         required by entity's property, i.e. is not parseable to the 
     *         specified type.
     * @throws UnknownSelectorException If such property does not exist.
     * @throws IllegalArgumentException If cannot find Criteria Builder to
     *         handle this epxression.
     */
    Criterion delegateToBuilder(String property, Comparison operator, String argument, Class<?> entityClass, String alias)
            throws ArgumentFormatException, UnknownSelectorException, IllegalStateException;
    
    /**
     * Get Argument Parser for parsing string arguments from query.
     * 
     * @return argument parser
     */
    ArgumentParser getArgumentParser();
    
    /**
     * Retrieve {@link ClassMetadata} associated with the given entity class.
     * 
     * @param entityClass entity class
     * @return The metadata associated with the given entity or null if no 
     *         such entity was mapped.
     */
    ClassMetadata getClassMetadata(Class<?> entityClass);
    
    /**
     * Get Mapper used to translate selectors to property names.
     * 
     * @return The mapper used to translate selectors to property names.
     */
    Mapper getMapper();
    
    /**
     * Get association alias of the root entity.
     * 
     * @return root alias
     */
    String getRootAlias();
    
}
