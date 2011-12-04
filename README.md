# RSQL for Hibernate

RESTful Service Query Language (RSQL) is a language and a library designed for searching entries in RESTful services.

This library provides convertor of [RSQL expression](https://github.com/jirutka/rsql-parser) to Hibernate’s [Criteria Query](http://docs.jboss.org/hibernate/core/3.5/reference/en/html/querycriteria.html) (object representation of HQL), which is translated to SQL query. RSQL was originally created for [KOSapi](https://kosapi.feld.cvut.cz) - RESTful web services for IS at the Czech Technical University in Prague. 

Feel free to contribute!


## Overview

RSQL-hibernate consists of five main parts - _RSQLHibernateFactory_, _RSQL2CriteriaConverter_, set of Criterion Builders, _ArgumentParser_ and _Mapper_.

**RSQL2HibernateFactory** is a factory for creating preconfigured instances of the _RSQL2CriteriaConverter_.

**RSQL2CriteriaConverter** is the main client interface that provides methods for creating Hibernate’s _DetachedCriteria_ from an input RSQL expression, or appending to given _Criteria_ instance (i.e. combine static Criteria and user’s RSQL query).
Firstly, the expression is parsed through the [RSQL-parser](https://github.com/jirutka/rsql-parser). The resulting tree is traversed, _Criterions_ for logical operators are created, and comparisons (constraints) are delegated to one of the Criterion Builders. Criterion Builders are arranged in the stack, which is searched for the builder that is able to handle the given comparison. You can simply add your custom builders.

**Criterion Builders** are responsible for creating _Criterion_ from a given comparison. Here comes the juicy part: Before creating a Criterion, the Builder has to match a selector (typically a name of an XML element) with a particular entity’s property and convert an argument to the property type (via given _ArgumentParser_). RSQL-hibernate provides four builders:

* **DefaultCriterionBuilder** - Default implementation, simply creates _Criterion_ for a basic property (not association).
* **IdentifierCriterionBuilder** - Creates _Criterion_ for a property representing an association, and an argument containing ID of the associated entity.
* **NaturalIdCriterionBuilder** - Creates _Criterion_ for a property representing an association, and an argument containing _NaturalID_ of the associated entity.
* **AssociationsCriterionBuilder** - Handles association “dereference”. That means you can specify constraints upon related entities by navigating associations using dot-notation. For example, we have entity Course with property _department_, which is _ManyToOne_ association, and entity Department with basic property _name_. Then we can use `department.name==KSI` to find all courses related to the department KSI. Builder implicitly creates JOIN (i.e. association alias) for every associated entity. You can also set upper limit of JOINs that can be generated.

If you need a custom builder, simply extend _AbstractCriterionBuilder_ (or any of the previously mentioned) and override methods you want.

**ArgumentParser** is used for parsing arguments from RSQL query according to a class type of the target entity’s property. When an argument cannot be parsed as a required type (i.e. is not in suitable format), then it throws an exception with useful information for user what’s wrong with his query. Provided _DefaultArgumentParser_ supports String, Integer, Long, Float, Boolean, Enum and Date. If neither one of them match, it tries to invoke valueOf(String s) method via reflection on the type’s class. If you need support for more types, simply implement your own _ArgumentParser_.

**Mapper** translates a selector to entity’s property name. Translation is done before delegating to Criterion Builder or setting _orderBy_ property. Mapper is useful when you don’t have 1:1 names mapping between selectors and entity properties, i.e. identifier used in RSQL expression doesn’t exactly match name of the corresponding entity’s property. You can use provided _SimpleMapper_ with maps of names mapping per entity or implement your own special Mapper. For example, I have one that maps selectors of multilingual elements according to request’s Accept-Language.


## Usage

Example of basic usage with only provided builders, default _ArgumentParser_ and without selectors remapping:

    // what we need from Hibernate
    SessionFactory sessionFactory;
    Session session;

    // setup factory
    RSQL2HibernateFactory factory = RSQL2HibernateFactory.getInstance();
    factory.setSessionFactory(sessionFactory);

    // create converter (may be done e.g. in Spring’s context)
    RSQL2CriteriaConverter converter = factory.createConverter();

    // parse RSQL and create detached criteria for specified entity class
    DetachedCriteria detached = converter.createCriteria("name==web*;credits>=5", Course.class);

    // connect it with current Hibernate Session
    Criteria criteria = detached.getExecutableCriteria(session);
    
    // execute query and get result
    List<Course> result = criteria.list();
    
You can also specify ordering:

	// ascending order by property "name"
    converter.createCriteria("name==web*;credits>=5", "name", true, Course.class);

or add what you like to _Criteria_:

    detached.setFetchMode("department", FetchMode.JOIN);
    criteria.setMaxResults(50);

Combine your _Criteria_ query and RSQL query from user: 

	// specify your static query
	Criteria criteria = currentSession().createCriteria(Course.class, "c")
			.createCriteria("courseInProgrammes")
			    .add(Restrictions.eq("programme", programme))
			.addOrder(Order.asc("c.name"));
	
	// append user’s RSQL query to given criteria
	converter.extendCriteria("name==web*;department.code==12345", Course.class, criteria)
	
    // execute query and get result
    List<Course> result = criteria.list();

Add _MySpecialCriterionBuilder_ and provided ones to all _RSQL2CriteriaConverter_ instances:

    List<AbstractCriterionBuilder> builders = new ArrayList(4);
    builders.add(new DefaultCriterionBuilder());
    builders.add(new IdentifierCriterionBuilder());
    builders.add(new NaturalIdCriterionBuilder());
    builders.add(new AssociationsCriterionBuilder());
    builders.add(new MySpecialCriterionBuilder());

    factory.setCriterionBuilders(builders);

or only to particular converter:

    converter.pushCriterionBuilder(new MySpecialCriterionBuilder());

When some selector doesn’t match name of its entity’s property, you can use _SimpleMapper_:

    Map<String, String> map = new HashMap<String, String>();
    SimpleMapper mapper = new SimpleMapper();

    // selector -> property
    map.put("name", "nameEn");

    // mapping for entity Course.class
    mapper.addMapping(Course.class, map);

    factory.setMapper(mapper);

Do you like Spring Framework and it’s XML configuration?

	<bean id="rsqlConverter" class="cz.jirutka.rsql.hibernate.RSQL2CriteriaConverterImpl">
        <constructor-arg name="sessionFactory" ref="sessionFactory" />
        <property name="associationsLimit" value="3" />
        <property name="argumentParser" ref="rsqlArgumentParser" />
        <property name="mapper" ref="rsqlMapper" />
        <property name="criterionBuilders">
            <list value-type="cz.jirutka.rsql.hibernate.AbstractCriterionBuilder">
                <bean class="cz.jirutka.rsql.hibernate.AssociationsCriterionBuilder" />
                <bean class="cz.jirutka.rsql.hibernate.NaturalIdCriterionBuilder" />
                <bean class="cz.jirutka.rsql.hibernate.IdentifierCriterionBuilder" />
				<!-- Default must be the last one. -->
                <bean class="cz.jirutka.rsql.hibernate.DefaultCriterionBuilder" />
            </list>
        </property>
    </bean>
    
    <bean id="rsqlArgumentParser" class="cz.jirutka.rsql.hibernate.DefaultArgumentParser" />
    <bean id="rsqlMapper" class="cz.jirutka.rsql.hibernate.SimpleMapper" />


## RSQL syntax

RSQL syntax is described on [RSQL-parser’s project page](https://github.com/jirutka/rsql-parser). There’s only one addition described below.

For comparing string arguments with Equals or Not Equals, you can use wildcards `*` and `_`. If the argument begins or ends with an asterisk character `*`, it acts as a wild card, matching any characters preceding or following (respectively) that position. If the argument also contains an underscore character `_`, it acts as a wildcard, matching exactly one character. It corresponds to the percentage, respectively underscore wildcard of the LIKE condition in SQL.


## Examples

I guess that some practical example will come handy. Below is a truncated output from my RESTful service KOSapi.

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
            <version>1.1</version>
        </dependency>
    </dependencies>

### Manual download

Otherwise, download jar file from [here](https://github.com/downloads/jirutka/rsql-hibernate/rsql-hibernate-1.1.jar).

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