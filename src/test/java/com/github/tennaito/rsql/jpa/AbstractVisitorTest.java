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

import javax.persistence.EntityManager;

import org.junit.BeforeClass;

import com.github.tennaito.rsql.jpa.entity.Course;
import com.github.tennaito.rsql.jpa.entity.Department;
import com.github.tennaito.rsql.jpa.entity.Person;

/**
 * @author AntonioRabelo
 */
public abstract class AbstractVisitorTest<T> {

    protected Class<T> entityClass;
    protected EntityManager entityManager;

	@BeforeClass
	public static void setUpBefore() throws Exception {
    	EntityManager entityManager = EntityManagerFactoryInitializer.getEntityManagerFactory().createEntityManager();
    	entityManager.getTransaction().begin();

    	Person head = new Person();
    	head.setId(1L);
    	head.setName("Some");
    	head.setSurname("One");
    	entityManager.persist(head);

    	Department department = new Department();
    	department.setId(1L);
    	department.setName("Testing");
    	department.setCode("MI-MDW");
    	department.setHead(head);
    	entityManager.persist(department);

    	Course c = new Course();
    	c.setId(1L);
    	c.setCode("MI-MDW");
    	c.setActive(true);
    	c.setCredits(10);
    	c.setName("Testing Course");
    	c.setDepartment(department);
    	entityManager.persist(c);

    	entityManager.getTransaction().commit();
	}
}
