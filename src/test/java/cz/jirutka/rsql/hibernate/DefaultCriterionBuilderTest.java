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
        parent = new MockCriterionBuilder(Course.class);
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
    }
    
    
    @Test
    public void testAccept() {
        assertTrue(instance.canAccept("whatever", Comparison.EQUAL, parent));
    }

    @Test
    public void testCreateCriterion() throws Exception {
        Criterion expResult;
        Criterion result;
        
        expResult = Restrictions.eq("code", "MI-MDW");
        result = instance.createCriterion("code", Comparison.EQUAL, "MI-MDW", parent);
        //equals() on Criterion doesn't work here, hence toString()
        assertEquals(expResult.toString(), result.toString());

        try {
            result = instance.createCriterion("invalid", Comparison.EQUAL, "MI-W20", parent);
            fail("Should raise an UnknownSelectorException");
        } catch (UnknownSelectorException ex) { /*OK*/ }
        
        try {
            result = instance.createCriterion("credits", Comparison.LESS_EQUAL, "non-number", parent);
            fail("Should raise an ArgumentFormatException");
        } catch (ArgumentFormatException ex) { /*OK*/ }
        
    }
       
}
