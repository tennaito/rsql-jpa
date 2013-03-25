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

import cz.jirutka.rsql.hibernate.entity.Course;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.metadata.ClassMetadata;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class IdentifierCriterionBuilderTest extends AbstractCriterionBuilderTest {
    
    
    @Before
    public void setUp() throws Exception {
        instance = new IdentifierCriterionBuilder();
        entityClass = Course.class;
        parent = new MockInnerBuilder(Course.class);
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
    }
    
    
    @Test
    public void testAccept() {
        assertTrue(instance.accept("department", entityClass, parent));
        assertFalse(instance.accept("name", entityClass, parent));
        assertFalse(instance.accept("id", entityClass, parent));
        assertFalse(instance.accept("invalid", entityClass, parent));
    }

    @Test
    public void testCreateCriterion() throws Exception {
        Criterion expResult;
        Criterion result;
        
        expResult = Restrictions.eq("that.department.id", 123456L);
        result = instance.createCriterion("department", Comparison.EQUAL, "123456", entityClass, "that.", parent);
        //equals() on Criterion doesn't work here, hence toString()
        assertEquals(expResult.toString(), result.toString());

        try {
            result = instance.createCriterion("department", Comparison.EQUAL, "non-number", entityClass, "that.", parent);
            fail("Should raise an ArgumentFormatException");
        } catch (ArgumentFormatException ex) { /*OK*/ }
    }
    
    @Test
    public void testIsAssociationType() {
        IdentifierCriterionBuilder instance = (IdentifierCriterionBuilder) this.instance;
        ClassMetadata classMetadata = sessionFactory.getClassMetadata(Course.class);
        
        assertTrue(instance.isAssociationType("department", classMetadata));
        assertFalse(instance.isAssociationType("name", classMetadata));
        assertFalse(instance.isAssociationType("id", classMetadata));
        try {
            assertFalse(instance.isAssociationType("invalid", classMetadata));
        } catch (Exception ex) { /*OK*/ }
        
    }
    
}
