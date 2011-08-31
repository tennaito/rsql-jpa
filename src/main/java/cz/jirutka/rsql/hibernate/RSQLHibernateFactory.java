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

import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;

/**
 * Factory for creating {@link RSQLCriteriaBuilder} instances. This may be used
 * in application's context for obtaining preconfigured Criteria Builders.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RSQLHibernateFactory {
    
    private static RSQLHibernateFactory singleton;
    
    private SessionFactory sessionFactory;
    private List<AbstractCriterionBuilder> criterionBuilders = new ArrayList<AbstractCriterionBuilder>(1);
    private int joinsLimit = 3; //default
    private Mapper mapper = new SimpleMapper(); //default


    /**
     * Factory method for obtaining singleton instance of 
     * <tt>RSQLHibernateFactory</tt>.
     * 
     * @return Singleton instance of <tt>RSQLHibernateFactory</tt>.
     */
    public static RSQLHibernateFactory getInstance() {
        if (singleton == null) {
            singleton = new RSQLHibernateFactory();
        }
        return singleton;
    }
    
    /**
     * Default non-parametric constructor. Factory method should be used 
     * instead, however if you really want multiple instances, you can use
     * this.
     */
    public RSQLHibernateFactory() {
        criterionBuilders.add(new DefaultCriterionBuilder());
    }
    
    
    /**
     * Create new {@link RSQLCriteriaBuilder} for given entity class that can 
     * parse a RSQL expression and build appropriate Hibernate 
     * {@link DetachedCriteria}.
     * 
     * @param entityClass Entity class for which will be Criteria created.
     * @return A RSQLCriteriaBuilder object that may be used to build 
     *         <tt>DetachedCriteria</tt> from a RSQL expression.
     */
    public RSQLCriteriaBuilder createBuilder(Class<?> entityClass) {
        RSQLCriteriaBuilderImpl instance = new RSQLCriteriaBuilderImpl(entityClass, sessionFactory);
        
        instance.setJoinsLimit(joinsLimit);
        instance.setMapper(mapper);
        for (AbstractCriterionBuilder builder : criterionBuilders) {
            instance.pushCriterionBuilder(builder);
        }
        
        return instance;
    }

    /**
     * @see RSQLHibernateFactory#setCriterionBuilders(java.util.List)
     * @return Default list of Criterion Builders used in new instances of
     *         Criteria Builder.
     */
    public List<AbstractCriterionBuilder> getCriterionBuilders() {
        return criterionBuilders;
    }

    /**
     * Set default stack of {@linkplain AbstractCriterionBuilder Criterion 
     * Builders} that will be copied to all new {@linkplain RSQLCriteriaBuilder 
     * Criteria Builder} instances. If you didn't set any, the 
     * {@link DefaultCriterionBuilder} will be used.
     * 
     * @param criterionBuilders List of Criterion Builders to use as default in 
     *        new instances of Criteria Builder.
     */
    public void setCriterionBuilders(List<AbstractCriterionBuilder> criterionBuilders) {
        assert !criterionBuilders.isEmpty();
        this.criterionBuilders = criterionBuilders;
    }

    /**
     * @see RSQLHibernateFactory#setJoinsLimit(int)
     * @return Default upper limit of JOINs that can be generated.
     */
    public int getJoinsLimit() {
        return joinsLimit;
    }
    
    /**
     * JOINs are quite expensive operations so you should limit number of JOINs
     * that can be generated per one query.
     * 
     * @param joinsLimit Default upper limit of JOINs that can be generated. 
     *        Must be greater or equal 0.
     */
    public void setJoinsLimit(int joinsLimit) {
        assert joinsLimit >= 0 : "must be greater or equal 0";
        this.joinsLimit = joinsLimit;
    }
    
    /**
     * @see RSQLHibernateFactory#setMapper(Mapper)
     * @return The current <tt>Mapper</tt> instance.
     */
    public Mapper getMapper() {
        return mapper;
    }

    /**
     * Set default <tt>Mapper</tt> that will be used in new {@linkplain 
     * RSQLCriteriaBuilder Criteria Builder} instances.
     * 
     * @param mapping A <tt>Mapper</tt> instance, must not be <tt>null</tt>.
     */
    public void setMapper(Mapper mapping) {
        this.mapper = mapping;
    }

    /**
     * @see RSQLHibernateFactory#setSessionFactory(SessionFactory) 
     * @return The current Hibernate <tt>SessionFactory</tt>.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Set Hibernate <tt>SessionFactory</tt> that will used to obtain 
     * <tt>ClassMetadata</tt>.
     * 
     * <b>This si required and must not be null!</b>
     * 
     * @param sessionFactory Hibernate <tt>SessionFactory</tt>, must not be 
     *                       <tt>null.</tt>.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
}
