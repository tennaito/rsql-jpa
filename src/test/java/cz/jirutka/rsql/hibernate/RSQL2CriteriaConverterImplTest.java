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

import org.hibernate.impl.CriteriaImpl.Subcriteria;
import cz.jirutka.rsql.hibernate.RSQL2CriteriaConverterImpl.InnerBuilder;
import org.hibernate.Criteria;
import cz.jirutka.rsql.hibernate.entity.Course;
import cz.jirutka.rsql.parser.model.Comparison;
import cz.jirutka.rsql.parser.model.ComparisonExpression;
import cz.jirutka.rsql.parser.model.Expression;
import cz.jirutka.rsql.parser.model.Logical;
import cz.jirutka.rsql.parser.model.LogicalExpression;
import java.util.Iterator;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.CriteriaImpl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RSQL2CriteriaConverterImplTest {
    
    private SessionFactory sessionFactory;
    private Mapper mapper;
    private RSQL2CriteriaConverterImpl instance;
    private InnerBuilder inner;

    
    @Before
    public void setUp() throws Exception {
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
        mapper = new Mapper() {
            @Override
            public String translate(String selector, Class<?> entityClass) {
                return selector;
            }
        };
        instance = new MockRSQL2CriteriaConverterImpl(sessionFactory);
        instance.setMapper(mapper);
        instance.pushCriterionBuilder(new MockCriterionBuilder());
        inner = ((MockRSQL2CriteriaConverterImpl)instance).createInnerBuilder(Course.class);
    }
    
    
    ////////////////////////// Tests //////////////////////////
       
    /**
     * Test if single comparison expression is routed to Criterion Builder well.
     */
    @Test
    public void testInnerConvertDetached0() {        
        instance.setMapper(mapper);
        instance.pushCriterionBuilder(new MockCriterionBuilder() {
            @Override
            public Criterion createCriterion(String property, Comparison operator, String argument, 
                    Class<?> entityClass, String alias, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                assertEquals("foo", property);
                assertEquals(operator, Comparison.EQUAL);
                assertEquals("bar", argument);
                assertEquals(Course.class, entityClass);
                assertEquals("this.", alias);
                assertNotNull(parent);
                
                throw new AssertionError("This should be thrown!");
            }
            
        });
        
        Expression expression = new ComparisonExpression("foo", Comparison.EQUAL, "bar");
        try {
            inner.convert(expression, DetachedCriteria.forClass(Course.class));
            fail("Method createCriterion() was not called!");
        } catch (AssertionError ex) { /*OK*/ }
    }
    
    /**
     * Test if composite expression is routed to Criterion Builder well.
     */
    @Test
    public void testInnerConvertDetached1() {       
        instance.setMapper(mapper);
        instance.pushCriterionBuilder(new MockCriterionBuilder() {
            int call = 0;

            @Override
            public Criterion createCriterion(String property, Comparison operator, String argument, 
                    Class<?> entityClass, String alias, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                if (call == 0) {
                    assertEquals("sel0", property);
                    assertEquals(operator, Comparison.EQUAL);
                    assertEquals("foo", argument);
                } else if (call == 1) {
                    assertEquals("sel1", property);
                    assertEquals(operator, Comparison.NOT_EQUAL);
                    assertEquals("bar", argument);
                } else if (call == 2) {
                    assertEquals("sel2", property);
                    assertEquals(operator, Comparison.LESS_THAN);
                    assertEquals("baz", argument);
                    
                    throw new AssertionError("This should be thrown!");
                }
                assertNotNull(parent);
                call++;
                return Restrictions.eq("null", "null");
            }
        });
        
        Expression expression = new LogicalExpression(
                new LogicalExpression(
                    new ComparisonExpression("sel0", Comparison.EQUAL, "foo"),
                    Logical.OR,
                    new ComparisonExpression("sel1", Comparison.NOT_EQUAL, "bar")),
                Logical.AND,
                new ComparisonExpression("sel2", Comparison.LESS_THAN, "baz"));
        
        try {
            inner.convert(expression, DetachedCriteria.forClass(Course.class));
            fail("Method createCriterion() was not called for third time!");
        } catch (AssertionError ex) { /*OK*/ }
    }

    /**
     * Finally test whole DetachedCriteria creation.
     */
    @Test
    public void testInnerConvertDetached2() {
        instance.pushCriterionBuilder(new MockCriterionBuilder());
        
        DetachedCriteria expResult = DetachedCriteria
                .forClass(Course.class, RSQL2CriteriaConverter.ROOT_ALIAS)
                .add(Restrictions.and(
                    Restrictions.eq("foo", "flynn"),
                    Restrictions.or(
                        Restrictions.eq("bar", 42),
                        Restrictions.eq("baz", 42.2))));
        
        Expression expression = new LogicalExpression(
                new ComparisonExpression("foo", Comparison.EQUAL, "flynn"),
                Logical.AND,
                new LogicalExpression(
                    new ComparisonExpression("bar", Comparison.EQUAL, "42"),
                    Logical.OR,
                    new ComparisonExpression("baz", Comparison.EQUAL, "42.2")));
        
        DetachedCriteria result = DetachedCriteria.forClass(Course.class);
        inner.convert(expression, result);
        
        assertEquals(expResult.toString(), result.toString());
    }
    
    
    @Test
    public void testCreateCriteria2arg() {
        DetachedCriteria expResult;
        expResult = DetachedCriteria.forClass(Course.class, RSQL2CriteriaConverter.ROOT_ALIAS)
                        .add(Restrictions.eq("foo", "bar"));
        
        DetachedCriteria result = instance.createCriteria("foo==bar", Course.class);
        assertEquals("Expression: foo==bar", expResult.toString(), result.toString());
        
        try {
            instance.createCriteria("invalid input ", Course.class);
            fail("Should raise RSQLException");
        } catch (RSQLException ex) { /* OK */ }
    }
    
    
    @Test
    public void testPushCriterionBuilder() {
        MockRSQL2CriteriaConverterImpl converter = new MockRSQL2CriteriaConverterImpl(sessionFactory);
        converter.setMapper(mapper);
        
        converter.pushCriterionBuilder(new MockCriterionBuilder() {
            @Override
            public Criterion createCriterion(String property, Comparison operator, String argument, 
                    Class<?> entityClass, String alias, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                fail("Should be at the bottom of builders stack");
                return null;
            }
        });
        
        converter.pushCriterionBuilder(new MockCriterionBuilder() {
            @Override
            public Criterion createCriterion(String property, Comparison operator, String argument, 
                    Class<?> entityClass, String alias, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                // ok
                return Restrictions.ne("foo", "notbar");
            }
        });
        
        converter.pushCriterionBuilder(new MockCriterionBuilder() {
            @Override
            public boolean accept(String selector, Class<?> entityClass, CriteriaBuilder parent) {
                return false;
            }
            @Override
            public Criterion createCriterion(String property, Comparison operator, String argument, 
                    Class<?> entityClass, String alias, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                fail("Should not be accepted");
                return null;
            }
        });
        
        Expression expression = new ComparisonExpression("foo", Comparison.EQUAL, "bar");
        
        DetachedCriteria expResult = DetachedCriteria.forClass(Course.class)
                .add(Restrictions.ne("foo", "notbar"));
        
        DetachedCriteria result = DetachedCriteria.forClass(Course.class);
        converter.createInnerBuilder(Course.class).convert(expression, result);
        
        assertEquals(expResult.toString(), result.toString());
    }
        
    
    @Test
    public void testCreateAssociationAlias() {
        final String aliasPrefix = RSQL2CriteriaConverter.ALIAS_PREFIX;
        
        instance.setAssociationsLimit(3);
        
        instance.pushCriterionBuilder(new MockCriterionBuilder() {
            @Override
            public Criterion createCriterion(String property, Comparison operator, String argument, 
                    Class<?> entityClass, String alias, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                assertEquals(aliasPrefix + "1", parent.createAssociationAlias("foo", Criteria.INNER_JOIN));
                assertEquals(aliasPrefix + "2", parent.createAssociationAlias("bar", Criteria.LEFT_JOIN));
                assertEquals(aliasPrefix + "1", parent.createAssociationAlias("foo", Criteria.LEFT_JOIN));
                assertEquals(aliasPrefix + "3", parent.createAssociationAlias("baz", Criteria.FULL_JOIN));
                
                try {
                    parent.createAssociationAlias("qux");
                    fail("Should raise JoinsLimitException");
                } catch (AssociationsLimitException ex) { /*OK*/ }
                
                throw new AssertionError("This should be thrown!");
            }
        });
        
        Criteria criteria = sessionFactory.openSession().createCriteria(Course.class);
        Expression expression = new ComparisonExpression("foo", Comparison.EQUAL, "bar");
        
        try {
            inner.convert(expression, criteria);
            fail("Method createCriterion() was not called!");
        } catch (AssertionError ex) { /*OK*/ }
        
        int i = 0;
        Iterator<Subcriteria> it = ((CriteriaImpl) criteria).iterateSubcriteria();
        while (it.hasNext()) {
            Subcriteria sub = it.next();
            switch (i) {
                case 0 : { 
                    assertEquals("foo", sub.getPath());
                    assertEquals("alias1", sub.getAlias());
                    assertEquals(Criteria.INNER_JOIN, sub.getJoinType());
                    break;
                }
                case 1 : {
                    assertEquals("bar", sub.getPath());
                    assertEquals("alias2", sub.getAlias());
                    assertEquals(Criteria.LEFT_JOIN, sub.getJoinType());
                    break;
                }
                case 2 : {
                    assertEquals("baz", sub.getPath());
                    assertEquals("alias3", sub.getAlias());
                    assertEquals(Criteria.FULL_JOIN, sub.getJoinType());
                    break;
                }
                default : fail("Should not be here!");
            }
            i++;
        }
        assertEquals("Should iterate over three subcriterias", 3, i);
        
    }
    
    
    @Test
    public void testLoadAssociationAliases() {
        Expression expression = new ComparisonExpression("foo", Comparison.EQUAL, "bar");
        
        instance.pushCriterionBuilder(new MockCriterionBuilder() {
            @Override
            public Criterion createCriterion(String property, Comparison operator, String argument, 
                    Class<?> entityClass, String alias, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                assertEquals("givenAlias1", parent.createAssociationAlias("foo"));
                assertEquals("givenAlias3", parent.createAssociationAlias("baz"));
                assertEquals("givenAlias2", parent.createAssociationAlias("bar"));
                
                throw new AssertionError("This should be thrown!");
            }
        });
        
        
        Criteria criteria1 = sessionFactory.openSession()
                .createCriteria(Course.class)
                    .createAlias("foo", "givenAlias1")
                    .createAlias("bar", "givenAlias2")
                    .createAlias("baz", "givenAlias3");
        try {
            inner.convert(expression, criteria1);
            fail("Method createCriterion() was not called!");
        } catch (AssertionError ex) { /*OK*/ }
        
        
        Criteria criteria2 = sessionFactory.openSession()
                .createCriteria(Course.class)
                    .createCriteria("foo", "givenAlias1")
                        .createCriteria("bar", "givenAlias2")
                            .createCriteria("baz", "givenAlias3");
        try {
            inner.convert(expression, criteria2);
            fail("Method createCriterion() was not called!");
        } catch (AssertionError ex) { /*OK*/ }
    }
    
    
    
    
    ////////////////////////// Mocks //////////////////////////
    
    private static class MockRSQL2CriteriaConverterImpl extends RSQL2CriteriaConverterImpl {
        
        public MockRSQL2CriteriaConverterImpl(SessionFactory sessionFactory) {
            super(sessionFactory);
        }
        
        public InnerBuilder createInnerBuilder(Class<?> entityClass) {
            return new InnerBuilder(entityClass);
        }
        
    }
    
    private static class MockCriterionBuilder extends AbstractCriterionBuilder {

        @Override
        public boolean accept(String property, Class<?> entityClass, CriteriaBuilder parent) {
            return true;
        }

        @Override
        public Criterion createCriterion(String property, Comparison operator, String argument, 
                Class<?> entityClass, String alias, CriteriaBuilder parent) 
                throws ArgumentFormatException, UnknownSelectorException {
            return Restrictions.eq(property, argument);
        }  
    }
    
}
