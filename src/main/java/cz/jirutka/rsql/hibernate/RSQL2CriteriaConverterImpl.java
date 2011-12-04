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
import cz.jirutka.rsql.parser.model.Comparison;
import cz.jirutka.rsql.parser.model.ComparisonExpression;
import cz.jirutka.rsql.parser.model.Expression;
import cz.jirutka.rsql.parser.model.LogicalExpression;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.CriteriaImpl.Subcriteria;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation of the {@link RSQL2CriteriaConverter}.
 * 
 * @see RSQLHibernateFactory
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RSQL2CriteriaConverterImpl implements RSQL2CriteriaConverter {
    
    private static final Logger LOG = LoggerFactory.getLogger(RSQL2CriteriaConverterImpl.class);
    
    private final SessionFactory sessionFactory;
    private List<AbstractCriterionBuilder> builders = new LinkedList<AbstractCriterionBuilder>();
    private ArgumentParser argumentParser;
    private Mapper mapper;
    private int associationsLimit = -1; //default

    
    
    /**
     * Construct a new RSQL to Criteria Converter.
     * 
     * @param sessionFactory Hibernate <tt>SessionFactory</tt> that will used to 
     *        obtain entities' <tt>ClassMetadata</tt>.
     */
    public RSQL2CriteriaConverterImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    
    @Override
    public DetachedCriteria createCriteria(String query, Class<?> entityClass) throws RSQLException {
        Expression queryTree;
        try {
            LOG.info("Parsing query: {}", query);
            queryTree = RSQLParser.parse(query);
            
        } catch (ParseException ex) {
            throw new RSQLException(ex);
        } catch (TokenMgrError er) {
            throw new RSQLException(er);
        }
        
        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass, ROOT_ALIAS);
        // convert query into this criteria
        new InnerBuilder(entityClass).convert(queryTree, criteria);
        
        return criteria;
    }
    
    @Override
    public DetachedCriteria createCriteria(String query, String orderBy, boolean ascending, Class<?> entityClass) 
            throws RSQLException {
        assert orderBy != null : "orderBy must not be null!";
        
        DetachedCriteria result = createCriteria(query, entityClass);
        
        orderBy = ROOT_ALIAS +'.'+ mapper.translate(orderBy, entityClass);
        result.addOrder(ascending ? Order.asc(orderBy) : Order.desc(orderBy));
            
        return result;
    }
    
    @Override
    public void extendCriteria(String query, Class<?> entityClass, Criteria criteria) throws RSQLException {
        Expression queryTree;

        try {
            LOG.info("Parsing query: {}", query);
            queryTree = RSQLParser.parse(query);
            
        } catch (ParseException ex) {
            throw new RSQLException(ex);
        } catch (TokenMgrError er) {
            throw new RSQLException(er);
        }
        
        // convert query into this criteria
        new InnerBuilder(entityClass).convert(queryTree, criteria);
    }    

    
    
    @Override
    public ArgumentParser getArgumentParser() {
        return argumentParser;
    }

    @Override
    public void setArgumentParser(ArgumentParser argumentParser) {
        this.argumentParser = argumentParser;
    }

    @Override
    public int getAssociationsLimit() {
        return associationsLimit;
    }

    @Override
    public void setAssociationsLimit(int limit) {
        assert limit >= -1 : "must be greater or equal -1";
        this.associationsLimit = limit;
    }
    
    
    @Override
    public List<AbstractCriterionBuilder> getCriterionBuilders() {
        return builders;
    }
    
    public void setCriterionBuilders(List<AbstractCriterionBuilder> builders) {
        this.builders = builders;
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

    
    
        
    ///////////////  INNER CLASSES  ///////////////
    
    /**
     * Inner class for building Criteria from parsed RSQL expression.
     */
    protected class InnerBuilder implements CriteriaBuilder {
        
        private final Map<String, String> aliases = new HashMap<String, String>(3);
        private final Class<?> entityClass;
        private CriteriaSpecification criteria;  // Criteria or DetachedCriteria
        private String rootAlias;
        private int associations = 0;  // number of aliases created by this builder

        
        protected InnerBuilder(Class<?> entityClass) {
            this.entityClass = entityClass;
        }


        /**
         * Convert given RSQL query tree to Criterions and append it to given
         * <i>empty</i> {@linkplain DetachedCriteria}.
         * 
         * @param queryTree RSQL query expression tree.
         * @param criteria Criteria which will be extended by given query.
         * @throws RSQLException 
         */
        protected void convert(Expression queryTree, DetachedCriteria criteria) throws RSQLException {
            this.criteria = criteria;
            this.rootAlias = ROOT_ALIAS;
            Criterion criterion = createCriterion(queryTree);
            criteria.add(criterion);
        }
        
        /**
         * Convert given RSQL query tree to Criterions and append it to given
         * {@linkplain Criteria}. This Criteria may already contain some 
         * Criterions and Subcriterions (aliases). Hence first load all 
         * associations aliases from given Criteria and the root alias.
         * 
         * @param queryTree RSQL query expression tree.
         * @param criteria Criteria which will be extended by given query.
         * @throws RSQLException 
         */
        protected void convert(Expression queryTree, Criteria criteria) throws RSQLException {
            this.criteria = criteria;
            this.rootAlias = loadAssociationAliases(criteria);
            Criterion criterion = createCriterion(queryTree);
            criteria.add(criterion);
        }
        
        
        /**
         * Extract all association aliases from given Criteria and put them into
         * our aliases map. Then return the root alias of given Criteria.
         * 
         * @param criteria The Criteria to load aliases from.
         * @return Root alias of given Criteria.
         */
        private String loadAssociationAliases(Criteria criteria) {
            // we cannot pick up aliases in this loop because when you create 
            // subcriterias by createAlias() instead of createCriteria(), 
            // there are not nested!
            while (criteria instanceof Subcriteria) {
                criteria = ((Subcriteria) criteria).getParent();
            }
            CriteriaImpl rootCriteria = (CriteriaImpl) criteria;
            
            Iterator<Subcriteria> it = rootCriteria.iterateSubcriteria();
            while (it.hasNext()) {
                Subcriteria sub = it.next();
                LOG.trace("Found association aliase '{}' for path '{}'", 
                        sub.getAlias(), sub.getPath()); 
                aliases.put(sub.getPath(), sub.getAlias());
            }
            
            return rootCriteria.getAlias();
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
                return createCriterion((ComparisonExpression)expression);
            }

            throw new IllegalArgumentException("Unknown expression type: " + expression.getClass());
        }

        /**
         * Create Hibernate Criterion for given logical expression.
         * 
         * @param logical Logical expression
         * @return Criterion generated from given logical expression.
         * @throws RSQLException
         * @throws IllegalArgumentException If expression contains unsupported 
         *         operator.
         */
        private Criterion createCriterion(LogicalExpression logical) 
                throws RSQLException, IllegalArgumentException {

            switch (logical.getOperator()) {
                case AND : return Restrictions.and(createCriterion(logical.getLeft()), 
                                                   createCriterion(logical.getRight()));

                case OR : return Restrictions.or(createCriterion(logical.getLeft()), 
                                                 createCriterion(logical.getRight()));
            }

            throw new IllegalArgumentException("Unknown operator: " + logical.getOperator());
        }

        /**
         * Create Hibernate Criterion for given comparison expression (constraint).
         * 
         * It translates selector to property name or path via {@linkplain Mapper}
         * and then calls the <tt>delegateToBuilder()</tt> method.
         * 
         * {@link ArgumentFormatException} and {@link UnknownSelectorException}
         * are wrapped to {@link RSQLException}.
         * 
         * @param comparison Comparison expression (constraint).
         * @return Criterion generated from given comparison expression.
         * @throws RSQLException
         */
        private Criterion createCriterion(ComparisonExpression comparison) 
                throws RSQLException {
            
            String property = mapper.translate(comparison.getSelector(), entityClass);
            
            try {
                return delegateToBuilder(property, comparison.getOperator(), comparison.getArgument(), entityClass, rootAlias + '.');
                
            } catch (ArgumentFormatException ex) {
                throw new RSQLException(
                        new ArgumentFormatException(comparison.getSelector(), ex.getArgument(), ex.getPropertyType()));
            } catch (UnknownSelectorException ex) {
                throw new RSQLException(ex);
            }
        }
        
        @Override
        public Criterion delegateToBuilder(String property, Comparison operator, String argument, Class<?> entityClass, String alias) 
                throws ArgumentFormatException, UnknownSelectorException, IllegalArgumentException {

            for (AbstractCriterionBuilder builder : builders) {
                if (builder.accept(property, entityClass, this)) {
                    LOG.debug("Delegating comparison [{} {} {}] on entity {} to builder: {}", 
                            new Object[]{property, operator, argument, entityClass.getSimpleName(), builder.getClass().getSimpleName()});
                    
                    return builder.createCriterion(property, operator, argument, entityClass, alias, this);
                }
            }

            throw new IllegalArgumentException("No Criterion Builder found for property " + property + " of " + entityClass);
        }
        
        @Override
        public String createAssociationAlias(String associationPath) 
                throws AssociationsLimitException {
            
            return createAssociationAlias(associationPath, Criteria.INNER_JOIN);
        }
        
        @Override
        public String createAssociationAlias(String associationPath, int joinType) 
                throws AssociationsLimitException {
            
            // if already aliased
            if (aliases.containsKey(associationPath)) {
                String alias = aliases.get(associationPath);
                LOG.trace("Association alias for {} already exists: {}", associationPath, alias);
                
                return alias;
            }

            // check limit
            associations++;
            if (associationsLimit != -1 && associations > associationsLimit) {
                throw new AssociationsLimitException(associationsLimit);
            }
            
            // create new alias
            String alias = ALIAS_PREFIX + String.valueOf(associations);
            LOG.debug("Creating association alias (i.e. JOIN) for {}: {}", associationPath, alias);
            aliases.put(associationPath, alias);

            if (criteria instanceof DetachedCriteria) {
                ((DetachedCriteria) criteria).createAlias(associationPath, alias, joinType);
            } else {
                ((Criteria)criteria).createAlias(associationPath, alias, joinType);
            }

            return alias;
        }
        
        
        @Override
        public ArgumentParser getArgumentParser() {
            return argumentParser;
        }

        @Override
        public ClassMetadata getClassMetadata(Class<?> entityClass) {
            return sessionFactory.getClassMetadata(entityClass);
        }

        @Override
        public Mapper getMapper() {
            return mapper;
        }
        
        @Override
        public String getRootAlias() {
            return rootAlias;
        }
    
    }
    
}
