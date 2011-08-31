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

import cz.jirutka.rsql.hibernate.entity.Department;
import cz.jirutka.rsql.hibernate.entity.Course;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import java.util.Date;
import cz.jirutka.rsql.parser.model.Comparison;
import java.util.GregorianCalendar;
import org.hibernate.criterion.Criterion;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public abstract class AbstractCriterionBuilderTest {
    
    protected AbstractCriterionBuilder instance;
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
    public void testParseArgument() throws Exception {
        String argument;
        Object expected;
        Object actual;
        
        argument = "string";
        expected = "string";
        actual = instance.parseArgument(argument, String.class);
        assertEquals(expected, actual);
        
        argument = "123456";
        expected = 123456;
        actual = instance.parseArgument(argument, Integer.class);
        assertEquals(expected, actual);
        
        argument = "true";
        expected = true;
        actual = instance.parseArgument(argument, Boolean.class);
        assertEquals(expected, actual);
        
        argument = "FOO";
        expected = MockEnum.FOO;
        actual = instance.parseArgument(argument, MockEnum.class);
        assertEquals(expected, actual);
        
        argument = "2011-08-26";
        expected = new GregorianCalendar(2011, 7, 26).getTime();
        actual = instance.parseArgument(argument, Date.class);
        assertEquals(expected, actual);
        
        argument = "2011-08-26T14:15:30";
        expected = new GregorianCalendar(2011, 7, 26, 14, 15, 30).getTime();
        actual = instance.parseArgument(argument, Date.class);
        assertEquals(expected, actual);
        
        argument = "foo";
        expected = new MockValueOfType();
        actual = instance.parseArgument(argument, MockValueOfType.class);
        assertTrue(actual instanceof MockValueOfType);
        
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

    protected class MockCriterionBuilder implements CriteriaBuilder {
        private Class<?> entityClass;

        public MockCriterionBuilder(Class<?> entityClass) {
            this.entityClass = entityClass;
        }

        private final Mapper mapper = new Mapper() {
            public String translate(String selector, Class<?> entityClass) {
                return selector;
            }
        };
        
        public String addJoin(String property) throws JoinsLimitException {
            return "";
        }

        public ClassMetadata getClassMetadata() {
            return sessionFactory.getClassMetadata(entityClass);
        }

        public ClassMetadata getClassMetadata(Class<?> entityClass) {
            return sessionFactory.getClassMetadata(entityClass);
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }

        public Mapper getMapper() {
            return mapper;
        }
    }
    
    protected enum MockEnum {
        FOO, BAR;
    }
    
    protected static class MockValueOfType {
        
        public static MockValueOfType valueOf(String s) {
            return new MockValueOfType();
        }
    }
    
}
