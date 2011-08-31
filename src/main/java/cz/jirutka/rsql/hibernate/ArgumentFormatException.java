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

/**
 * Indicate that argument is not in suitable format required by entity's 
 * property, i.e. is not parseable to the specified type.
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class ArgumentFormatException extends Exception {
    
    private final String selector;
    private final String argument;
    private final Class<?> propertyType;

    
    /**
     * Construct an <tt>ArgumentFormatException</tt> with specified argument
     * and property type.
     * 
     * @param argument
     * @param propertyType 
     */
    public ArgumentFormatException(String argument, Class<?> propertyType) {
        super("Cannot cast '" + argument + "' to " + propertyType);
        this.selector = null;
        this.argument = argument;
        this.propertyType = propertyType;
    }
    /**
     * Construct an <tt>ArgumentFormatException</tt> with specified selector,
     * argument and property type.
     * 
     * @param selector
     * @param argument
     * @param propertyType 
     */
    public ArgumentFormatException(String selector, String argument, Class<?> propertyType) {
        super("Argument '" + argument + "' of " + selector + " must be " + propertyType.getSimpleName());
        this.selector = selector;
        this.argument = argument;
        this.propertyType = propertyType;
    }

    
    public String getArgument() {
        return argument;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }

    public String getSelector() {
        return selector;
    }
    
}
