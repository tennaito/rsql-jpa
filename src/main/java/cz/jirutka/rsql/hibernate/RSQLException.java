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

/**
 * This is a wrapper exception for {@link ParseException},
 * {@link ArgumentFormatException}, {@link JoinsLimitException} and
 * {@link UnknownSelectorException}.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class RSQLException extends RuntimeException {

    /**
     * Constructs a <tt>RSQLException</tt> with the specified cause and a detail 
     * message of (cause==null ? null : cause.toString()) (which typically 
     * contains the class and detail message of cause).
     * 
     * @param cause The cause (which is saved for later retrieval by the 
     *        Throwable.getCause() method).
     */
    public RSQLException(Throwable cause) {
        super(cause);
    }
    
}
