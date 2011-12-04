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

import cz.jirutka.rsql.parser.model.Comparison;
import org.hibernate.criterion.Criterion;

/**
 * Default implementation of Criterion Builder that simply creates 
 * <tt>Criterion</tt> for a basic property (not association). This should be the 
 * last builder in stack because its <tt>accept()</tt> method always returns 
 * <tt>true</tt>. Before creating a Criterion, property name is checked if it's 
 * valid and {@link UnknownSelectorException} is thrown if not.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class DefaultCriterionBuilder extends AbstractCriterionBuilder {
    
    
    @Override
    public boolean accept(String property, Class<?> entityClass, CriteriaBuilder builder) {
        return true;
    }
    
    @Override
    public Criterion createCriterion(String property, Comparison operator, 
            String argument, Class<?> entityClass, String alias, CriteriaBuilder builder) 
            throws ArgumentFormatException, UnknownSelectorException {
        
        if (!isPropertyName(property, builder.getClassMetadata(entityClass))) {
            throw new UnknownSelectorException(property);
        }
        
        Class<?> type = findPropertyType(property, builder.getClassMetadata(entityClass));
        Object castedArgument = builder.getArgumentParser().parse(argument, type);
        
        return createCriterion(alias + property, operator, castedArgument);
    }
    
}
