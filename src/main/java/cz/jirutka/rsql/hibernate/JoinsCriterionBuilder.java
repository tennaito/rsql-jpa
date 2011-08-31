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
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Criterion Builder that can handle association "dereference". That means you 
 * can specify constraints upon related entities by navigating associations
 * using dot-notation. Builder implicitly creates JOIN for every associated 
 * entity.
 * 
 * <p>For example, we have entity <tt>Course</tt> with property 
 * <tt>department</tt> which is ManyToOne association with entity 
 * <tt>Unit</tt>. That entity contains property <tt>code</tt> of type Integer.
 * When you want to find all courses which are related with department code
 * 18102, you will use query <tt>department.code==18102</tt> on courses 
 * resource.</p>
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
//@TODO vyřešit dereferenci ID a NaturalID
public class JoinsCriterionBuilder extends AbstractCriterionBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(JoinsCriterionBuilder.class);
    
    
    @Override
    public boolean canAccept(String propPath, Comparison operator, CriteriaBuilder builder) {       
        return splitPath(propPath).length > 1;
    }

    @Override
    public Criterion createCriterion(String propPath, Comparison operator, 
            String argument, CriteriaBuilder builder) 
            throws ArgumentFormatException, UnknownSelectorException, JoinsLimitException {
        
        String[] properties = splitPath(propPath);
        String entityAlias = "";
        String property = null;
        Class<?> type = builder.getEntityClass();
        
        for (int i = 0; i < properties.length; i++) {
            ClassMetadata metadata = builder.getClassMetadata(type);
            property = builder.getMapper().translate(properties[i], type);
            
            if (!isPropertyName(property, metadata)) {
                throw new UnknownSelectorException(property);
            }
            type = findPropertyType(property, metadata);
            
            LOG.trace("Nesting level {}: property '{}' of type '{}'",
                    new Object[]{i, property, type});
            
            if (i < properties.length -1) {
                entityAlias = builder.addJoin(entityAlias + property) + '.';
            }
        }
        
        Object castedArgument = parseArgument(argument, type);
        
        return createCriterion(entityAlias + property, operator, castedArgument);
    }
    
    protected String[] splitPath(String path) {
        return path.split("\\.");
    }
    
}
