/*
 * The MIT License
 *
 * Copyright 2015 Antonio Rabelo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.tennaito.rsql.jpa;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.junit.Before;
import org.junit.Test;

import com.github.tennaito.rsql.builder.BuilderTools;
import com.github.tennaito.rsql.jpa.entity.Course;
import com.github.tennaito.rsql.misc.SimpleMapper;
import com.github.tennaito.rsql.parser.ast.ComparisonOperatorProxy;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.AbstractNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.LogicalOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

/**
 * @author AntonioRabelo
 */
public class JpaVisitorTest extends AbstractVisitorTest<Course> {

	final static XorNode xorNode = new XorNode(new ArrayList<Node>());
	private static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");
	
    @Before
    public void setUp() throws Exception {
        entityManager = EntityManagerFactoryInitializer.getEntityManagerFactory().createEntityManager();
        entityClass = Course.class;
    }

    @Test
    public void testUnknowProperty() throws Exception {
        try {
            Node rootNode = new RSQLParser().parse("invalid==1");
            RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
            CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

            List<Course> courses = entityManager.createQuery(query).getResultList();
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown property: invalid from entity " + Course.class.getName(), e.getMessage());
        }
    }

    @Test
    public void testSimpleSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("id==1");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testSimpleSelectionWhenPassingArgumentInTemplate() throws Exception {
        Node rootNode = new RSQLParser().parse("id==1");
        // not a recommended usage
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>(new Course());
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }


