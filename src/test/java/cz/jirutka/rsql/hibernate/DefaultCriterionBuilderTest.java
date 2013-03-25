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
import org.junit.Before;
import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class DefaultCriterionBuilderTest extends AbstractCriterionBuilderTest {
    
    @Before
    public void setUp() throws Exception {
        instance = new DefaultCriterionBuilder();
        entityClass = Course.class;
        parent = new MockInnerBuilder(Course.class);
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
    }
    
    
    @Test
    public void testAccept() {
        assertTrue(instance.accept("whatever", entityClass, parent));
    }

    @Test
    public void testCreateCriterion() throws Exception {
        Criterion expResult;
        Criterion result;
        
        expResult = Restrictions.eq("that.code", "MI-MDW");
        result = instance.createCriterion("code", Comparison.EQUAL, "MI-MDW", entityClass, "that.", parent);
        //equals() on Criterion doesn't work here, hence toString()
        assertEquals(expResult.toString(), result.toString());

        try {
            result = instance.createCriterion("invalid", Comparison.EQUAL, "MI-W20", entityClass, "that.", parent);
            fail("Should raise an UnknownSelectorException");
        } catch (UnknownSelectorException ex) { /*OK*/ }
        
        try {
            result = instance.createCriterion("credits", Comparison.LESS_EQUAL, "non-number", entityClass, "that.", parent);
            fail("Should raise an ArgumentFormatException");
        } catch (ArgumentFormatException ex) { /*OK*/ }
        
    }
       
}
