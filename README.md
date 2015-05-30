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

4.1. Worker Based Implementation
--------------

4.2. Plug-in Based Implementation
--------------

4.3. Built-in Implementations
--------------

4.3.1. Stack Trace Dumper
--------
**Stack Trace Dumper** feature dumps stack trace(s) of specified thread(s) or all threads with more additional informations from usual stack trace such as method frame type (interpreted, compiled or native) and local variable names, types, values in stack. 

One of the way of using **Stack Trace Dumper** feature is using it through `HotSpotServiceabilityAgentManager`:
- `HotSpotServiceabilityAgentManager.getStackTracesOfCurrentThread()`: Dumps stack trace of caller thread.
- `HotSpotServiceabilityAgentManager.getStackTraces(String ... threadNames)`: Dumps stack traces of given thread names. If `threadNames` is empty or null, stack traces of all threads are dumped.
- `HotSpotServiceabilityAgentManager.getStackTraces(int processId, String ... threadNames)`: Dumps stack traces of given thread names on given process with `processId`. If `threadNames` is empty or null, stack traces of all threads are dumped.

All results are returned as `HotSpotSAStackTracerResult`. `HotSpotSAStackTracerResult` instance has stack trace dumps per thread. So all stack trace dumps can be accessed by `HotSpotSAStackTracerResult.getStackTraces()` which returns a `java.util.Map<String, String>` with keys are thread names and values are stack trace dumps or any specific stack trace dump can be accessed via `HotSpotSAStackTracerResult.getStackTrace(String threadName)` which return stack trace dump as `String` of given thread.

The another way of using **Stack Trace Dumper** feature is using it from command line as plugin. Usage format of **Stack Trace Dumper** plugin is:
```
tr.com.serkanozal.jemstone.Jemstone 
	(-i "HotSpot_Stack_Tracer" <process_id> [thread_name]*) 
	| 
	(-p tr.com.serkanozal.jemstone.sa.impl.stacktracer.HotSpotSAStackTracerPlugin <process_id> [thread_name]*) 
```

- The `processId` parameter is the id of process to be attached and dumped stack traces of threads on it.
- The `thread_name` parameter is optional so if it is not specified, all threads are dumped.

Here is the sample output of **Stack Trace Dumper** feature:
```
HotSpotSAStackTracerResult [stackTraces=
- Thread Name: main
    |- tr.com.serkanozal.jemstone.Demo.method2(boolean, int, float, long, double, char, java.lang.String) @bci=100, line=74, pc=0x00000001078797e4 (Interpreted frame)
	parameters:
	       name                      value                          type
	    ==============================================================================
	    |- b2                        false                          boolean
	    |- i2                        100                            int
	    |- f2                        400.000000                     float
	    |- l2                        900                            long
	    |- d2                        1600.000000                    double
	    |- c2                        Y                              char
	    |- s2                        str-str                        java.lang.String
	local variables:
	       name                      value                          type
	    ==============================================================================
	    |- localVar_method2          3000                           int
	    |- foo                       0x000000076afd8328 (address)   tr.com.serkanozal.jemstone.Demo$Foo
		fields:
		       name                      value                          type
		    ==============================================================================
		    |- b                         true                           boolean
		    |- i                         100                            int
		    |- d                         200.000000                     double
		    |- s                         foo.s                          java.lang.String
		    |- bar                       0x000000076afdb358 (address)   tr.com.serkanozal.jemstone.Demo$Bar

	    |- intArray                  0x000000076afdb368 (address)   int[]
		elements:
		    ==============================================================================
		    |- [0]: 10
		    |- [1]: 20
		    |- [2]: 30

	    |- objArray                  0x000000076afdb388 (address)   java.lang.Object[]
		elements:
		    ==============================================================================
		    |- [0]: 0x000000076afdb3a0 (tr.com.serkanozal.jemstone.Demo$Foo)
		    |- [1]: String in object array (java.lang.String)

    |- tr.com.serkanozal.jemstone.Demo.method1(boolean, int, float, long, double, char, java.lang.String) @bci=55, line=61, pc=0x000000010787998d (Interpreted frame)
	parameters:
	       name                      value                          type
	    ==============================================================================
	    |- b1                        true                           boolean
	    |- i1                        10                             int
	    |- f1                        20.000000                      float
	    |- l1                        30                             long
	    |- d1                        40.000000                      double
	    |- c1                        X                              char
	    |- s1                        str                            java.lang.String
	local variables:
	       name                      value                          type
	    ==============================================================================
	    |- localVar_method1          2000                           int
    |- tr.com.serkanozal.jemstone.Demo.stackTraceDemo() @bci=19, line=55, pc=0x000000010787998d (Interpreted frame)
	parameters:
	       name                      value                          type
	    ==============================================================================
	local variables:
	       name                      value                          type
	    ==============================================================================
	    |- localVar_stackTraceDemo   1000                           int
    |- tr.com.serkanozal.jemstone.Demo.main(java.lang.String[]) @bci=28, line=43, pc=0x000000010787998d (Interpreted frame)
	parameters:
	       name                      value                          type
	    ==============================================================================
	    |- args                      0x000000076af7a440 (address)   java.lang.String[]
		elements:
		    ==============================================================================

]
```

As seen from the output, variable types are also resolved and values are interpreted as their types. `String` instances are handled in a specific way and its characters are printed as value. Also complex object are printed as all field names/ types/values of these objects.

And this is a sample internal usage of this feature: https://github.com/serkan-ozal/jemstone/blob/master/src/main/java/tr/com/serkanozal/jemstone/Demo.java#L45

4.3.2. Compressed References Finder
--------
**Compressed References Finder** feature finds the compressed references information (such as are compressed-references enabled or not, if enabled what is its base offset and shift size) on the target JVM process (on current or another JVM process). 

One of the way of using **Compressed References Finder** feature is using it through `HotSpotServiceabilityAgentManager`:
- `HotSpotServiceabilityAgentManager.getCompressedReferences()`: Gets compressed references information of current JVM process.
- `HotSpotServiceabilityAgentManager.getCompressedReferences(int processId)`: Gets compressed references information of target JVM process specified with `processId`.

All results are returned as `HotSpotSACompressedReferencesResult`. `HotSpotSACompressedReferencesResult` instance has compressed references mode (enabled or disabled), compressed references base address and shift size informations for oops and classes. These values for classes maybe may be different from oops on Java 8+ because since Java 8, there is another class based compressed references configuration then oops based.

The another way of using **Compressed References Finder** feature is using it from command line as plugin. Usage format of **Compressed References Finder** plugin is:
```
tr.com.serkanozal.jemstone.Jemstone 
	(-i ""HotSpot_Compressed_References_Finder" <process_id>) 
	| 
	(-p tr.com.serkanozal.jemstone.sa.impl.compressedrefs.HotSpotSACompressedReferencesPluginn <process_id>) 
```

- The `processId` parameter is the id of process to be attached and finding compressed references informations on it.

Here is the sample output of **Compressed References Finder** feature:
```
HotSpotSACompressedReferencesResult [addressSize=8, objectAlignment=8, oopSize=4, compressedOopsEnabled=true, narrowOopBase=0, narrowOopShift=3, klassOopSize=4, compressedKlassOopsEnabled=true, narrowKlassBase=0, narrowKlassShift=3]
```

And this is a sample internal usage of this feature: https://github.com/serkan-ozal/jemstone/blob/master/src/main/java/tr/com/serkanozal/jemstone/Demo.java#L41

5. Roadmap
==============

- Built-in **Heap Dump** support/implementation

- Remote process attaching support
