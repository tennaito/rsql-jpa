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
