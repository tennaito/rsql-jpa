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
package com.github.tennaito.rsql.jpa;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.github.tennaito.rsql.jpa.entity.Course;
import com.github.tennaito.rsql.misc.ArgumentFormatException;
import com.github.tennaito.rsql.misc.ArgumentParser;
import com.github.tennaito.rsql.misc.DefaultArgumentParser;

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
        
        argument = "654321";
        expected = 654321;
        actual = instance.parse(argument, int.class);
        assertEquals(expected, actual);
        
        try {
            argument = "abc";
            expected = 123456;
            actual = instance.parse(argument, Integer.class);
            fail();
        } catch (ArgumentFormatException e) {
        	assertEquals("Cannot cast '" + argument + "' to type " + Integer.class, e.getMessage());
        	assertEquals(argument, e.getArgument());
        	assertEquals(Integer.class, e.getPropertyType());
        }
        
        argument = "true";
        expected = true;
        actual = instance.parse(argument, Boolean.class);
        assertEquals(expected, actual);
        
        argument = "false";
        expected = false;
        actual = instance.parse(argument, boolean.class);
        assertEquals(expected, actual);
        
        argument = "FOO";
        expected = MockEnum.FOO;
        actual = instance.parse(argument, MockEnum.class);
        assertEquals(expected, actual);
        
        argument = "42.22";
        expected = new Float(42.22);
        actual = instance.parse(argument, Float.class);
        assertEquals(expected, actual);
        
        argument = "22.24";
        expected = 22.24f;
        actual = instance.parse(argument, float.class);
        assertEquals(expected, actual);
        
        argument = "42.22";
        expected = new Double(42.22);
        actual = instance.parse(argument, Double.class);
        assertEquals(expected, actual);
        
        argument = "33.33";
        expected = 33.33d;
        actual = instance.parse(argument, double.class);
        assertEquals(expected, actual);
        
        argument = "123456789123456789";
        expected = new Long(123456789123456789L);
        actual = instance.parse(argument, Long.class);
        assertEquals(expected, actual);
        
        argument = "987654321987654321";
        expected = 987654321987654321L;
        actual = instance.parse(argument, long.class);
        assertEquals(expected, actual);
        
        argument = "2011-08-26";
        expected = new GregorianCalendar(2011, 7, 26).getTime();
        actual = instance.parse(argument, Date.class);
        assertEquals(expected, actual);
        
        argument = "2011-08-26T14:15:30";
        expected = new GregorianCalendar(2011, 7, 26, 14, 15, 30).getTime();
        actual = instance.parse(argument, Date.class);
        assertEquals(expected, actual);
        
        try {
            argument = "2011-08";
            expected = new GregorianCalendar(2011, 7, 26, 14, 15, 30).getTime();
            actual = instance.parse(argument, Date.class);
            fail();
        } catch (ArgumentFormatException e) {
        	assertEquals("Cannot cast '" + argument + "' to type " + Date.class, e.getMessage());
        	assertEquals(argument, e.getArgument());
        	assertEquals(Date.class, e.getPropertyType());
        }
        
        argument = "foo";
        expected = new MockValueOfType();
        actual = instance.parse(argument, MockValueOfType.class);
        assertTrue(actual instanceof MockValueOfType);
        
        try {
        	argument = "foo";
        	expected = new MockPrivateValueOfType();
        	actual = instance.parse(argument, MockPrivateValueOfType.class);
        } catch(IllegalArgumentException e) {
        	assertEquals("Cannot parse argument type " + MockPrivateValueOfType.class, e.getMessage());
        }
        
        try {
        	argument = "foo";
        	expected = new MockBrokenValueOfType();
        	actual = instance.parse(argument, MockBrokenValueOfType.class);
        } catch(ArgumentFormatException e) {
        	assertEquals("Cannot cast '" + argument + "' to type " + MockBrokenValueOfType.class, e.getMessage());
        	assertEquals(argument, e.getArgument());
        	assertEquals(MockBrokenValueOfType.class, e.getPropertyType());
        }
        
        try {
        	argument = "foo";
        	expected = new Course();
        	actual = instance.parse(argument, Course.class);
        } catch(IllegalArgumentException e) {
        	assertEquals("Cannot parse argument type " + Course.class, e.getMessage());
        }
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
    
    protected static class MockPrivateValueOfType {
        
        private static MockPrivateValueOfType valueOf(String s) {
            return new MockPrivateValueOfType();
        }
    }    
    
    protected static class MockBrokenValueOfType {
        
    	public static MockBrokenValueOfType valueOf(String s) {
            throw new RuntimeException();
        }
    }    
}
