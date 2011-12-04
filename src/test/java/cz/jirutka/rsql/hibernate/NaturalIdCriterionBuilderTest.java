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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.HibernateException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class NaturalIdCriterionBuilderTest extends AbstractCriterionBuilderTest {

    @Before
    public void setUp() throws Exception {
        instance = new NaturalIdCriterionBuilder();
        entityClass = Course.class;
        parent = new MockInnerBuilder(Course.class);
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
    }
    

    @Test
    public void testAccept() {
        CriteriaBuilder parentDep = new MockInnerBuilder(Department.class);

        assertTrue(instance.accept("department", entityClass, parent));
        assertFalse(instance.accept("head", entityClass, parentDep));
        assertFalse(instance.accept("name", entityClass, parent));
        assertFalse(instance.accept("id", entityClass, parent));
        assertFalse(instance.accept("invalid", entityClass, parent));
    }

    @Test
    public void testCreateCriterion() throws Exception {
        Criterion expResult;
        Criterion result;
        
        CriteriaBuilder parent1 = new MockInnerBuilder(Course.class) {
            @Override
            public String createAssociationAlias(String property) throws AssociationsLimitException {
                assertEquals("that.department", property);
                return "alias1";
            }
        };
        expResult = Restrictions.eq("alias1.code", "18102");
        result = instance.createCriterion("department", Comparison.EQUAL, "18102", entityClass, "that.", parent1);
        assertEquals(expResult.toString(), result.toString());

        try {
            result = instance.createCriterion("department", Comparison.EQUAL, "non-numeric", entityClass, "that.", parent1);
            fail("Should raise an ArgumentFormatException");
        } catch (ArgumentFormatException ex) { /*OK*/ }
    }
    
    @Test
    public void testHasNaturalIdentifier() throws HibernateException {
        NaturalIdCriterionBuilder instance = (NaturalIdCriterionBuilder) this.instance;
        CriteriaBuilder parentDep = new MockInnerBuilder(Department.class);

        assertTrue(instance.hasNaturalIdentifier("department", entityClass, parent));
        assertFalse(instance.hasNaturalIdentifier("head", Department.class, parentDep));
        try {
            assertFalse(instance.hasNaturalIdentifier("name", entityClass, parent));
            assertFalse(instance.hasNaturalIdentifier("id", entityClass, parent));
            assertFalse(instance.hasNaturalIdentifier("invalid", entityClass, parent));
        } catch (Exception ex) { /*OK*/ }
    }
}
