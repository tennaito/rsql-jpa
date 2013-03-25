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

import cz.jirutka.rsql.hibernate.entity.Department;
import cz.jirutka.rsql.hibernate.entity.Course;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.criterion.Criterion;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public abstract class AbstractCriterionBuilderTest {
    
    protected AbstractCriterionBuilder instance;
    protected Class<?> entityClass;
    protected CriteriaBuilder parent;
    protected SessionFactory sessionFactory;
    
    
    
    ////////////////////////// Tests //////////////////////////
    
    @Test
    public void testCreateCriterion3args() {
        String property = "foo";
        Criterion exptected;
        Criterion actual;
        
        exptected = Restrictions.eq(property, "bar");
        actual = instance.createCriterion(property, Comparison.EQUAL, "bar");
        assertEquals(exptected.toString(), actual.toString());
        
        exptected = Restrictions.ilike(property, "bar%");
        actual = instance.createCriterion(property, Comparison.EQUAL, "bar*");
        assertEquals(exptected.toString(), actual.toString());
        
        exptected = Restrictions.ne(property, "bar");
        actual = instance.createCriterion(property, Comparison.NOT_EQUAL, "bar");
        assertEquals(exptected.toString(), actual.toString());
        
        exptected = Restrictions.not(Restrictions.ilike(property, "%bar"));
        actual = instance.createCriterion(property, Comparison.NOT_EQUAL, "*bar");
        assertEquals(exptected.toString(), actual.toString());
        
        exptected = Restrictions.gt(property, 42);
        actual = instance.createCriterion(property, Comparison.GREATER_THAN, 42);
        assertEquals(exptected.toString(), actual.toString());
        
        exptected = Restrictions.ge(property, -42);
        actual = instance.createCriterion(property, Comparison.GREATER_EQUAL, -42);
        assertEquals(exptected.toString(), actual.toString());
        
        exptected = Restrictions.lt(property, 42.2);
        actual = instance.createCriterion(property, Comparison.LESS_THAN, 42.2);
        assertEquals(exptected.toString(), actual.toString());
        
        exptected = Restrictions.le(property, -42.2);
        actual = instance.createCriterion(property, Comparison.LESS_EQUAL, -42.2);
        assertEquals(exptected.toString(), actual.toString());

    }
    
    @Test
    public void testIsPropertyName() {
        SessionFactory sf = SessionFactoryInitializer.getSessionFactory();
        ClassMetadata classMetadata = sf.getClassMetadata(Course.class);
        
        assertTrue(instance.isPropertyName("code", classMetadata));
        assertTrue(instance.isPropertyName("department", classMetadata));
        assertFalse(instance.isPropertyName("foo", classMetadata));
    }
    
    @Test
    public void testFindPropertyType() {
        SessionFactory sf = SessionFactoryInitializer.getSessionFactory();
        ClassMetadata classMetadata = sf.getClassMetadata(Course.class);
        
        assertSame(String.class, instance.findPropertyType("code", classMetadata));
        assertSame(Boolean.class, instance.findPropertyType("active", classMetadata));
        assertSame(Department.class, instance.findPropertyType("department", classMetadata));
        assertNotSame(Integer.class, instance.findPropertyType("code", classMetadata));
    }
    
    
    
    ////////////////////////// Mocks //////////////////////////

    protected class MockInnerBuilder implements CriteriaBuilder {
        private Class<?> entityClass;
        private ArgumentParser argumentParser = new DefaultArgumentParser();

        public MockInnerBuilder(Class<?> entityClass) {
            this.entityClass = entityClass;
        }

        private final Mapper mapper = new Mapper() {
            @Override
            public String translate(String selector, Class<?> entityClass) {
                return selector;
            }
        };
        
        @Override
        public String createAssociationAlias(String associationPath) throws AssociationsLimitException {
            return "this";
        }

        @Override
        public String createAssociationAlias(String associationPath, int joinType) throws AssociationsLimitException {
            return "this";
        }
        
        @Override
        public Criterion delegateToBuilder(String property, Comparison operator, String argument, Class<?> entityClass, String alias) 
                throws ArgumentFormatException, UnknownSelectorException, IllegalStateException {
            return new DefaultCriterionBuilder().createCriterion(property, operator, argument, entityClass, alias, parent);
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
            return "this.";
        }

    }
    
}
