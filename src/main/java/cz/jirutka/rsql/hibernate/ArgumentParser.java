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
 * Interface for Argument Parser that is used for parsing given string argument
 * from RSQL query according to type of the target property.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
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
}
