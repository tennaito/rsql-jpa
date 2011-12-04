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

import cz.jirutka.rsql.hibernate.entity.Person;
import cz.jirutka.rsql.hibernate.entity.Course;
import cz.jirutka.rsql.hibernate.entity.Department;
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
public class AssociationsCriterionBuilderTest extends AbstractCriterionBuilderTest {
    
    @Before
    public void setUp() throws Exception {
        instance = new AssociationsCriterionBuilder();
        entityClass = Course.class;
        parent = new MockInnerBuilder(Course.class);
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
    }
    

    @Test
    public void testAccept() {
        assertTrue(instance.accept("department.name", entityClass, parent));
        assertTrue(instance.accept("department.head.name", entityClass, parent));
        assertFalse(instance.accept("department", entityClass, parent));
    }

    @Test
    public void testCreateCriterion() throws Exception {
        Criterion expResult;
        Criterion result;

        
        CriteriaBuilder parent1 = new MockInnerBuilder(Course.class) {
            @Override
            public String createAssociationAlias(String property) throws AssociationsLimitException {
                assertEquals("this.department", property);
                return "alias1";
            }
            @Override
            public Criterion delegateToBuilder(String property, Comparison operator, String argument, Class<?> entityClass, String alias) 
                    throws ArgumentFormatException, UnknownSelectorException, IllegalStateException {
                assertEquals("name", property);
                assertEquals(Comparison.EQUAL, operator);
                assertEquals("KSI", argument);
                assertEquals(Department.class, entityClass);
                assertEquals(alias, "alias1.");
                return Restrictions.eq("alias1.name", "KSI");
            }
            
        };
        expResult = Restrictions.eq("alias1.name", "KSI");
        result = instance.createCriterion("department.name", Comparison.EQUAL, "KSI", entityClass, "this.", parent1);
        //equals() on Criterion doesn't work here, hence toString()
        assertEquals(expResult.toString(), result.toString());
        
        
        CriteriaBuilder parent2 = new MockInnerBuilder(Course.class) {
            private int associations = 0;
            @Override
            public String createAssociationAlias(String property) throws AssociationsLimitException {
                if (associations == 0) {
                    assertEquals("this.department", property);
                    associations++;
                    return "alias1";
                } else if (associations == 1) {
                    assertEquals("alias1.head", property);
                    associations++;
                    return "alias2";
                }
                fail("Should call createAssociationAlias only twice.");
                return null;
            }
            @Override
            public Criterion delegateToBuilder(String property, Comparison operator, String argument, Class<?> entityClass, String alias) 
                    throws ArgumentFormatException, UnknownSelectorException, IllegalStateException {
                assertEquals("surname", property);
                assertEquals("Torvalds", argument);
                assertEquals(Person.class, entityClass);
                assertEquals(alias, "alias2.");
                return Restrictions.eq("alias2.surname", "Torvalds");
            }
        };
        expResult = Restrictions.eq("alias2.surname", "Torvalds");
        result = instance.createCriterion("department.head.surname", Comparison.EQUAL, "Torvalds", entityClass, "this.", parent2);
        //equals() on Criterion doesn't work here, hence toString()
        assertEquals(expResult.toString(), result.toString());

        
        try {
            result = instance.createCriterion("department.code", Comparison.EQUAL, "non-numeric", entityClass, "this.", parent);
            fail("Should raise an ArgumentFormatException");
        } catch (ArgumentFormatException ex) { /*OK*/ }
    }

}
