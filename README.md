# jemstone
Hidden Gems of JVM/JDK

1. What is Jemstone?
==============



2. Installation
==============

In your `pom.xml`, you must add repository and dependency for **Jemstone**. 
You can change `jemstone.version` to any existing **Jemstone** library version.

``` xml
...
<properties>
    ...
    <jemstone.version>1.0</jemstone.version>
    ...
</properties>
...
<dependencies>
    ...
	<dependency>
		<groupId>tr.com.serkanozal</groupId>
		<artifactId>jemstone</artifactId>
		<version>${jemstone.version}</version>
	</dependency>
	...
</dependencies>
...
<repositories>
	...
	<repository>
		<id>serkanozal-maven-repository</id>
		<url>https://github.com/serkan-ozal/maven-repository/raw/master/</url>
	</repository>
	...
</repositories>
...
```

3. Configurations
==============

* **`jemstone.skipHotspotSAInit`:** 
* **`jemstone.skipHotspotSAAttach`:** 
* **`jemstone.skipClasspathLookup`:** 
* **`jemstone.skipJdkHomeLookup`:** 
* **`jemstone.skipAllJarLookup`:** 
* **`jemstone.pipelineSize`:** 
* **`jemstone.timeout`:** 
* **`jemstone.tryWithSudo`:**


4. Usage
==============



5. Roadmap
==============

* Plug-in support for allowing registering custom Jemstone plugins as explicitly or auto-detected at classpath.
