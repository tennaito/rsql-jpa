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
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.TokenMgrError;
import cz.jirutka.rsql.parser.model.ComparisonExpression;
import cz.jirutka.rsql.parser.model.Expression;
import cz.jirutka.rsql.parser.model.LogicalExpression;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation of the {@link RSQLCriteriaBuilder}.
 * 
 * @see RSQLHibernateFactory
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RSQLCriteriaBuilderImpl implements RSQLCriteriaBuilder, CriteriaBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(RSQLCriteriaBuilderImpl.class);
    
    private final Class<?> entityClass;
    private final SessionFactory sessionFactory;
    private final List<AbstractCriterionBuilder> builders = new LinkedList<AbstractCriterionBuilder>();
    private Mapper mapper = new SimpleMapper(); //default
    private int joinsLimit = 3; //default
    
    private DetachedCriteria criteria;
    private Map<String, String> aliases;
    private int joins;

    
    
    /**
     * Construct a new Criteria Builder for given entity class.
     * 
     * @param entityClass The root entity class to build <tt>Criteria</tt> for. 
     * @param sessionFactory Hibernate <tt>SessionFactory</tt> that will used to 
     *        obtain entities <tt>ClassMetadata</tt>.
     */
    protected RSQLCriteriaBuilderImpl(Class<?> entityClass, SessionFactory sessionFactory) {
        this.entityClass = entityClass;
        this.sessionFactory = sessionFactory;
    }
    
    
    @Override
    public DetachedCriteria parse(String expression) throws RSQLException {
        Expression expTree;
        try {
            LOG.info("Parsing expression: {}", expression);
            expTree = RSQLParser.parse(expression);
        } catch (ParseException ex) {
            throw new RSQLException(ex);
        } catch (TokenMgrError er) {
            throw new RSQLException(er);
        }
        
        return createCriteria(expTree);
    }
    
    @Override
    public DetachedCriteria parse(String expression, String orderBy, OrderType order) 
            throws RSQLException {
        DetachedCriteria result = parse(expression);
        
        if (orderBy != null) {
            orderBy = ROOT_ALIAS +'.'+ mapper.translate(orderBy, entityClass);
            result.addOrder(order == OrderType.ASCENDING
                            ? Order.asc(orderBy) : Order.desc(orderBy));
        }
        return result;
    }
    
    @Override
    public String addJoin(String property) throws JoinsLimitException {
        // if already joined
        if (aliases.containsKey(property)) {
            return aliases.get(property);
        }
        
        // create new join
        joins++;
        if (joins > joinsLimit) {
            throw new JoinsLimitException(joinsLimit);
        }
        String alias = ALIAS_PREFIX + String.valueOf(joins);
        LOG.debug("Adding JOIN for {} with alias {}", property, alias);
        aliases.put(property, alias);
        criteria.createCriteria(property, alias, Criteria.LEFT_JOIN);

        return alias;
    }
    
    /**
     * Initialize builder and create Criteria for given RSQL expression tree.
     * 
     * @param expression RSQL <tt>Expression</tt>
     * @return <tt>DetachedCriteria</tt>
     * @throws RSQLException
     */
    protected synchronized DetachedCriteria createCriteria(Expression expression) 
            throws RSQLException {
        joins = 0;
        aliases = new HashMap<String, String>(3);              
        criteria = DetachedCriteria.forClass(entityClass, ROOT_ALIAS);
        
        Criterion criterion = createCriterion(expression);
        criteria.add(criterion);
        
        return criteria;
    }
    
    /**
     * Delegate given expression instance to 
     * {@link #createCriterion(LogicalExpression)} or 
     * {@link #createCriterion(ComparisonExpression)} according to its type.
     * 
     * @param expression Instance of {@link LogicalExpression} or 
     *                   {@link ComparisonExpression}.
     * @return Criterion
     * @throws RSQLException
     * @throws IllegalArgumentException If cannot be cast to 
     *         {@link LogicalExpression} nor {@link ComparisonExpression}.
     */
    private Criterion createCriterion(Expression expression) 
            throws RSQLException, IllegalArgumentException {
        
        LOG.trace("Creating criterion for: {}", expression);
        
        if (expression.isLogical()) {
            return createCriterion((LogicalExpression)expression);
        }
        
        if (expression.isComparison()) {
            try {
                return createCriterion((ComparisonExpression)expression);
                
            } catch (ArgumentFormatException ex) {
                throw new RSQLException(ex);
            } catch (UnknownSelectorException ex) {
                throw new RSQLException(ex);
            }
        }
        
        throw new IllegalArgumentException("Unknown expression type: " + expression.getClass());
    }
    
    /**
     * Create Hibernate Criterion for given logical expression.
     * 
     * @param exp Logical expression
     * @return Criterion generated from given logical expression.
     * @throws RSQLException
     * @throws IllegalArgumentException If expression contains unsupported 
     *         operator.
     */
    private Criterion createCriterion(LogicalExpression exp) 
            throws RSQLException, IllegalArgumentException {
        
        switch (exp.getOperator()) {
            case AND : return Restrictions.and(createCriterion(exp.getLeft()), 
                                               createCriterion(exp.getRight()));
                
            case OR : return Restrictions.or(createCriterion(exp.getLeft()), 
                                             createCriterion(exp.getRight()));
        }
        
        throw new IllegalArgumentException("Unknown operator: " + exp.getOperator());
    }
    
    /**
     * Create Hibernate Criterion for given comparison expression (constraint).
     * It iterates over Criterion builders stack to find builder that can handle
     * given expression (its canAccept() method returns <tt>true</tt>) and then 
     * delegate to it. Selector is translated to property name or path by 
     * {@link Mapper}.
     * 
     * @param exp comparison expression (constraint)
     * @return Criterion generated from given comparison expression.
     * @throws ArgumentFormatException If argument is not in suitable format
     *         required by entity's property, i.e. is not parseable to the 
     *         specified type.
     * @throws UnknownSelectorException If cannot find property for selector.
     * @throws IllegalStateException If cannot find Criteria Builder to
     *         handle this epxression.
     */
    private Criterion createCriterion(ComparisonExpression exp) 
            throws ArgumentFormatException, UnknownSelectorException, IllegalStateException {
        
        String property = mapper.translate(exp.getSelector(), entityClass);

        for (AbstractCriterionBuilder builder : builders) {
            if (builder.canAccept(property, exp.getOperator(), this)) {
                LOG.debug("Delegating comparison '{}' to builder: {}", 
                        exp, builder.getClass().getSimpleName());
                try {
                    return builder.createCriterion(property, exp.getOperator(), exp.getArgument(), this);
                    
                } catch (ArgumentFormatException ex) {
                    throw new ArgumentFormatException(exp.getSelector(), 
                            exp.getArgument(), ex.getPropertyType());
                }
            }
        }

        throw new IllegalStateException("No Criterion Builder found for: " + exp);
    }

    
    @Override
    public List<AbstractCriterionBuilder> getCriterionBuilders() {
        return builders;
    }
    
    @Override
    public void pushCriterionBuilder(AbstractCriterionBuilder builder) {
        builders.add(0, builder);
    }
    
    @Override
    public Mapper getMapper() {
        return mapper;
    }

    @Override
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public ClassMetadata getClassMetadata() {
        return sessionFactory.getClassMetadata(entityClass);
    }

    @Override
    public ClassMetadata getClassMetadata(Class<?> entityClass) {
        return sessionFactory.getClassMetadata(entityClass);
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }
    
    /**
     * @see RSQLCriteriaBuilderImpl#setJoinsLimit(int)
     * @return Upper limit of JOINs that can be generated.
     */
    public int getJoinsLimit() {
        return joinsLimit;
    }

    /**
     * JOINs are quite expensive operations so you should limit number of JOINs
     * that can be generated per one query. Default value is preset by 
     * {@link RSQLHibernateFactory}.
     * 
     * @param joinsLimit Upper limit of JOINs that can be generated. Must be 
     *        greater or equal 0.
     */
    public void setJoinsLimit(int limit) {
        this.joinsLimit = limit;
    }
    
}
