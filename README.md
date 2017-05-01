# RSQL for JPA

[![Build Status](https://travis-ci.org/tennaito/rsql-jpa.svg)](https://travis-ci.org/tennaito/rsql-jpa)
[![Coverage Status](https://coveralls.io/repos/tennaito/rsql-jpa/badge.svg)](https://coveralls.io/r/tennaito/rsql-jpa)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tennaito/rsql-jpa/badge.svg?style=flat)](http://mvnrepository.com/artifact/com.github.tennaito/rsql-jpa)

RESTful Service Query Language (RSQL) is a language and a library designed for searching entries in RESTful services.

This library provides converter of [RSQL expression](https://github.com/jirutka/rsql-parser) to JPA [Criteria Query](http://docs.oracle.com/javaee/6/tutorial/doc/gjitv.html) (object representation of JPQL), which is translated to SQL query. RSQL was originally created for [KOSapi](https://kosapi.feld.cvut.cz) - RESTful web services for IS at the Czech Technical University in Prague. 

Feel free to contribute!

## Overview

The interaction with this API occurs from the  _JpaCriteriaQueryVisitor_ class. It has provided method for configuration purposes: _getBuilderTools_.

From _getBuilderTools_ you can access or modify the _ArgumentParser_ (a class responsible for parsing the string arguments into respective classes), the _PropertiesMapper_ (a class responsible from re-mapping properties) and an optional _PredicateBuilder_ (needed when you have an new _ComparisonNode_ defined with the rsql-parser API).

If you want more control you may use the new _JpaPredicateVisitor_ class.

In the usage section we will cover all that usages. 


## Usage

### _JpaCriteriaQueryVisitor_ class: 

Example of basic usage with only provided predicate builders, default _ArgumentParser_ and without selectors re-mapping:

```java

// We will need a JPA EntityManager
EntityManager manager;

// Create the JPA Visitor
RSQLVisitor<CriteriaQuery<Course>, EntityManager> visitor = new JpaCriteriaQueryVisitor<Course>();

// Parse a RSQL into a Node
Node rootNode = new RSQLParser().parse("id==1");

// Visit the node to retrieve CriteriaQuery
CriteriaQuery<Course> query = rootNode.accept(visitor, manager);

// Do all sort of operations you want with the criteria query
query.orderBy(...);

// Execute and get results
List<Course> courses = entityManager.createQuery(query).getResultList();
```

If you want to create another comparison node you must configure the Rsql-parser _Node_ and the _JpaCriteriaQueryVisitor_:

```java
// We will need a JPA EntityManager
EntityManager manager;

// We will need your specific PredicateBuilderStrategy (if you want a set of new operators create a delegation strategy)
PredicateBuilderStrategy predicateStrategy = new MyDefOperatorStrategy();

// Create the JPA Visitor
JpaCriteriaQueryVisitor<Course> visitor = new JpaCriteriaQueryVisitor<Course>();
visitor.getBuilderTools().setPredicateBuilder(predicateStrategy);

// Define the new operator into rsql-parser API
Set<ComparisonOperator> operators = RSQLOperators.defaultOperators();
operators.add(new ComparisonOperator("=def="));

// execute parser
Node rootNode = new RSQLParser(operators).parse("id=def=1");

// Visit the node to retrieve CriteriaQuery
CriteriaQuery<Course> query = rootNode.accept(visitor, manager);

// Execute and get results
List<Course> courses = entityManager.createQuery(query).getResultList();
```

If you want to change to argument parser you must configure the _JpaCriteriaQueryVisitor_:

```java
// We will need a JPA EntityManager
EntityManager manager;

// We will need your specific ArgumentParser
ArgumentParser argumentParser = new MyNewArgumentParser();

// Create the JPA Visitor
JpaCriteriaQueryVisitor<Course> visitor = new JpaCriteriaQueryVisitor<Course>();
visitor.getBuilderTools().setArgumentParser(argumentParser);

// execute parser
Node rootNode = new RSQLParser().parse("mysteriousElementType==Xyz123");

// Visit the node to retrieve CriteriaQuery
CriteriaQuery<Course> query = rootNode.accept(visitor, manager);

// Execute and get results
List<Course> courses = entityManager.createQuery(query).getResultList();
```

Finally, if you want to re-map the selector property name you must configure the _JpaCriteriaQueryVisitor_:
```java
// We will need a JPA EntityManager
EntityManager manager;

// We will need your specific Mapper
Mapper propertyMapper = new MyCustomPropertiesMapper();

// Create the JPA Visitor
JpaCriteriaQueryVisitor<Course> visitor = new JpaCriteriaQueryVisitor<Course>();
visitor.getBuilderTools().setPropertiesMapper(propertyMapper);

// execute parser (nice feature for translating propertyNames to another language ...)
// 'departamento' translating to 'department'
// 'responsavel' translating to 'head'
// 'sobrenome' translating to 'surname'
// Node rootNode = new RSQLParser().parse("departamento.responsavel.sobrenome==One");

// execute parser (... or if you want to create aliases)
// 'd' translating to 'department'
// 'h' translating to 'head'
// 'sn' translating to 'surname'
Node rootNode = new RSQLParser().parse("d.h.sn==One");

// Visit the node to retrieve CriteriaQuery
CriteriaQuery<Course> query = rootNode.accept(visitor, manager);

// Execute and get results
List<Course> courses = entityManager.createQuery(query).getResultList();
```

### _JpaPredicateVisitor_ class:

Example of basic usage with only provided predicate builders, default _ArgumentParser_ and without selectors re-mapping:

```java

// We will need a JPA EntityManager
EntityManager manager;

// Create criteria and from 
CriteriaBuilder builder = manager.getCriteriaBuilder();
CriteriaQuery criteria = builder.createQuery(Course.class);
From root = criteria.from(Course.class);

// Create the JPA Predicate Visitor
RSQLVisitor<Predicate, EntityManager> visitor = new JpaPredicateVisitor<Course>().defineRoot(root);

// Parse a RSQL into a Node
Node rootNode = new RSQLParser().parse("id==1");

// Visit the node to retrieve CriteriaQuery
Predicate predicate = rootNode.accept(visitor, manager);

// Use generated predicate as you like
criteria.where(predicate);
```

## RSQL syntax

RSQL syntax is described on [RSQL-parser’s project page](https://github.com/jirutka/rsql-parser). There’s only one addition described below.

For comparing string arguments with Equals or Not Equals, you can use wildcards `*` and `_`. If the argument begins or ends with an asterisk character `*` (converted to '%' defined in JSR 317, section 4.6.10), it acts as a wild card, matching any characters preceding or following (respectively) that position. If the argument also contains an underscore character `_` (JSR 317, section 4.6.10), it acts as a wildcard, matching exactly one character. It corresponds to the percentage, respectively underscore wildcard of the LIKE condition in SQL.

## Examples of RSQL

I guess that some practical example will come handy. Below is a truncated output from my RESTful service KOSapi.

```xml
<atom:feed xml:lang="en" xml:base="https://kosapi.fit.cvut.cz/api/3/">
    ...
    <atom:entry>
        <atom:title>Web Services and Middleware</atom:title>
        <atom:id>https://kosapi.fit.cvut.cz/api/3/courses/MI-MDW</atom:id>
        ...
        <atom:content xsi:type="kos:course" atom:type="xml">
            <code>MI-MDW</code>
            <completion>CREDIT_EXAM</completion>
            <credits>4</credits>
            <name>Web Services and Middleware</name>
            <season>WINTER</season>
            <department xlink:href="units/18102">Department of Software Engineering</department>
            ...
        </atom:content>
    </atom:entry>
    ...
</atom:feed>
```

Now some real examples of RSQL queries.

    finds courses which...
    - /courses?query=code==MI-MDW - code matches MI-MDW
    - /courses?query=name==*services* - name contains "services"
    - /courses?query=name=='web services*' - name begins with "web services"
    - /courses?query=credits>3 - is for more than 3 credits
    - /courses?query=name==*web*;season==WINTER;(completion==CLFD_CREDIT,completion==CREDIT_EXAM) - name contains "web" and season is "WINTER" and completion is CLFD_CREDIT or CREDIT_EXAM
    - /courses?query=department.id==18102 - is related with department id 18102
    - /courses?query=department.name==*engineering - is guaranteed by the department that name ends to "engineering"
    - /courses?query=name==*services*&orderBy=name&maxResults=50 - name contains "services", order by name and limit output to maximum 50 results

## Maven

```xml
<dependency>
    <groupId>com.github.tennaito</groupId>
    <artifactId>rsql-jpa</artifactId>
    <version>2.0.2</version>
</dependency>
```

## License

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).

## Change log

- (2.0.2) Minor changes;
- (2.0.1) Added Embeddable property Path;
		  Resolved thread safed of Data formatting;
		  Minor updates;
- (2.0.0) Correcting the design of the JPA Queries creation. 
		  That allows Hibernate provider to work correctly.
          When using Hibernate only use 4.3.10.Final or newer.
- (1.0.2) Adding a Predicate Visitor.
- (1.0.1) Added navigation through collection graphs.
