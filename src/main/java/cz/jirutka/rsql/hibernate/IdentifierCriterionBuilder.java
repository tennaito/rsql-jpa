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
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Criterion Builder that can create <tt>Criterion</tt> for a property which
 * represents an association and argument which contains ID of the associated 
 * entity. It accepts only valid property of an association type.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class IdentifierCriterionBuilder extends AbstractCriterionBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(IdentifierCriterionBuilder.class);
    
    
    @Override
    public boolean accept(String property, Class<?> entityClass, CriteriaBuilder builder) {
        ClassMetadata metadata = builder.getClassMetadata(entityClass);
        
        return isPropertyName(property, metadata) && isAssociationType(property, metadata);
    }

    @Override
    public Criterion createCriterion(String property, Comparison operator, 
            String argument, Class<?> entityClass, String alias, CriteriaBuilder builder) 
            throws ArgumentFormatException, UnknownSelectorException {
            
        Class<?> type = findPropertyType(property, builder.getClassMetadata(entityClass));
        ClassMetadata assocClassMetadata = builder.getClassMetadata(type);
        Class<?> idType = assocClassMetadata.getIdentifierType().getReturnedClass();
        
        LOG.debug("Property is association type {}, parsing argument to ID type {}", 
                type, idType.getSimpleName());
        
        Object parsedArgument = builder.getArgumentParser().parse(argument, idType);
        String propertyPath = alias + property + ".id";
        
        return createCriterion(propertyPath, operator, parsedArgument);
    }

    /**
     * Return <tt>true</tt> if property of given name is an association type.
     * 
     * @param property property name
     * @param classMetadata entity metadata
     * @return <tt>true</tt> if given property is an association, <tt>false</tt>
     *         otherwise
     * @throws HibernateException If entity does not contain such property.
     */
    protected boolean isAssociationType(String property, ClassMetadata classMetadata) 
            throws HibernateException {
        Type type = classMetadata.getPropertyType(property);
        
        return type.isEntityType() && !type.isCollectionType();
    }
    
}
