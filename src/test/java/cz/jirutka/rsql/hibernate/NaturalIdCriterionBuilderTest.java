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
