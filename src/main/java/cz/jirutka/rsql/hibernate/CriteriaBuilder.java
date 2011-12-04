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
