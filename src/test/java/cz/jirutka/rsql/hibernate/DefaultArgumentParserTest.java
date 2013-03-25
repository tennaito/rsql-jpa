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
