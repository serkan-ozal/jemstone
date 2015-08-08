/*
 * Copyright (c) 1986-2015, Serkan OZAL, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tr.com.serkanozal.jemstone;

import sun.jvm.hotspot.gc_interface.CollectedHeap;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentConfig;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentManager;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter.NoHotSpotServiceabilityAgentParameter;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentPlugin;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResultProcessor;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotSAKeyValueResult;

/**
 * Demo application for Jemstone framework usage.
 * 
 * @author Serkan Ozal
 */
public class Demo {

    private static final HotSpotServiceabilityAgentManager hotSpotSAManager;
    
    static {
        // Don't look at the classpath for sa-jdi.jar
        System.setProperty("jemstone.hotspotsa.skipClasspathLookup", "true");
        hotSpotSAManager = Jemstone.getHotSpotServiceabilityAgentManager();
    }
    
    public static void main(String[] args) {
        System.out.println(hotSpotSAManager.details());

        // ///////////////////////////////////////////////////////////////////////////////

        System.out.println(hotSpotSAManager.getCompressedReferences());

        // ///////////////////////////////////////////////////////////////////////////////
        
        stackTraceDemo();

        // ///////////////////////////////////////////////////////////////////////////////

        System.out.println(hotSpotSAManager.executeOnHotSpotSA(new HeapSummaryWorker()));
        
        // ///////////////////////////////////////////////////////////////////////////////
        
        System.out.println(hotSpotSAManager.runPlugin(HeapSummaryPlugin.PLUGIN_ID));
        
        // ///////////////////////////////////////////////////////////////////////////////
    }
    
    @SuppressWarnings("unused")
    private static void stackTraceDemo() {
        int localVar_stackTraceDemo = 1000;
        method1(true, 10, 20.0F, 30L, 40.0, 'X', "str");
    }
    
    @SuppressWarnings("unused")
    private static void method1(boolean b1, int i1, float f1, long l1, double d1, char c1, String s1) {
        int localVar_method1 = 2000;
        method2(!b1, i1 * i1, f1 * f1, l1 * l1, d1 * d1, 'Y', s1 + "-" + s1);
    }
    
    @SuppressWarnings("unused")
    private static void method2(boolean b2, int i2, float f2, long l2, double d2, char c2, String s2) {
        int localVar_method2 = 3000;
        Foo foo = new Foo();
        int[] intArray = {10, 20, 30};
        Object[] objArray = {
                new Foo(true, 1000, 2000.0, "foo.sss", new Bar()),
                "I am a string in object array"
            };
        
        System.out.println(hotSpotSAManager.getStackTracesOfCurrentThread());
    }
    
    static class Foo {
        
        boolean b = true;
        int i = 100;
        double d = 200.0;
        String s = "foo.s";
        Bar bar = new Bar();
        
        Foo() {
            
        }

        Foo(boolean b, int i, double d, String s, Bar bar) {
            this.b = b;
            this.i = i;
            this.d = d;
            this.s = s;
            this.bar = bar;
        }
        
    }
    
    static class Bar {
        
    }

    @SuppressWarnings("serial")
    public static class HeapSummaryWorker
            implements HotSpotServiceabilityAgentWorker<NoHotSpotServiceabilityAgentParameter, HotSpotSAKeyValueResult> {

        @Override
        public HotSpotSAKeyValueResult run(HotSpotServiceabilityAgentContext context,
                NoHotSpotServiceabilityAgentParameter param) {
            CollectedHeap heap = context.getVM().getUniverse().heap();
            long startAddress = Long.parseLong(heap.start().toString().substring(2), 16);
            long capacity = heap.capacity();
            return new HotSpotSAKeyValueResult().
                            addResult("startAddress", "0x" + Long.toHexString(startAddress)).
                            addResult("endAddress", "0x" + Long.toHexString(startAddress + capacity)).
                            addResult("capacity", capacity);
        }

    }
    
    public static class HeapSummaryPlugin 
            implements HotSpotServiceabilityAgentPlugin<NoHotSpotServiceabilityAgentParameter, 
                                                        HotSpotSAKeyValueResult, 
                                                        HeapSummaryWorker> {
        
        public static final String PLUGIN_ID = "HotSpot_Heap_Summarizer";
        
        private static final JavaVersion[] SUPPORTED_JAVA_VERSIONS = 
                new JavaVersion[] { 
                    JavaVersion.ALL_VERSIONS 
                };
        
        private static final String USAGE = 
                Jemstone.class.getName() + " " + 
                    "(-i " + "\"" + PLUGIN_ID + "\"" + " <process_id>)" + 
                    " | " + 
                    "(-p " + HeapSummaryPlugin.class.getName() + " <process_id>)";
        
        private int processId = HotSpotServiceabilityAgentConfig.CONFIG_NOT_SET;
                    
        @Override
        public String getId() {
            return PLUGIN_ID;
        }

        @Override
        public String getUsage() {
            return USAGE;
        }

        @Override
        public JavaVersion[] getSupportedJavaVersions() {
            return SUPPORTED_JAVA_VERSIONS;
        }

        @Override
        public HeapSummaryWorker getWorker() {
            return new HeapSummaryWorker();
        }

        @Override
        public NoHotSpotServiceabilityAgentParameter getParamater(String[] args) {
            // No need to parameter
            return null;
        }

        @Override
        public HotSpotServiceabilityAgentConfig getConfig() {
            if (processId != HotSpotServiceabilityAgentConfig.CONFIG_NOT_SET) {
                HotSpotServiceabilityAgentConfig config = new HotSpotServiceabilityAgentConfig();
                // Only set "process id" and don't touch others ("pipeline size", "timeout").
                // So default configurations will be used for them ("pipeline size", "timeout").
                config.setProcessId(processId);
                return config;
            } else {
                // Use default configuration, so just returns "null"
                return null;
            }
        }

        @Override
        public HotSpotServiceabilityAgentResultProcessor<HotSpotSAKeyValueResult> getResultProcessor() {
            // Use default result processor (print to console)
            return null;
        }
        
    }

}
