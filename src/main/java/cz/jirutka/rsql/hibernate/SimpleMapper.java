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

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of the {@link Mapper}.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class SimpleMapper implements Mapper {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleMapper.class);
    
    private Map<Class<?>, Map<String, String>> mapping;

    
    /**
     * Construct new <tt>SimpleMapper</tt> with zero initial capacity of the
     * entities map.
     */
    public SimpleMapper() {
        mapping = new HashMap<Class<?>, Map<String, String>>(0);
    }
    /**
     * Construct new <tt>SimpleMapper</tt> with the specified initial capacity
     * of the entities map.
     * 
     * @param initialCapacity initial capacity of entities map
     */
    public SimpleMapper(int initialCapacity) {
        mapping = new HashMap<Class<?>, Map<String, String>>(initialCapacity);
    }
    
    
    @Override
    public String translate(String selector, Class<?> entityClass) {
        if (mapping.isEmpty()) return selector;
        
        Map<String, String> map = mapping.get(entityClass);
        String property = (map != null) ? map.get(selector) : null;
        
        if (property != null) {
            LOG.debug("Found mapping {} -> {}", selector, property);
            return property;
        }
        
        return selector;
    }

    
    /**
     * Add selectors -> property names mapping for given entity class.
     * 
     * @param entityClass entity class
     * @param mapping mapping of selectors to property names
     */
    public void addMapping(Class<?> entityClass, Map<String, String> mapping) {
        this.mapping.put(entityClass, mapping);
    }
    
    /**
     * Add one selector -> property name mapping for given entity class.
     * 
     * @param entityClass entity class
     * @param selector Selector that identifies some element of an entry's content.
     * @param property Name of corresponding entity's property.
     */
    public void addMapping(Class<?> entityClass, String selector, String property) {
        mapping.get(entityClass).put(selector, property);
    }

    /**
     * @see SimpleMapper#setMapping(java.util.Map) 
     * @return The current mapping of all entities.
     */
    public Map<Class<?>, Map<String, String>> getMapping() {
        return mapping;
    }

    /**
     * Set the mapping of selectors to property names per entity class.
     * 
     * @param Mapping {entity class -> {selector -> property}}
     */
    public void setMapping(Map<Class<?>, Map<String, String>> mapping) {
        this.mapping = mapping;
    }
    
}
