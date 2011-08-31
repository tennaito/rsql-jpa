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
        parent = new MockCriterionBuilder(Course.class);
        sessionFactory = SessionFactoryInitializer.getSessionFactory();
    }
    
    
    @Test
    public void testAccept() {
        assertTrue(instance.canAccept("department", Comparison.EQUAL, parent));
        assertFalse(instance.canAccept("name", Comparison.EQUAL, parent));
        assertFalse(instance.canAccept("id", Comparison.EQUAL, parent));
        assertFalse(instance.canAccept("invalid", Comparison.EQUAL, parent));
    }

    @Test
    public void testCreateCriterion() throws Exception {
        Criterion expResult;
        Criterion result;
        
        expResult = Restrictions.eq("department.id", 123456L);
        result = instance.createCriterion("department", Comparison.EQUAL, "123456", parent);
        //equals() on Criterion doesn't work here, hence toString()
        assertEquals(expResult.toString(), result.toString());

        try {
            result = instance.createCriterion("department", Comparison.EQUAL, "non-number", parent);
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
