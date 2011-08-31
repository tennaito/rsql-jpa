# RSQL for Hibernate

RESTful Service Query Language (RSQL) is my language and library that is designed for searching entries in RESTful services. 

This library provides convertor of [RSQL expression](https://github.com/jirutka/rsql-parser) to Hibernate’s [Criteria Query](http://docs.jboss.org/hibernate/core/3.5/reference/en/html/querycriteria.html) (object representation of HQL), from which is then generated SQL query. RSQL was originally created for [KOSapi](https://kosapi.fit.cvut.cz) - RESTful web services for our IS at the Czech Technical University in Prague. 


## Overview

RSQL-hibernate consists of four main parts - _RSQLHibernateFactory_, _RSQLCriteriaBuilder_, set of Criterion Builders and _Mapper_.

**RSQLHibernateFactory** is a factory for creating preconfigured instances of the _RSQLCriteriaBuilder_ for a particular entity class. This is mostly useful when you’re using some IoC container like Spring Framework and its XML configuration of beans.

**RSQLCriteriaBuilder** is the main client interface that provides methods for creating Hibernate’s _DetachedCriteria_ from an input RSQL expression. This workflow begins with parsing the expression through the [RSQL-parser](https://github.com/jirutka/rsql-parser). Then it traverses resulting tree, creates _Criterions_ for logical operators and delegates comparisons (constraints) to one of the Criterion Builders. It iterates over the builders stack to find the builder that can handle given comparison. You can simply add your custom ones.

**Criterion Builders** are responsible for creating _Criterion_ from given comparison. Here comes the most interesting part. Before do that, it has to match a selector (typically name of XML element) with a particular entity’s property and convert argument to the property type. RSQL-hibernate provides four builders:

* **DefaultCriterionBuilder** - Default implementation that simply creates _Criterion_ for a basic property (not association).
* **IdentifierCriterionBuilder** - Builder that can create _Criterion_ for a property which represents an association and argument which contains ID of the associated entity.
* **NaturalIdCriterionBuilder** - Builder that can create _Criterion_ for a property which represents an association and argument which contains _NaturalID_ of the associated entity.
* **JoinsCriterionBuilder** - This builder can handle association “dereference”. That means you can specify constraints upon related entities by navigating associations using dot-notation. For example, we have entity Course with property _department_, which is _ManyToOne_ association, and entity Department with basic property _name_. Then we can use `department.name==KSI` to find all courses related to department KSI. Builder implicitly creates JOIN for every associated entity. You can also set upper limit of JOINs that can be generated.

If you need some special builder, simply extend _AbstractCriterionBuilder_ (or one of these) and override methods you want.

**Mapper** is used to translate a selector to entity’s property name. Translation is done before delegating to Criterion Builder or setting _orderBy_ property. Mapper is useful when you don’t have 1:1 names mapping between selectors and entity properties, i.e. identifier used in RSQL expression doesn’t exactly match name of the corresponding entity’s property. You can use provided _SimpleMapper_ with maps of names mapping per entity or implement your own special Mapper. For example, I have one that maps selectors of multilingual elements according to request _Accept-Language_. 


## Usage

Example of basic usage with only default _DefaultCriterionBuilder_ and without selectors remapping:

    // what we need from Hibernate
    SessionFactory sessionFactory;
    Session session;

    // setup factory (should be done e.g. in Spring’s context)
    RSQLHibernateFactory factory = RSQLHibernateFactory.getInstance();
    factory.setSessionFactory(sessionFactory);

    // get builder for a particular entity class
    RSQLCriteriaBuilder builder = factory.createBuilder(Course.class);

    // parse RSQL and create detached criteria
    DetachedCriteria detached = builder.parse("name==web*;credits>=5");

    // connect it with current Hibernate Session
    Criteria criteria = detached.getExecutableCriteria(session);
    
    // execute query and get result
    List<Course> result = criteria.list();
    
You can also specify ordering:

    builder.parse("name==web*;credits>=5", "name", OrderType.ASCENDING);

or add what you like to _Criteria_, but be aware of aliases:

    detached.setFetchMode(RSQLCriteriaBuilder.ROOT_ALIAS + ".department", FetchMode.JOIN);
    criteria.setMaxResults(50);

Add special Criterion Builders to all _RSQLCriteriaBuilder_ instances:

    List<AbstractCriterionBuilder> builders = new ArrayList(4);
    builders.add(new DefaultCriterionBuilder());
    builders.add(new IdentifierCriterionBuilder());
    builders.add(new NaturalIdCriterionBuilder());
    builders.add(new JoinsCriterionBuilder());

    factory.setCriterionBuilders(builders);

or only to particular builder:

    builder.pushCriterionBuilder(new NaturalIdCriterionBuilder());

When some selector doesn’t match name of its entity’s property, you can use _SimpleMapper_:

    Map<String, String> map = new HashMap<String, String>();
    SimpleMapper mapper = new SimpleMapper();

    // selector -> property
    map.put("name", "nameEn");

    // mapping for entity Course.class
    mapper.addMapping(Course.class, map);

    factory.setMapper(mapper);


## RSQL syntax

RSQL syntax is described on the project page of [RSQL-parser](https://github.com/jirutka/rsql-parser). There’s only one addition described below.

For comparing string arguments with Equals or Not Equals, you can use wildcards `*` and `_`. If the argument begins or ends with an asterisk character `*`, it acts as a wild card, matching any characters preceding or following (respectively) that position. If it also contains an underscore character `_`, it acts as a wildcard, matching exactly one any character. It corresponds to the percentage, respectively underscore wildcard of the LIKE condition in SQL.


## Examples

I guess that some practical example will come handy. Below is truncated output from my RESTful service KOSapi.

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


## Download

RSQL-parser uses Maven2 as its build tool.

### Maven artifact
 
If you’re using Maven2, simply add these lines to your _pom.xml_:

    <repositories>
        <repository>
            <id>jirutka.cz</id>
            <name>Repository with RSQL</name>
            <url>http://repos.jirutka.cz/maven/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>cz.jirutka.rsql</groupId>
            <artifactId>rsql-hibernate</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>

### Manual download

Otherwise, download jar file from [here](https://github.com/downloads/jirutka/rsql-hibernate/rsql-hibernate-1.0.jar).

Compile dependencies:

* [rsql-parser](https://github.com/jirutka/rsql-parser)
* [hibernate-core](http://www.hibernate.org/downloads.html)
* [slf4j-api](http://www.slf4j.org/download.html)

Test dependencies:

* [junit](https://github.com/KentBeck/junit/downloads)
* [hsqldb](http://sourceforge.net/projects/hsqldb/files/hsqldb/)
* [hibernate-entitymanager](http://www.hibernate.org/downloads.html)


## License

This project is licensed under [LGPL version 3](http://www.gnu.org/licenses/lgpl.txt).