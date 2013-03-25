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

import cz.jirutka.rsql.parser.ParseException;
import cz.jirutka.rsql.parser.model.ComparisonExpression;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;

/**
 * RSQL to Hibernate Criteria Converter
 * 
 * <p>Converts RSQL query expression into Hibernate's {@link Criteria} 
 * or {@link DetachedCriteria}.</p>
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public interface RSQL2CriteriaConverter {
    
    /**
     * When Criteria Builder needs to generate an association alias (aka JOIN), 
     * it will use this prefix and ordinal number starting at 1 for an entity 
     * alias.
     */
    static final String ALIAS_PREFIX = "alias";
    
    /**
     * Default association alias of the root entity.
     */
    static final String ROOT_ALIAS = "this";
    
    
    /**
     * Parse given RSQL query expression, bind it to given entity class and 
     * convert to Hibernate Criteria query. 
     * 
     * <p>This method returns {@link DetachedCriteria}, i.e. {@linkplain Criteria} 
     * that isn't connected with any Hibernate Session. Use 
     * {@link DetachedCriteria#getExecutableCriteria(org.hibernate.Session) 
     * getExecutableCriteria()} to connect with session and then execute.</p>
     * 
     * <p>If you want to add a {@linkplain Criterion} or something that refers to 
     * a property name, be aware of aliases. Root entity has alias 
     * {@link #ROOT_ALIAS}.</p>
     * 
     * @param query RSQL query expression.
     * @param entityClass Entity class which given query is related to.
     * @return Criteria query generated from given RSQL expression.
     * @throws RSQLException If some problem occured when parsing or building
     *         Criteria. This is a wrapper exception for {@link ParseException},
     *         {@link ArgumentFormatException}, {@link JoinsLimitException},
     *         {@link UnknownSelectorException}.
     */
    DetachedCriteria createCriteria(String query, Class<?> entityClass) throws RSQLException;
    
    /**
     * Parse given RSQL query expression, bind it to given entity class and 
     * convert to Hibernate Criteria query.  
     * 
     * <p>This method returns {@link DetachedCriteria}, i.e. {@linkplain Criteria} 
     * that isn't connected with any Hibernate Session. Use 
     * {@link DetachedCriteria#getExecutableCriteria(org.hibernate.Session) 
     * getExecutableCriteria()} to connect with session and then execute.</p>
     * 
     * <p>If you want to add a {@linkplain Criterion} or something that refers to 
     * a property name, be aware of aliases. Root entity has alias 
     * {@link #ROOT_ALIAS}.</p>
     * 
     * @param query RSQL query expression.
     * @param orderBy Selector or property name to order by.
     * @param ascending <tt>true</tt> ascending order, 
     *                  <tt>false</tt> descending order
     * @param entityClass Entity class which given query is related to.
     * @return Criteria query generated from given RSQL expression.
     * @throws RSQLException If some problem occured when parsing or building
     *         Criteria. This is a wrapper exception for {@link ParseException},
     *         {@link ArgumentFormatException}, {@link JoinsLimitException},
     *         {@link UnknownSelectorException}.
     */
    DetachedCriteria createCriteria(String query, String orderBy, boolean ascending, Class<?> entityClass) 
            throws RSQLException;
    
    /**
     * Parse RSQL query expression, bind it to given entity class, convert 
     * and append to given Criteria query.
     * 
     * @param query RSQL query expression.
     * @param entityClass Entity class which given query is related to.
     * @param targetCriteria Criteria which will be extended.
     * @throws RSQLException If some problem occured when parsing or building
     *         Criteria. This is a wrapper exception for {@link ParseException},
     *         {@link ArgumentFormatException}, {@link JoinsLimitException},
     *         {@link UnknownSelectorException}.
     */
    void extendCriteria(String query, Class<?> entityClass, Criteria targetCriteria) 
            throws RSQLException;

    
    
    /**
     * Get Argument Parser that is used in Criterion Builders for parsing 
     * string arguments from query.
     * 
     * @return The <tt>ArgumentParser</tt>
     */
    ArgumentParser getArgumentParser();
    

    /**
     * Set Argument Parser that is used in Criterion Builders for parsing 
     * string arguments from query.
     * 
     * @param argumentParser An <tt>ArgumentParser</tt> instance, 
     *        must not be <tt>null</tt>.
     */
    void setArgumentParser(ArgumentParser argumentParser);
    
    /**
     * Set the maximum number of associations that can be aliased, e.g. how many 
     * JOINs can be generated. When this limit is exceeded, 
     * a {@link AssociationsLimitException} is thrown.
     * 
     * @see RSQL2CriteriaConverter#setAssociationsLimit(int)
     * @return Upper limit of associations that can be handled.
     */
    public int getAssociationsLimit();

    /**
     * Set the maximum number of associations that can be aliased, e.g. how many 
     * JOINs can be generated. When this limit is exceeded, 
     * a {@link AssociationsLimitException} is thrown.
     * 
     * <p>JOINs are quite expensive operations so you should limit number of 
     * JOINs that can be generated per one query to prevent users writing bad 
     * queries.</p>
     * 
     * @param joinsLimit Upper limit of associations that can be handled. 
     *        Must be greater or equal 0.
     */
    public void setAssociationsLimit(int limit);
    
    /**
     * Return the stack of Criterion Builders that are used to convert specific 
     * {@linkplain ComparisonExpression} (constraint) to {@linkplain Criterion}.
     * 
     * @return The current stack of Criterion Builders.
     */
    List<AbstractCriterionBuilder> getCriterionBuilders();
    
    /**
     * Push a new Criterion Builder to the top of the builders stack. 
     * Criterion Builders are used to convert specific 
     * {@linkplain ComparisonExpression} (constraint) to {@linkplain Criterion}.
     * 
     * @param builder An Criterion Builder instance.
     */
    void pushCriterionBuilder(AbstractCriterionBuilder builder);

    /**
     * Get Mapper used to translate selectors to property names.
     * 
     * @return The <tt>Mapper</tt>
     */
    Mapper getMapper();
    
    /**
     * Set Mapper that is used to translate selectors to property names.
     * 
     * @param mapping A <tt>Mapper</tt> instance, must not be <tt>null</tt>.
     */
    void setMapper(Mapper mapper);
    
}
