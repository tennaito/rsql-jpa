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

import org.hibernate.Criteria;
import cz.jirutka.rsql.hibernate.entity.Course;
import cz.jirutka.rsql.parser.model.Comparison;
import cz.jirutka.rsql.parser.model.ComparisonExpression;
import cz.jirutka.rsql.parser.model.Expression;
import cz.jirutka.rsql.parser.model.Logical;
import cz.jirutka.rsql.parser.model.LogicalExpression;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RSQLCriteriaBuilderImplTest {
    
    private SessionFactory sessionFactory;
    private Mapper mapper;
    private RSQLCriteriaBuilderImpl instance;

    
    @Before
    public void setUp() throws Exception {
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
        mapper = new Mapper() {
            public String translate(String selector, Class<?> entityClass) {
                return selector;
            }
        };
        instance = new RSQLCriteriaBuilderImpl(Course.class, sessionFactory);
        instance.setMapper(mapper);
        instance.pushCriterionBuilder(new MockCriterionBuilder());
    }
    
    
    ////////////////////////// Tests //////////////////////////
       
    /**
     * Test if single comparison expression is routed to Criterion Builder well.
     */
    @Test
    public void testCreateCriteria0() {
        RSQLCriteriaBuilderImpl builder = new RSQLCriteriaBuilderImpl(Course.class, sessionFactory);
        
        builder.setMapper(mapper);
        builder.pushCriterionBuilder(new MockCriterionBuilder() {
            public Criterion createCriterion(String property, Comparison operator, String argument, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                assertEquals("foo", property);
                assertEquals(operator, Comparison.EQUAL);
                assertEquals("bar", argument);
                assertNotNull(parent);
                return Restrictions.eq("foo", "bar");
            }
        });
        
        Expression expression = new ComparisonExpression("foo", Comparison.EQUAL, "bar");
        builder.createCriteria(expression);
    }
    
    /**
     * Test if composite expression is routed to Criterion Builder well.
     */
    @Test
    public void testCreateCriteria1() {
        RSQLCriteriaBuilderImpl builder = new RSQLCriteriaBuilderImpl(Course.class, sessionFactory);
        
        builder.setMapper(mapper);
        builder.pushCriterionBuilder(new MockCriterionBuilder() {
            int call = 0;

            public Criterion createCriterion(String property, Comparison operator, String argument, CriteriaBuilder parent) 
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
        builder.createCriteria(expression);
    }

    /**
     * Finally test whole Criteria creation.
     */
    @Test
    public void testCreateCriteria2() {
        DetachedCriteria expResult = DetachedCriteria.forClass(Course.class, RSQLCriteriaBuilder.ROOT_ALIAS)
                        .add(Restrictions.eq("foo", "bar"));
        
        Expression expression = new ComparisonExpression("foo", Comparison.EQUAL, "bar");
        DetachedCriteria result = instance.createCriteria(expression);
        
        assertEquals(expResult.toString(), result.toString());
    }
    
    
    @Test
    public void testParse1arg() {
        DetachedCriteria expResult;
        expResult = DetachedCriteria.forClass(Course.class, RSQLCriteriaBuilder.ROOT_ALIAS)
                        .add(Restrictions.eq("foo", "bar"));
        
        DetachedCriteria result = instance.parse("foo==bar");
        assertEquals("Expression: foo==bar", expResult.toString(), result.toString());
        
        try {
            instance.parse("invalid input ");
            fail("Should raise RSQLException");
        } catch (RSQLException ex) { /* OK */ }
    }
    
    
    @Test
    public void testPushCriterionBuilder() {
        RSQLCriteriaBuilderImpl builder = new RSQLCriteriaBuilderImpl(Course.class, sessionFactory);
        builder.setMapper(mapper);
        
        builder.pushCriterionBuilder(new MockCriterionBuilder() {
            public Criterion createCriterion(String property, Comparison operator, String argument, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                fail("Should be at the bottom of builders stack");
                return null;
            }
        });
        
        builder.pushCriterionBuilder(new MockCriterionBuilder() {
            public Criterion createCriterion(String property, Comparison operator, String argument, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                // ok
                return Restrictions.eq("null", "null");
            }
        });
        
        builder.pushCriterionBuilder(new MockCriterionBuilder() {
            public boolean canAccept(String selector, Comparison operator, CriteriaBuilder parent) {
                return false;
            }
            public Criterion createCriterion(String property, Comparison operator, String argument, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                fail("Should not be accepted");
                return null;
            }
        });
        
        Expression expression = new ComparisonExpression("foo", Comparison.EQUAL, "bar");
        builder.createCriteria(expression);
    }
    
    
    @Test
    public void testAddJoin() {
        final String aliasPrefix = RSQLCriteriaBuilder.ALIAS_PREFIX;
        
        RSQLCriteriaBuilderImpl builder = new RSQLCriteriaBuilderImpl(Course.class, sessionFactory);
        builder.setMapper(mapper);
        builder.setJoinsLimit(3);
        
        builder.pushCriterionBuilder(new MockCriterionBuilder() {
            
            public Criterion createCriterion(String property, Comparison operator, String argument, CriteriaBuilder parent) 
                    throws ArgumentFormatException, UnknownSelectorException {
                assertEquals(aliasPrefix + "1", parent.addJoin("foo"));
                assertEquals(aliasPrefix + "2", parent.addJoin("bar"));
                assertEquals(aliasPrefix + "1", parent.addJoin("foo"));
                assertEquals(aliasPrefix + "3", parent.addJoin("baz"));
                
                try {
                    parent.addJoin("qux");
                    fail("Should raise JoinsLimitException");
                } catch (JoinsLimitException ex) { /*OK*/ }
                
                return super.createCriterion(property, operator, argument, parent);
            }
            
        });
        
        DetachedCriteria expResult;
        expResult = DetachedCriteria.forClass(Course.class, RSQLCriteriaBuilder.ROOT_ALIAS);
        expResult.createCriteria("foo", aliasPrefix + "1", Criteria.LEFT_JOIN);
        expResult.createCriteria("bar", aliasPrefix + "2", Criteria.LEFT_JOIN);
        expResult.createCriteria("baz", aliasPrefix + "3", Criteria.LEFT_JOIN);
        expResult.add(Restrictions.eq("foo", "bar"));
        
        DetachedCriteria result = builder.createCriteria(
                new ComparisonExpression("foo", Comparison.EQUAL, "bar"));
        
        assertEquals(expResult.toString(), result.toString());
        
    }
    
    
    ////////////////////////// Mocks //////////////////////////
    
    private static class MockCriterionBuilder extends AbstractCriterionBuilder {

        @Override
        public boolean canAccept(String selector, Comparison operator, CriteriaBuilder parent) {
            return true;
        }

        @Override
        public Criterion createCriterion(String property, Comparison operator, String argument, CriteriaBuilder parent) 
                throws ArgumentFormatException, UnknownSelectorException {
            return Restrictions.eq(property, argument);
        }   
    }
    
}
