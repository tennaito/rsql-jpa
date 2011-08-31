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
 * Provides mapping of selectors to property names of entities.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public interface Mapper {
    
    /**
     * Translate given selector to the mapped property name or dot-separated
     * path of the property.
     * 
     * @param selector Selector that identifies some element of an entry's content.
     * @param entityClass entity class
     * @return Property name or dot-separated path of the property.
     */
    String translate(String selector, Class<?> entityClass);
    
}
