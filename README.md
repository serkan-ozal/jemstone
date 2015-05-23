# jemstone
Hidden Gems of JVM/JDK

**P.S:** On UNIX and MaxOSX, it may require running the application with `sudo` for necessary user privileges. 

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

* **`jemstone.skipHotspotSAAttach`:** Skips HotSpot Serviceability Agent attaching to current process. This means, no HotSpot SA support. Default value is `false`.

* **`jemstone.skipClasspathLookup`:** Skips looking for HotSpot Serviceability Agent classes at classpath. Default value is `false`.

* **`jemstone.skipJdkHomeLookup`:** Skips looking for HotSpot Serviceability Agent classes at `$JDK_HOME/lib/sa-jdi.jar`. Default value is `false`.

* **`jemstone.skipAllJarLookup`:** Skips looking for HotSpot Serviceability Agent classes from a composite [jar] (https://github.com/serkan-ozal/maven-repository/blob/master/com/sun/tools/sa-jdi-all/1.0/sa-jdi-all-1.0.jar) that contains HotSpot Serviceability Agent classes for different JVM versions (6, 7 and 8) at different directories inside it and these classes are loaded via JVM version aware classloader `tr.com.serkanozal.jemstone.sa.impl.HotSpotJvmAwareSaJdiClassLoader`. Default value is `false`.

* **`jemstone.pipelineSize`:** Max size of data in bytes transferred through pipeline between current process and HotSpot SA process. Since, under the hood, memory mapped file is used for interprocess communication, this value determines the size of the memory mapped file. Default value is `16 * 1024` bytes (`16` KB).

* **`jemstone.maxPipelineSize`:** Maximum size of data in bytes that pipeline size can be expanded until in case of `BufferOverflowException` if `jemstone.disableExpandingPipeline` is enabled. Default value is `256 * 1024 * 1024` bytes (`256` MB).

* **`jemstone.disableExpandingPipeline`:** Disables expanding pipeline size in case of `BufferOverflowException` for transferring data through pipeline between current process and HotSpot SA process. Default value is `false`.

* **`jemstone.timeout`:** Timeout value in milliseconds for getting result from HotSpot SA process. Default value is `5000` milliseconds (`5` seconds).

* **`jemstone.tryWithSudo`:** Skips using `sudo` for creating HotSpot SA process to attach current process on `Unix` based, `MacOSX` and `Solaris` operating systems. Default value is `false`.


4. Usage
==============



5. Roadmap
==============

