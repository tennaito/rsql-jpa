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

import cz.jirutka.rsql.parser.ParseException;
import cz.jirutka.rsql.parser.model.ComparisonExpression;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;

/**
 * Translates RSQL query expression into Hibernate's {@link DetachedCriteria}.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public interface RSQLCriteriaBuilder {
    
    /**
     * When Criteria Builder needs to generate a JOIN, it will use this prefix
     * and ordinal number starting at 1 for an entity alias.
     */
    static final String ALIAS_PREFIX = "alias";
    
    /**
     * Alias of the root entity.
     */
    static final String ROOT_ALIAS = "alias0";
    
    /**
     * Asceding or desceding ordering.
     */
    enum OrderType {
        ASCENDING,
        DESCENDING;
    }
    
    
    /**
     * Parse given RSQL query expression and translate it to Hibernate Criteria 
     * query. 
     * 
     * <p>This method returns {@link DetachedCriteria}, i.e. Criteria that isn't
     * connected with any Hibernate Session. Use 
     * {@link DetachedCriteria#getExecutableCriteria(org.hibernate.Session) 
     * getExecutableCriteria()} to connect with session and then execute.</p>
     * 
     * <p>If you want to add <tt>Criterion</tt> or something that refers to 
     * a property name, be aware of aliases. Root entity has alias 
     * {@link #ALIAS_FIRST}.</p>
     * 
     * @param expression RSQL query expression.
     * @return Hibernate <tt>DetachedCriteria</tt> generated from given expression.
     * @throws RSQLException If some problem occured when parsing or building
     *         Criteria. This is a wrapper exception for {@link ParseException},
     *         {@link ArgumentFormatException}, {@link JoinsLimitException},
     *         {@link UnknownSelectorException}.
     */
    DetachedCriteria parse(String expression) throws RSQLException;
    
    /**
     * Parse given RSQL query expression and translate it to Hibernate Criteria 
     * query. 
     * 
     * <p>This method returns {@link DetachedCriteria}, i.e. Criteria that isn't
     * connected with any Hibernate Session. Use 
     * {@link DetachedCriteria#getExecutableCriteria(org.hibernate.Session) 
     * getExecutableCriteria()} to connect with session and then execute.</p>
     * 
     * <p>If you want to add <tt>Criterion</tt> or something that refers to 
     * a property name, be aware of aliases. Root entity has alias 
     * {@link #ALIAS_FIRST}.</p>
     * 
     * @param expression RSQL query expression.
     * @param orderBy Selector or property name to order by.
     * @param order Ascending or descending order.
     * @return Hibernate Criteria query generated from given RSQL expression.
     * @throws RSQLException If some problem occured when parsing or building
     *         Criteria. This is a wrapper exception for {@link ParseException},
     *         {@link ArgumentFormatException}, {@link JoinsLimitException},
     *         {@link UnknownSelectorException}.
     */
    DetachedCriteria parse(String expression, String orderBy, OrderType order) throws RSQLException;
    
    /**
     * Get the stack of Criterion Builders that are used to translate specific 
     * {@link ComparisonExpression} (constraint) to Hibernate {@link Criterion}.
     * 
     * @return The current stack of Criterion Builders.
     */
    List<AbstractCriterionBuilder> getCriterionBuilders();
    
    /**
     * Push a new Criterion Builder to the top of the builders stack. 
     * Criterion Builders are used to translate specific 
     * {@link ComparisonExpression} (constraint) to Hibernate {@link Criterion}.
     * 
     * @param builder Criterion Builder
     */
    void pushCriterionBuilder(AbstractCriterionBuilder builder);

    /**
     * Get the {@link Mapper}.
     * 
     * @return The mapper used to translate selectors to property names.
     */
    Mapper getMapper();
    
    /**
     * Set <tt>Mapper</tt> that will be used to translate selectors to property
     * names.
     * 
     * @param mapping A <tt>Mapper</tt> instance, must not be <tt>null</tt>.
     */
    void setMapper(Mapper mapper);
    
}
