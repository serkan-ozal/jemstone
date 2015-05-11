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

package tr.com.serkanozal.jemstone.sa.impl.instancecount;

import sun.jvm.hotspot.oops.HeapVisitor;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.oops.Oop;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;

@SuppressWarnings("serial")
public class HotSpotSAInstanceCountWorker 
        implements HotSpotServiceabilityAgentWorker<HotSpotSAInstanceCountParameter,  
                                                    HotSpotSAInstanceCountResult> {

    @Override
    public HotSpotSAInstanceCountResult run(HotSpotServiceabilityAgentContext context,
                                            HotSpotSAInstanceCountParameter param) {
        ObjectHeap heap = context.getVM().getObjectHeap();
        ClassHeapVisitor classHeapVisitor = new ClassHeapVisitor(param.getClassName());
        heap.iterate(classHeapVisitor);
        return new HotSpotSAInstanceCountResult(param.getClassName(), classHeapVisitor.oopCount);
    }
    
    private class ClassHeapVisitor implements HeapVisitor {

        private int oopCount = 0;
        private final String className;
        
        private ClassHeapVisitor(String className) {
            this.className = className;
        }
        
        @Override
        public boolean doObj(Oop oop) {
            Klass klass = oop.getKlass();
            String oopClassName = klass.getName().asString().replace("/", ".");
            if (oopClassName.equals(className)) {
                oopCount++;
            }    
            return false;
        }

        @Override
        public void epilogue() {
            
        }

        @Override
        public void prologue(long arg0) {
            
        }
        
    }

}
