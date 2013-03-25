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
