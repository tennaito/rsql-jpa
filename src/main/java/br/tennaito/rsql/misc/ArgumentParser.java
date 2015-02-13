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
package br.tennaito.rsql.misc;

import java.util.List;

/**
 * Interface for Argument Parser that is used for parsing given string argument
 * from RSQL query according to type of the target property.
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 * @author AntonioRabelo
 */
public interface ArgumentParser {

    /**
     * Parse given string argument as the specified class type.
     *
     * @param <T> class type
     * @param argument string argument
     * @param type class type
     * @return The instance of the given argument in the specified type.
     * @throws ArgumentFormatException If the given argument is not parseable
     *         to the specified type.
     * @throws IllegalArgumentException If the specified type is not supported.
     */
    <T> T parse(String argument, Class<T> type)
    		throws ArgumentFormatException, IllegalArgumentException;

    /**
     * Create an array of arguments casted to their correct types.
     *
     * @param arguments List of all arguments in String format.
     * @param type      type class type.
     * @return          The list with instances of the given arguments in the specified type.
     * @throws ArgumentFormatException If the a given argument is not parseable
     *         to the specified type.
     * @throws IllegalArgumentException If the specified type is not supported.
     */
    <T> List<T> parse(List<String> arguments, Class<T> type)
    		throws ArgumentFormatException, IllegalArgumentException;
}