    @Test
    public void testNotEqualSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("id!=1");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals(0, courses.size());
    }

    @Test
    public void testGreaterThanSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("id=gt=1");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals(0, courses.size());
    }

    @Test
    public void testGreaterThanEqualSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("id=ge=1");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testGreaterThanEqualDate() throws Exception {
    	Date date = Calendar.getInstance().getTime();
    	Node rootNode = new RSQLParser().parse("date=ge='"+DATE_TIME_FORMATTER.format(date)+"'");
    	RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
    	CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

    	List<Course> courses = entityManager.createQuery(query).getResultList();
    	assertEquals("Testing Course", courses.get(0).getName());
    }
    
    @Test
    public void testGreaterThanDate() throws Exception {
        Date date = Calendar.getInstance().getTime();
        Node rootNode = new RSQLParser().parse("date=gt='"+DATE_TIME_FORMATTER.format(date)+"'");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals(0, courses.size());
    }

    @Test
    public void testLessThanSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("id=lt=1");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals(0, courses.size());
    }
    
    @Test
    public void testLessThanEqualDate() throws Exception {
    	Date date = new Date(999999999999999999L);
    	Node rootNode = new RSQLParser().parse("date=le='"+DATE_TIME_FORMATTER.format(date)+"'");
    	RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
    	CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

    	List<Course> courses = entityManager.createQuery(query).getResultList();
    	assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testLessThanDate() throws Exception {
        Date date = Calendar.getInstance().getTime();
        Node rootNode = new RSQLParser().parse("date=lt='"+DATE_TIME_FORMATTER.format(date)+"'");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals(0, courses.size());
    }
    
    @Test
    public void testLessThanEqualSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("id=le=1");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testInSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("id=in=(1,2,3,4)");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testOutSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("id=out=(1,2,3,4)");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals(0, courses.size());
    }

    @Test
    public void testLikeSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("name==*Course");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testNotLikeSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("name!=*Course");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals(0, courses.size());
    }


    @Test
    public void testIsNullSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("name==null");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals(0, courses.size());
    }

    @Test
    public void testNotIsNullSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("name!=null");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testUndefinedComparisonOperator() {
        try {
            ComparisonOperator newOp = new ComparisonOperator("=def=");
            Set<ComparisonOperator> set = new HashSet<ComparisonOperator>();
            set.add(newOp);
            Node rootNode = new RSQLParser(set).parse("id=def=null");
            RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
            CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);
            List<Course> courses = entityManager.createQuery(query).getResultList();
            fail();
        } catch (Exception e) {
            assertEquals("Unknown operator: =def=", e.getMessage());
        }
    }

    @Test
    public void testDefinedComparisonOperator() {
        // define the new operator
        ComparisonOperator newOp = new ComparisonOperator("=def=");
        Set<ComparisonOperator> set = new HashSet<ComparisonOperator>();
        set.add(newOp);
        // execute parser
        Node rootNode = new RSQLParser(set).parse("id=def=1");

        JpaCriteriaQueryVisitor<Course> visitor = new JpaCriteriaQueryVisitor<Course>();
        createDefOperator(visitor);

        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);
        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    private void createDefOperator(JpaCriteriaQueryVisitor<Course> visitor) {
        // define new operator resolver
        PredicateBuilderStrategy predicateStrategy = new PredicateBuilderStrategy() {
            public <T> Predicate createPredicate(Node node, From root, Class<T> entity, EntityManager manager,
                    BuilderTools tools) throws IllegalArgumentException {
                ComparisonNode comp = ((ComparisonNode) node);
                ComparisonNode def = new ComparisonNode(ComparisonOperatorProxy.EQUAL.getOperator(), comp.getSelector(),
                        comp.getArguments());
                return PredicateBuilder.createPredicate(def, root, entity, manager, tools);
            }
        };
        visitor.getBuilderTools().setPredicateBuilder(predicateStrategy);
    }

    @Test
    public void testAssociationSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("department.id==1");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testAssociationAliasSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("dept_id==1");
        JpaCriteriaQueryVisitor<Course> visitor = new JpaCriteriaQueryVisitor<Course>();
        // add to SimpleMapper
        assertNotNull(((SimpleMapper) visitor.getBuilderTools().getPropertiesMapper()).getMapping());
        ((SimpleMapper) visitor.getBuilderTools().getPropertiesMapper()).addMapping(Course.class,
                new HashMap<String, String>());
        ((SimpleMapper) visitor.getBuilderTools().getPropertiesMapper()).addMapping(Course.class, "dept_id",
                "department.id");

        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);
        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());

        ((SimpleMapper) visitor.getBuilderTools().getPropertiesMapper()).setMapping(null);
        assertNull(((SimpleMapper) visitor.getBuilderTools().getPropertiesMapper()).getMapping());
    }

    @Test
    public void testAndSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("department.id==1;id==2");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals(0, courses.size());
    }

    @Test
    public void testOrSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("department.id==1,id==2");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testVariousNodesSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("((department.id==1;id==2),id<3);department.id=out=(3,4,5)");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testNavigateThroughCollectionSelection() throws Exception {
        Node rootNode = new RSQLParser().parse("department.head.titles.name==Phd");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testUnsupportedNode() throws Exception {
        try {
            PredicateBuilder.createPredicate(new OtherNode(), null, null, null, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown expression type: class com.github.tennaito.rsql.jpa.JpaVisitorTest$OtherNode",
                    e.getMessage());
        }
    }

    @Test
    public void testSetBuilderTools() throws Exception {
        JpaCriteriaQueryVisitor<Course> visitor = new JpaCriteriaQueryVisitor<Course>();
        visitor.setBuilderTools(null);
        assertNotNull(visitor.getBuilderTools());

        visitor.getBuilderTools().setArgumentParser(null);
        assertNotNull(visitor.getBuilderTools().getArgumentParser());

        visitor.getBuilderTools().setPropertiesMapper(null);
        assertNotNull(visitor.getBuilderTools().getPropertiesMapper());

        visitor.getBuilderTools().setPredicateBuilder(null);
        assertNull(visitor.getBuilderTools().getPredicateBuilder());
    }

    @Test
    public void testUnsupportedLogicalNode() throws Exception {
        try {
            PredicateBuilder.createPredicate(JpaVisitorTest.xorNode, null, Course.class, entityManager, null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown operator: ^", e.getMessage());
        }
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<PredicateBuilder> priv = PredicateBuilder.class.getDeclaredConstructor();
        // It is really private?
        assertFalse(priv.isAccessible());
        priv.setAccessible(true);
        Object predicateBuilder = priv.newInstance();
        // When used it returns a instance?
        assertNotNull(predicateBuilder);
    }

    ////////////////////////// Mocks //////////////////////////

    protected static class OtherNode extends AbstractNode {

        public <R, A> R accept(RSQLVisitor<R, A> visitor, A param) {
            throw new UnsupportedOperationException();
        }
    }

    protected static class XorNode extends LogicalNode {

        final static LogicalOperator XOR = createLogicalOperatorXor();

        public XorNode(List<? extends Node> children) {
            super(XOR, children);
        }

        public static void setStaticFinalField(Field field, Object value)
                throws NoSuchFieldException, IllegalAccessException {
            // we mark the field to be public
            field.setAccessible(true);
            // next we change the modifier in the Field instance to
            // not be final anymore, thus tricking reflection into
            // letting us modify the static final field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            int modifiers = modifiersField.getInt(field);
            // blank out the final bit in the modifiers int
            modifiers &= ~Modifier.FINAL;
            modifiersField.setInt(field, modifiers);
            sun.reflect.FieldAccessor fa =
                    sun.reflect.ReflectionFactory.getReflectionFactory().newFieldAccessor(field, false);
            fa.set(null, value);
        }

        private static LogicalOperator createLogicalOperatorXor() {
            LogicalOperator xor = null;
            try {
                Constructor<LogicalOperator> cstr =
                        LogicalOperator.class.getDeclaredConstructor(String.class, int.class, String.class);
                sun.reflect.ReflectionFactory factory = sun.reflect.ReflectionFactory.getReflectionFactory();
                xor = (LogicalOperator) factory.newConstructorAccessor(cstr).newInstance(new Object[] {"XOR", 2, "^"});

                Field ordinalField = Enum.class.getDeclaredField("ordinal");
                ordinalField.setAccessible(true);

                LogicalOperator[] values = xor.values();
                Field valuesField = LogicalOperator.class.getDeclaredField("ENUM$VALUES");
                valuesField.setAccessible(true);
                LogicalOperator[] newValues = Arrays.copyOf(values, values.length + 1);
                newValues[newValues.length - 1] = xor;
                setStaticFinalField(valuesField, newValues);
                int ordinal = newValues.length - 1;
                ordinalField.set(xor, ordinal);
            } catch (ReflectiveOperationException e) {
                // do nothing
                e.printStackTrace();
            }
            return xor;
        }

        @Override
        public LogicalNode withChildren(List<? extends Node> children) {
            return new XorNode(children);
        }

        public <R, A> R accept(RSQLVisitor<R, A> visitor, A param) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void testUndefinedRootForPredicate() throws Exception {
        try {
            Node rootNode = new RSQLParser().parse("id==1");
            RSQLVisitor<Predicate, EntityManager> visitor = new JpaPredicateVisitor<Course>();
            Predicate query = rootNode.accept(visitor, entityManager);
        } catch (IllegalArgumentException e) {
            assertEquals("From root node was undefined.", e.getMessage());
        }
    }

    @Test
    public void testSelectionUsingEmbeddedField() throws Exception {
        Node rootNode = new RSQLParser().parse("details.description==test");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }

    @Test
    public void testSelectionUsingEmbeddedAssociationField() throws Exception {
        Node rootNode = new RSQLParser().parse("details.teacher.specialtyDescription==Maths");
        RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();
        CriteriaQuery<Course> query = rootNode.accept(visitor, entityManager);

        List<Course> courses = entityManager.createQuery(query).getResultList();
        assertEquals("Testing Course", courses.get(0).getName());
    }


}
