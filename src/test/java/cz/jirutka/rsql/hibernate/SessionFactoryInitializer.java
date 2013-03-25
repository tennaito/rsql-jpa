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

import cz.jirutka.rsql.hibernate.entity.Department;
import cz.jirutka.rsql.hibernate.entity.Person;
import cz.jirutka.rsql.hibernate.entity.Course;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;

/**
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class SessionFactoryInitializer {
    
    private static SessionFactory instance;
    
    
    public static SessionFactory getSessionFactory() {
        if (instance != null) return instance;
        
        Configuration configuration = new Configuration();
        configuration.setProperty(Environment.DRIVER, "org.hsqldb.jdbcDriver");
        configuration.setProperty(Environment.URL, "jdbc:hsqldb:mem:ProductDAOTest");
        configuration.setProperty(Environment.USER, "sa");
        configuration.setProperty(Environment.DIALECT, HSQLDialect.class.getName());
        configuration.setProperty(Environment.SHOW_SQL, "true");
        configuration.addAnnotatedClass(Course.class);
        configuration.addAnnotatedClass(Department.class);
        configuration.addAnnotatedClass(Person.class);

        instance = configuration.buildSessionFactory();
        
        return instance;
    }
}
