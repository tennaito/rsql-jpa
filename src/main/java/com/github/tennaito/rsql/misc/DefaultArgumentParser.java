/*
 * The MIT License
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
 * Copyright 2015 Antonio Rabelo
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
package com.github.tennaito.rsql.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of {@linkplain ArgumentParser}. Supported types
 * are String, Integer, Long, Float, Boolean, Enum and Date. If neither one
 * of them match, it tries to invoke valueOf(String s) method via reflection on
 * the type's class.
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 * @author AntonioRabelo
 */
public class DefaultArgumentParser implements ArgumentParser {

	private static final Logger LOG = Logger.getLogger(DefaultArgumentParser.class.getName());

    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd"); //ISO 8601
    private static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //ISO 8601


    /* (non-Javadoc)
     * @see br.tennaito.rsql.misc.ArgumentParser#parse(java.lang.String, java.lang.Class)
     */
    public <T> T parse(String argument, Class<T> type)
            throws ArgumentFormatException, IllegalArgumentException {

    	LOG.log(Level.INFO, "Parsing argument '{0}' as type {1}", new Object[] {argument, type.getSimpleName()});

        // Nullable object
        if (argument == null || "null".equals(argument.trim().toLowerCase())) {
        	return (T) null;
        }

        // common types
        try {
            if (type.equals(String.class)) return (T) argument;
            if (type.equals(Integer.class) || type.equals(int.class)) return (T) Integer.valueOf(argument);
            if (type.equals(Boolean.class) || type.equals(boolean.class)) return (T) Boolean.valueOf(argument);
            if (type.isEnum()) return (T) Enum.valueOf((Class<Enum>)type, argument);
            if (type.equals(Float.class)   || type.equals(float.class)) return (T) Float.valueOf(argument);
            if (type.equals(Double.class)  || type.equals(double.class)) return (T) Double.valueOf(argument);
            if (type.equals(Long.class)    || type.equals(long.class)) return (T) Long.valueOf(argument);
        } catch (IllegalArgumentException ex) {
            throw new ArgumentFormatException(argument, type);
        }

        // date
        if (type.equals(Date.class)) {
            try {
                return (T) DATE_TIME_FORMATTER.parse(argument);
            } catch (ParseException ex) {
            	LOG.log(Level.INFO, "Not a date time format, lets try with date format.");
            }
            try {
                return (T) DATE_FORMATTER.parse(argument);
            } catch (ParseException ex1) {
                throw new ArgumentFormatException(argument, type);
            }
        }

        // try to parse via valueOf(String s) method
        try {
        	LOG.log(Level.INFO, "Trying to get and invoke valueOf(String s) method on {0}", type);
            Method method = type.getMethod("valueOf", String.class);
            return (T) method.invoke(type, argument);
        } catch (InvocationTargetException ex) {
        	throw new ArgumentFormatException(argument, type);
        } catch (ReflectiveOperationException ex) {
        	LOG.log(Level.WARNING, "{0} does not have method valueOf(String s) or method is inaccessible", type);
        	throw new IllegalArgumentException("Cannot parse argument type " + type);
        }
    }


	/* (non-Javadoc)
	 * @see br.tennaito.rsql.misc.ArgumentParser#parse(java.util.List, java.lang.Class)
	 */
	public <T> List<T> parse(List<String> arguments, Class<T> type)
			throws ArgumentFormatException, IllegalArgumentException {
    	List<T> castedArguments = new ArrayList<T>(arguments.size());
    	for (String argument : arguments) {
    		castedArguments.add(this.parse(argument, type));
    	}
		return castedArguments;
	}
	
	public static void main(String[] args) {
		int e = 1;
		Class int_ = int.class;
		System.out.println(int_.equals(int.class));
	}
}
