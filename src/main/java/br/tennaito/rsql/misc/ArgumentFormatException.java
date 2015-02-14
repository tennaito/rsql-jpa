/*
 * The MIT License
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
 * Coryright 2015 Antonio Rabelo.
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
package br.tennaito.rsql.misc;

/**
 * Indicate that argument is not in suitable format required by entity's
 * property, i.e. is not parseable to the specified type.
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 * @author AntonioRabelo
 */
public class ArgumentFormatException extends RuntimeException {

    /**
	 * SERIAL UID
	 */
	private static final long serialVersionUID = 521849874508654920L;
	
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
        super("Cannot cast '" + argument + "' to type " + propertyType);
        this.argument = argument;
        this.propertyType = propertyType;
    }


    public String getArgument() {
        return argument;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }
}
