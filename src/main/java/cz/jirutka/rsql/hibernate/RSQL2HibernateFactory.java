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
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;

/**
 * Factory for creating {@link RSQL2CriteriaConverter} instances. This may be used
 * in application's context for obtaining preconfigured Criteria Converters.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RSQL2HibernateFactory {
    
    private static RSQL2HibernateFactory instance;
    
    private SessionFactory sessionFactory;
    private ArgumentParser argumentParser = new DefaultArgumentParser();  //default
    private List<AbstractCriterionBuilder> criterionBuilders = new ArrayList<AbstractCriterionBuilder>(4);
    private int associationsLimit = -1;  // default
    private Mapper mapper = new SimpleMapper();  // default


    /**
     * Factory method for obtaining singleton instance of 
     * <tt>RSQL2HibernateFactory</tt>.
     * 
     * @return Singleton instance of <tt>RSQL2HibernateFactory</tt>.
     */
    public static RSQL2HibernateFactory getInstance() {
        if (instance == null) {
            instance = new RSQL2HibernateFactory();
        }
        return instance;
    }
    
    private RSQL2HibernateFactory() {
        criterionBuilders.add(new AssociationsCriterionBuilder());
        criterionBuilders.add(new NaturalIdCriterionBuilder());
        criterionBuilders.add(new IdentifierCriterionBuilder());
        criterionBuilders.add(new DefaultCriterionBuilder());
    }
    
    
    /**
     * Create new {@link RSQL2CriteriaConverter} that can parse a RSQL expressions 
     * and convert it to Hibernate {@link Criteria} or {@link DetachedCriteria}.
     * 
     * @return RSQL2CriteriaConverter
     */
    public RSQL2CriteriaConverter createConverter() {
        RSQL2CriteriaConverterImpl converter = new RSQL2CriteriaConverterImpl(sessionFactory);
        
        // copy defaults
        converter.setArgumentParser(argumentParser);
        converter.setAssociationsLimit(associationsLimit);
        converter.setMapper(mapper);
        converter.getCriterionBuilders().addAll(criterionBuilders);
        
        return converter;
    }

    
    /**
     * Set default Argument Parser. If you don't set any, the 
     * {@link DefaultArgumentParser} will be used.
     * 
     * @see RSQL2CriteriaConverter#getArgumentParser()
     * @param argumentParser An <tt>ArgumentParser</tt> instance, 
     *        must not be <tt>null</tt>.
     */
    public void setArgumentParser(ArgumentParser argumentParser) {
        this.argumentParser = argumentParser;
    }

    /**
     * Set default stack of {@linkplain AbstractCriterionBuilder Criterion 
     * Builders} that will be copied to all new RSQL2CriteriaConverter instances. 
     * 
     * If you dont't set any, these will by used: <ul>
     * <li>{@link AssociationsCriterionBuilder} - Handles association "dereference". 
     * That means you can specify constraints upon related entities by navigating 
     * associations using dot-notation.</li>
     * <li>{@link NaturalIdCriterionBuilder} - Creates Criterion for a property 
     * representing an association, and an argument containing NaturalID of the 
     * associated entity.</li>
     * <li>{@link IdentifierCriterionBuilder} - Creates Criterion for a property 
     * representing an association, and an argument containing ID of the 
     * associated entity.</li>
     * <li>{@link DefaultCriterionBuilder} - Default implementation, simply 
     * creates Criterion for a basic property (not association).</li>
     * </ul>
     * 
     * @param criterionBuilders List of Criterion Builders to use as default in 
     *        new instances of Criteria Builder.
     */
    public void setCriterionBuilders(List<AbstractCriterionBuilder> criterionBuilders) {
        assert !criterionBuilders.isEmpty();
        this.criterionBuilders = criterionBuilders;
    }
    
    /**
     * Set default associations limit. Default value is -1 (e.g. none limit).
     * 
     * @see RSQL2CriteriaConverter#getAssociationsLimit() 
     * @param limit Upper limit of associations that can be handled. Must be 
     *        greater or equal -1.
     */
    public void setAssociationsLimit(int limit) {
        assert limit >= -1 : "must be greater or equal -1";
        this.associationsLimit = limit;
    }

    /**
     * Set default <tt>Mapper</tt> for new instances. If you don't set any, 
     * the {@link SimpleMapper} will be used.
     * 
     * @see RSQL2CriteriaConverter#getMapper() 
     * @param mapping A <tt>Mapper</tt> instance, must not be <tt>null</tt>.
     */
    public void setMapper(Mapper mapping) {
        this.mapper = mapping;
    }

    /**
     * Set Hibernate <tt>SessionFactory</tt> that will be used to obtain 
     * <tt>ClassMetadata</tt>.
     * 
     * <b>This si required and must not be null!</b>
     * 
     * @param sessionFactory Hibernate <tt>SessionFactory</tt>, must not be 
     *        <tt>null.</tt>.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
}
