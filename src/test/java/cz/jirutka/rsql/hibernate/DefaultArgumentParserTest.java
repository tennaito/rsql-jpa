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
import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class DefaultArgumentParserTest {
    
    protected ArgumentParser instance = new DefaultArgumentParser();
    

    @Test
    public void testParseArgument() throws Exception {
        String argument;
        Object expected;
        Object actual;
        
        argument = "string";
        expected = "string";
        actual = instance.parse(argument, String.class);
        assertEquals(expected, actual);
        
        argument = "123456";
        expected = 123456;
        actual = instance.parse(argument, Integer.class);
        assertEquals(expected, actual);
        
        argument = "true";
        expected = true;
        actual = instance.parse(argument, Boolean.class);
        assertEquals(expected, actual);
        
        argument = "FOO";
        expected = MockEnum.FOO;
        actual = instance.parse(argument, MockEnum.class);
        assertEquals(expected, actual);
        
        argument = "42.22";
        expected = new Float(42.22);
        actual = instance.parse(argument, Float.class);
        assertEquals(expected, actual);
        
        argument = "42.22";
        expected = 42.22;
        actual = instance.parse(argument, Double.class);
        assertEquals(expected, actual);
        
        argument = "123456789123456789";
        expected = 123456789123456789L;
        actual = instance.parse(argument, Long.class);
        assertEquals(expected, actual);
        
        argument = "2011-08-26";
        expected = new GregorianCalendar(2011, 7, 26).getTime();
        actual = instance.parse(argument, Date.class);
        assertEquals(expected, actual);
        
        argument = "2011-08-26T14:15:30";
        expected = new GregorianCalendar(2011, 7, 26, 14, 15, 30).getTime();
        actual = instance.parse(argument, Date.class);
        assertEquals(expected, actual);
        
        argument = "foo";
        expected = new MockValueOfType();
        actual = instance.parse(argument, MockValueOfType.class);
        assertTrue(actual instanceof MockValueOfType);
        
    }
    
    
    
    ////////////////////////// Mocks //////////////////////////
    
    protected enum MockEnum {
        FOO, BAR;
    }
    
    protected static class MockValueOfType {
        
        public static MockValueOfType valueOf(String s) {
            return new MockValueOfType();
        }
    }
    
}
