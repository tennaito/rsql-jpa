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
