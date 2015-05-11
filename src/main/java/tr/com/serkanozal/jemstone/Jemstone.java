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

import java.util.Date;

import sun.jvm.hotspot.gc_interface.CollectedHeap;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentManager;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter.NoHotSpotServiceabilityAgentParameter;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResult;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotServiceabilityAgentManagerImpl;

public class Jemstone {

    private static HotSpotServiceabilityAgentManager hotSpotServiceabilityAgentManager = 
            HotSpotServiceabilityAgentManagerImpl.getInstance();

    public static HotSpotServiceabilityAgentManager getHotSpotServiceabilityAgentManager() {
        return hotSpotServiceabilityAgentManager;
    }

    public static void setHotSpotServiceabilityAgentManager(
            HotSpotServiceabilityAgentManager hotSpotServiceabilityAgentManager) {
        Jemstone.hotSpotServiceabilityAgentManager = hotSpotServiceabilityAgentManager;
    }

    public static void main(String[] args) {
        HotSpotServiceabilityAgentManager hotSpotServiceabilityAgentManager = getHotSpotServiceabilityAgentManager();

        System.out.println(hotSpotServiceabilityAgentManager.details());

        // ///////////////////////////////////////////////////////////////////////////////

        System.out.println(hotSpotServiceabilityAgentManager.getCompressedReferences());

        // ///////////////////////////////////////////////////////////////////////////////

        System.out.println("Before instance creation: " + 
                           hotSpotServiceabilityAgentManager.getInstanceCount(Date.class));
        Date[] array = new Date[1000];
        for (int i = 0; i < array.length; i++) {
            array[i] = new Date();
        }
        System.out.println("After instance creation: " + 
                           hotSpotServiceabilityAgentManager.getInstanceCount(Date.class));

        // ///////////////////////////////////////////////////////////////////////////////

        System.out.println(hotSpotServiceabilityAgentManager.executeOnHotSpotSA(HeapSummaryWorker.class));
    }

    @SuppressWarnings("serial")
    // Can be executed via "HotSpotServiceabilityAgent.executeOnHotSpotSA(HeapSummaryWorker.class);"
    public static class HeapSummaryResult implements HotSpotServiceabilityAgentResult {

        private final long start;
        private final long end;
        private final long capacity;

        public HeapSummaryResult() {
            start = -1;
            end = -1;
            capacity = -1;
        }

        public HeapSummaryResult(long start, long capacity) {
            this.start = start;
            this.end = start + capacity;
            this.capacity = capacity;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public long getCapacity() {
            return capacity;
        }

        @Override
        public String toString() {
            return "HeapSummaryResult [" + 
                    "start=" + "0x" + Long.toHexString(start) + 
                    ", end=" + "0x" + Long.toHexString(end) + 
                    ", capacity=" + capacity + "]";
        }

    }

    @SuppressWarnings("serial")
    public static class HeapSummaryWorker
            implements HotSpotServiceabilityAgentWorker<NoHotSpotServiceabilityAgentParameter, HeapSummaryResult> {

        @Override
        public HeapSummaryResult run(HotSpotServiceabilityAgentContext context,
                NoHotSpotServiceabilityAgentParameter param) {
            CollectedHeap heap = context.getVM().getUniverse().heap();
            long startAddress = Long.parseLong(heap.start().toString().substring(2), 16);
            long capacity = heap.capacity();
            return new HeapSummaryResult(startAddress, capacity);
        }

    }

}
