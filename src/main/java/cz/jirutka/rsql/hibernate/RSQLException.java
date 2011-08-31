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
