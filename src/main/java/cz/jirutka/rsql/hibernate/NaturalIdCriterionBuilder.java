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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Criterion Builder that can create <tt>Criterion</tt> for a property which
 * represents an association and argument which contains 
 * {@linkplain org.hibernate.annotations.NaturalId NaturalID} of the associated 
 * entity. It accepts only valid property of association type which associates
 * an entity that has a natural identifier.
 * 
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class NaturalIdCriterionBuilder extends IdentifierCriterionBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(NaturalIdCriterionBuilder.class);

    
    @Override
    public boolean canAccept(String property, Comparison operator, CriteriaBuilder builder) {
        return super.canAccept(property, operator, builder) 
                && hasNaturalIdentifier(property, builder);
    }

    @Override
    public Criterion createCriterion(String property, Comparison operator, 
            String argument, CriteriaBuilder builder) 
            throws ArgumentFormatException, UnknownSelectorException {

        Class<?> type = findPropertyType(property, builder.getClassMetadata());
        ClassMetadata classMetadata = builder.getClassMetadata(type);
        int[] idProps = classMetadata.getNaturalIdentifierProperties();
        Class<?> idType = classMetadata.getPropertyTypes()[idProps[0]].getReturnedClass();
        String idName = classMetadata.getPropertyNames()[idProps[0]];
        
        if (idProps.length != 1) {
            LOG.warn("Entity {} has more than one Natural ID, only first will be used");
        }
        LOG.debug("Entity {} has Natural ID {} of type {}", 
                new Object[]{type.getSimpleName(), idName, idType.getSimpleName()});

        Object castedArgument = parseArgument(argument, idType);
        
        String entityAlias = builder.addJoin(property);

        return createCriterion(entityAlias +'.'+ idName, operator, castedArgument);

    }
    
    /**
     * Check if given property associates an entity that has a natural 
     * identifier. This doesn't check if property is an association type or even
     * if it exists!
     * 
     * @param property property name
     * @param builder parent <tt>CriteriaBuilder</tt>
     * @return <tt>true</tt> if given property associates an entity that has
     *         a natural identifier, <tt>false</tt> otherwise
     * @throws HibernateException If entity does not contain such property or
     *         it's not an association type.
     */
    protected boolean hasNaturalIdentifier(String property, CriteriaBuilder builder) 
            throws HibernateException {
        Class<?> type = findPropertyType(property, builder.getClassMetadata());
        ClassMetadata assocClassMetadata = builder.getClassMetadata(type);
        
        return assocClassMetadata.hasNaturalIdentifier();
    }

}
