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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@linkplain ArgumentParser}. Supported types 
 * are String, Integer, Long, Float, Boolean, Enum and Date. If neither one 
 * of them match, it tries to invoke valueOf(String s) method via reflection on
 * the type's class.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class DefaultArgumentParser implements ArgumentParser {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultArgumentParser.class);
    
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd"); //ISO 8601
    private static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //ISO 8601
    
    
    @Override
    public <T> T parse(String argument, Class<T> type)
            throws ArgumentFormatException, IllegalArgumentException {
        
        LOG.trace("Parsing argument '{}' as type {}", argument, type.getSimpleName());
        
        // common types
        try {
            if (type.equals(String.class)) return (T) argument;
            if (type.equals(Integer.class)) return (T) Integer.valueOf(argument);
            if (type.equals(Boolean.class)) return (T) Boolean.valueOf(argument);
            if (type.isEnum()) return (T) Enum.valueOf((Class<Enum>)type, argument);
            if (type.equals(Float.class)) return (T) Float.valueOf(argument);
            if (type.equals(Double.class)) return (T) Double.valueOf(argument);
            if (type.equals(Long.class)) return (T) Long.valueOf(argument);
        } catch (IllegalArgumentException ex) {
            throw new ArgumentFormatException(argument, type);
        }
        
        // date
        if (type.equals(Date.class)) {
            try {
                return (T) DATE_TIME_FORMATTER.parse(argument);
            } catch (ParseException ex) {}
            try {
                return (T) DATE_FORMATTER.parse(argument);
            } catch (ParseException ex1) {
                throw new ArgumentFormatException(argument, type);
            }
        }
        
        // try to parse via valueOf(String s) method
        try {
            LOG.trace("Trying to get and invoke valueOf(String s) method on {}", type);
            Method method = type.getMethod("valueOf", String.class);
            return (T) method.invoke(type, argument);
            
        } catch (NoSuchMethodException ex) {
            LOG.warn("{} does not have method valueOf(String s)", type);
        } catch (InvocationTargetException ex) {
            throw new ArgumentFormatException(argument, type);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        throw new IllegalArgumentException("Cannot parse argument type " + type);
    }

    
}
