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

import cz.jirutka.rsql.hibernate.entity.Course;
import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class JoinsCriterionBuilderTest extends AbstractCriterionBuilderTest {
    
    @Before
    public void setUp() throws Exception {
        instance = new JoinsCriterionBuilder();
        parent = new MockCriterionBuilder(Course.class);
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
    }
    

    @Test
    public void testAccept() {
        assertTrue(instance.canAccept("department.name", Comparison.EQUAL, parent));
        assertTrue(instance.canAccept("department.head.name", Comparison.EQUAL, parent));
        assertFalse(instance.canAccept("department", Comparison.EQUAL, parent));
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
        expResult = Restrictions.eq("alias1.name", "KSI");
        result = instance.createCriterion("department.name", Comparison.EQUAL, "KSI", parent1);
        //equals() on Criterion doesn't work here, hence toString()
        assertEquals(expResult.toString(), result.toString());
        
        
        CriteriaBuilder parent2 = new MockCriterionBuilder(Course.class) {
            private int joins = 0;
            public String addJoin(String property) throws JoinsLimitException {
                if (joins == 0) {
                    assertEquals("department", property);
                    joins++;
                    return "alias1";
                } else if (joins == 1) {
                    assertEquals("alias1.head", property);
                    joins++;
                    return "alias2";
                }
                fail("Should call addJoin only twice.");
                return null;
            }
        };
        expResult = Restrictions.eq("alias2.surname", "Torvalds");
        result = instance.createCriterion("department.head.surname", Comparison.EQUAL, "Torvalds", parent2);
        //equals() on Criterion doesn't work here, hence toString()
        assertEquals(expResult.toString(), result.toString());

        
        try {
            result = instance.createCriterion("department.code", Comparison.EQUAL, "non-numeric", parent1);
            fail("Should raise an ArgumentFormatException");
        } catch (ArgumentFormatException ex) { /*OK*/ }
    }

}
