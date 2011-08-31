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

import org.hibernate.metadata.ClassMetadata;

/**
 * Special interface of <tt>RSQLCriteriaBuilder</tt> that is used by 
 * {@linkplain AbstractCriterionBuilder Criterion Builders}.
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public interface CriteriaBuilder {
    
    /**
     * Create JOIN with entity that is associated by specified property and
     * return its alias. If such JOIN already exists, only return its alias.
     * 
     * @param property A dot-separated path of the property that is associated 
     *        with entity for which JOIN will be created.
     * @return Alias of joined entity.
     * @throws JoinsLimitException If allowed number of joins was exceeded.
     */
    String addJoin(String property) throws JoinsLimitException;

    /**
     * Retrieve {@link ClassMetadata} associated with the root entity class.
     * 
     * @return The metadata associated with the given entity or null if no 
     *         such entity was mapped.
     */
    ClassMetadata getClassMetadata();
    
    /**
     * Retrieve {@link ClassMetadata} associated with the given entity class.
     * 
     * @param entityClass entity class
     * @return The metadata associated with the given entity or null if no 
     *         such entity was mapped.
     */
    ClassMetadata getClassMetadata(Class<?> entityClass);
    
    /**
     * Get the root entity class.
     * 
     * @return The root entity class.
     */
    Class<?> getEntityClass();
    
    /**
     * Get the {@link Mapper}.
     * 
     * @return The mapper used to translate selectors to property names.
     */
    Mapper getMapper();
    
}
