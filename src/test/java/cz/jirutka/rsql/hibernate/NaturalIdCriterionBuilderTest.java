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
        parent = new MockCriterionBuilder(Course.class);
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
    }
    

    @Test
    public void testAccept() {
        CriteriaBuilder parentDep = new MockCriterionBuilder(Department.class);

        assertTrue(instance.canAccept("department", Comparison.EQUAL, parent));
        assertFalse(instance.canAccept("head", Comparison.EQUAL, parentDep));
        assertFalse(instance.canAccept("name", Comparison.EQUAL, parent));
        assertFalse(instance.canAccept("id", Comparison.EQUAL, parent));
        assertFalse(instance.canAccept("invalid", Comparison.EQUAL, parent));
    }

    @Test
    public void testCreateCriterion() throws Exception {
        Criterion expResult;
        Criterion result;
        
        CriteriaBuilder parent1 = new MockCriterionBuilder(Course.class) {
            public String addJoin(String property) throws JoinsLimitException {
                assertEquals("department", property);
                return "alias1";
            }
        };
        expResult = Restrictions.eq("alias1.code", "18102");
        result = instance.createCriterion("department", Comparison.EQUAL, "18102", parent1);
        assertEquals(expResult.toString(), result.toString());

        try {
            result = instance.createCriterion("department", Comparison.EQUAL, "non-numeric", parent1);
            fail("Should raise an ArgumentFormatException");
        } catch (ArgumentFormatException ex) { /*OK*/ }
    }
    
    @Test
    public void testHasNaturalIdentifier() throws HibernateException {
        NaturalIdCriterionBuilder instance = (NaturalIdCriterionBuilder) this.instance;
        CriteriaBuilder parentDep = new MockCriterionBuilder(Department.class);

        assertTrue(instance.hasNaturalIdentifier("department", parent));
        assertFalse(instance.hasNaturalIdentifier("head", parentDep));
        try {
            assertFalse(instance.hasNaturalIdentifier("name", parent));
            assertFalse(instance.hasNaturalIdentifier("id", parent));
            assertFalse(instance.hasNaturalIdentifier("invalid", parent));
        } catch (Exception ex) { /*OK*/ }
    }
}
