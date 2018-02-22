/*
 * The MIT License
 *
 * Copyright 2015 Antonio Rabelo
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
package com.github.tennaito.rsql.jpa;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.junit.BeforeClass;

import com.github.tennaito.rsql.jpa.entity.Course;
import com.github.tennaito.rsql.jpa.entity.CourseDetails;
import com.github.tennaito.rsql.jpa.entity.Department;
import com.github.tennaito.rsql.jpa.entity.Person;
import com.github.tennaito.rsql.jpa.entity.PersonDependent;
import com.github.tennaito.rsql.jpa.entity.Teacher;
import com.github.tennaito.rsql.jpa.entity.Title;

/**
 * @author AntonioRabelo
 */
public abstract class AbstractVisitorTest<T> {

    private static boolean loaded = false;

    protected Class<T> entityClass;
    protected EntityManager entityManager;

    @BeforeClass
    public static void setUpBefore() throws Exception {
        if (!loaded) {

            EntityManager entityManager = EntityManagerFactoryInitializer.getEntityManagerFactory()
                    .createEntityManager();
            entityManager.getTransaction().begin();

            Title title1 = new Title();
            title1.setId(1L);
            title1.setName("Phd");
            entityManager.persist(title1);

            Title title2 = new Title();
            title2.setId(2L);
            title2.setName("Consultant");
            entityManager.persist(title2);

            Set<Title> titles = new HashSet<Title>();
            titles.add(title1);
            titles.add(title2);

            Person person = new Person();
            person.setId(2L);
            person.setName("Other");
            person.setSurname("Person");
            entityManager.persist(person);

            Person head = new Person();
            head.setId(1L);
            head.setName("Some");
            head.setSurname("One");
            head.setTitles(titles);
            entityManager.persist(head);

            Department department = new Department();
            department.setId(1L);
            department.setName("Testing");
            department.setCode("MI-MDW");
            department.setHead(head);
            entityManager.persist(department);

            Teacher teacher = new Teacher();
            teacher.setId(23L);
            teacher.setSpecialtyDescription("Maths");
            entityManager.persist(teacher);

            Title titleCourse1 = new Title();
            titleCourse1.setId(100L);
            titleCourse1.setName("course 1");

            Title titleCourse2 = new Title();
            titleCourse2.setId(101L);
            titleCourse2.setName("course 2");

            Title titleCourse3 = new Title();
            titleCourse3.setId(102L);
            titleCourse3.setName("course 3");

            Course c = new Course();
            c.setId(1L);
            c.setCode("MI-MDW");
            c.setActive(true);
            c.setCredits(10);
            c.setName("Testing Course");
            c.setDepartment(department);
            c.setDetails(CourseDetails.of("test"));
            c.getDetails().setTeacher(teacher);
            c.setStartDate(new Date());
            c.getTitles().addAll(Arrays.asList(titleCourse1, titleCourse2, titleCourse3));
            entityManager.persist(c);

            Department department1 = new Department();
            department1.setId(0L);
            department1.setName("Testing");
            department1.setCode("z");
            entityManager.persist(department1);

            Course c1 = new Course();
            c1.setId(0L);
            c1.setCode("z");
            c1.setActive(true);
            c1.setCredits(10);
            c1.setName("Testing Course");
            c1.setDepartment(department1);
            c1.setDetails(CourseDetails.of("test"));
            c1.getDetails().setTeacher(teacher);
            c1.setStartDate(new GregorianCalendar(1985, Calendar.FEBRUARY, 11).getTime());
            entityManager.persist(c1);

            if (hasEntity(PersonDependent.class)) {

                PersonDependent dependent = new PersonDependent();
                dependent.setAge(2);
                dependent.getId().setName("son1");
                dependent.getId().setPerson(head);
                entityManager.persist(dependent);

            }

            entityManager.getTransaction().commit();

            loaded = true;
        }
    }

    public static boolean hasEntity(Class<?> clazz) {

        Set<EntityType<?>> entities = EntityManagerFactoryInitializer.getEntityManagerFactory().getMetamodel()
                .getEntities();

        for (EntityType<?> entityType : entities) {

            if (entityType.getJavaType().equals(clazz)) {
                return true;
            }

        }

        return false;
    }
}
