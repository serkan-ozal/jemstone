# Jemstone
Hidden Gems of JVM/JDK

**P.S:** On **UNIX** and especially **MaxOSX** operations systems, it may require running the application with `sudo` for necessary user privileges. 

1. What is Jemstone?
==============
**Jemstone** is a platform for running **HotSpot Serviceability Agent API** based implementations on current application (JVM process) or other application (other JVM process). 

Here is the Jemstone execution logic:
* **Jemstone** gets the implementation (`HotSpotServiceabilityAgentWorker`) which depends on **HotSpot Serviceability Agent API**.
* **Jemstone** (current/caller process) creates a new process (call this as **HotSpot SA Process**) and serialize the implementation (`HotSpotServiceabilityAgentWorker`) with parameters (`HotSpotServiceabilityAgentParameter`) if needed through pipeline to **HotSpot SA Process**.
* **HotSpot SA Process** attaches as HotSpot Serviceabiliy Agent to target process (current/caller process or any other process with specified process id). At this time all threads (even native threads) on target JVM process wait at safe point. This means that target process (or application) suspends until **HotSpot SA Process** detaches from it.
* **HotSpot SA Process** runs the implementation (`HotSpotServiceabilityAgentWorker`) on target process (current/caller process or any other process with specified process id) and returns its result (`HotSpotServiceabilityAgentResult`) to caller process (**Jemstone**) by serializing it through pipeline between the current process and **HotSpot SA Process**.
* **Jemstone** (current/caller process) destroys the connection/pipeline between it and  **HotSpot SA Process** then returns the result (`HotSpotServiceabilityAgentResult`) to the caller (**Jemstone** API user).

For more informations about HotSpot Serviceability Agent, please see the following resources:
* [The HotSpot™ Serviceability Agent: An out-of-process high level debugger for a Java™ virtual machine](https://www.usenix.org/legacy/publications/library/proceedings/jvm01/full_papers/russell/russell_html/index.html)
* [Serviceability in HotSpot](http://openjdk.java.net/groups/hotspot/docs/Serviceability.html)



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

* **`jemstone.hotspotsa.skipHotspotSAAttach`:** Skips HotSpot Serviceability Agent attaching to current process. This means, no HotSpot SA support. Default value is `false`.

* **`jemstone.hotspotsa.skipClasspathLookup`:** Skips looking for HotSpot Serviceability Agent classes at classpath. Default value is `false`.

* **`jemstone.hotspotsa.skipJdkHomeLookup`:** Skips looking for HotSpot Serviceability Agent classes at `$JDK_HOME/lib/sa-jdi.jar`. Default value is `false`.

* **`jemstone.hotspotsa.skipAllJarLookup`:** Skips looking for HotSpot Serviceability Agent classes from a composite [jar] (https://github.com/serkan-ozal/maven-repository/blob/master/com/sun/tools/sa-jdi-all/1.0/sa-jdi-all-1.0.jar) that contains HotSpot Serviceability Agent classes for different JVM versions (6, 7 and 8) at different directories inside it and these classes are loaded via JVM version aware classloader `tr.com.serkanozal.jemstone.sa.impl.HotSpotJvmAwareSaJdiClassLoader`. Default value is `false`.

* **`jemstone.hotspotsa.pipelineSize`:** Max size of data in bytes transferred through pipeline between current process and HotSpot SA process. Since, under the hood, memory mapped file is used for interprocess communication, this value determines the size of the memory mapped file. Default value is `16 * 1024` bytes (`16` KB).

* **`jemstone.hotspotsa.maxPipelineSize`:** Maximum size of data in bytes that pipeline size can be expanded until in case of `BufferOverflowException` if `jemstone.hotspotsa.disableExpandingPipeline` is enabled. Default value is `256 * 1024 * 1024` bytes (`256` MB).

* **`jemstone.hotspotsa.disableExpandingPipeline`:** Disables expanding pipeline size in case of `BufferOverflowException` for transferring data through pipeline between current process and HotSpot SA process. Default value is `false`.

* **`jemstone.hotspotsa.timeout`:** Timeout value in milliseconds for getting result from HotSpot SA process. Default value is `5000` milliseconds (`5` seconds).

* **`jemstone.hotspotsa.tryWithSudo`:** Skips using `sudo` for creating HotSpot SA process to attach current process on `Unix` based, `MacOSX` and `Solaris` operating systems. Default value is `false`.


4. Usage
==============



5. Roadmap
==============

