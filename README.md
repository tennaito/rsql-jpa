# RSQL for JPA

[![Build Status](https://travis-ci.org/tennaito/rsql-jpa.svg)](https://travis-ci.org/tennaito/rsql-jpa)
[![Coverage Status](https://coveralls.io/repos/tennaito/rsql-jpa/badge.svg)](https://coveralls.io/r/tennaito/rsql-jpa)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.tennaito/rsql-jpa/badge.svg?style=flat)](http://mvnrepository.com/artifact/com.github.tennaito/rsql-jpa)

RESTful Service Query Language (RSQL) is a language and a library designed for searching entries in RESTful services.

This library provides converter of [RSQL expression](https://github.com/jirutka/rsql-parser) to JPA [Criteria Query](http://docs.oracle.com/javaee/6/tutorial/doc/gjitv.html) (object representation of JPQL), which is translated to SQL query. RSQL was originally created for [KOSapi](https://kosapi.feld.cvut.cz) - RESTful web services for IS at the Czech Technical University in Prague. 

Feel free to contribute!

## Overview

TODO


## Usage

Example of basic usage with only provided builders, default _ArgumentParser_ and without selectors re-mapping:

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

## RSQL syntax

RSQL syntax is described on [RSQL-parser’s project page](https://github.com/jirutka/rsql-parser). There’s only one addition described below.

For comparing string arguments with Equals or Not Equals, you can use wildcards `*` and `_`. If the argument begins or ends with an asterisk character `*`, it acts as a wild card, matching any characters preceding or following (respectively) that position. If the argument also contains an underscore character `_`, it acts as a wildcard, matching exactly one character. It corresponds to the percentage, respectively underscore wildcard of the LIKE condition in SQL.

## Examples

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
    - /courses?query=department==18102 - is related with department 18102 (this is actually ManyToOne association and NaturalID)
    - /courses?query=department.name==*engineering - is guaranteed by the department that name ends to "engineering"
    - /courses?query=name==*services*&orderBy=name&maxResults=50 - name constains "services", order by name and limit output to maximum 50 results

## Maven

```xml
<dependency>
    <groupId>com.github.tennaito</groupId>
    <artifactId>rsql-jpa</artifactId>
    <version>1.0.0</version>
</dependency>
```

## License

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).