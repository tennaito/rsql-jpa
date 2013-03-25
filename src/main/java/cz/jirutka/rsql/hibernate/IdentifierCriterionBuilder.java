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
