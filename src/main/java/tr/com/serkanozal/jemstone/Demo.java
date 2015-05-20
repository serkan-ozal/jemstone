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

import java.util.Arrays;
import java.util.HashSet;

import sun.jvm.hotspot.gc_interface.CollectedHeap;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentManager;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter.NoHotSpotServiceabilityAgentParameter;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotSAKeyValueResult;

public class Demo {

    private static final HotSpotServiceabilityAgentManager hotSpotSAManager = 
            Jemstone.getHotSpotServiceabilityAgentManager();
    
    public static void main(String[] args) {
        System.out.println(hotSpotSAManager.details());

        // ///////////////////////////////////////////////////////////////////////////////

        System.out.println(hotSpotSAManager.getCompressedReferences());

        // ///////////////////////////////////////////////////////////////////////////////
        
        stackTraceDemo();

        // ///////////////////////////////////////////////////////////////////////////////

        System.out.println(hotSpotSAManager.executeOnHotSpotSA(HeapSummaryWorker.class));
        
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
        System.out.println(hotSpotSAManager.getStackTraces(
                new HashSet<String>(Arrays.asList(Thread.currentThread().getName()))));
    }

    @SuppressWarnings("serial")
    // Can be executed via "HotSpotServiceabilityAgent.executeOnHotSpotSA(HeapSummaryWorker.class);"
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

}
