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

package tr.com.serkanozal.jemstone.sa.impl.compressedrefs;

import java.lang.reflect.Method;

import sun.jvm.hotspot.memory.Universe;
import sun.jvm.hotspot.runtime.VM;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotServiceabilityAgentUtil;

import static tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter.NoHotSpotServiceabilityAgentParameter;

@SuppressWarnings("serial")
public class HotSpotSACompressedReferencesWorker 
        implements HotSpotServiceabilityAgentWorker<NoHotSpotServiceabilityAgentParameter,  
                                                    HotSpotSACompressedReferencesResult> {

    @Override
    public HotSpotSACompressedReferencesResult run(HotSpotServiceabilityAgentContext context,
                                                   NoHotSpotServiceabilityAgentParameter param) {
        try {
            Class<?> universeClass = HotSpotServiceabilityAgentUtil.getUniverseClass();
            Class<?> vmClass = HotSpotServiceabilityAgentUtil.getVmClass();
            VM vm = HotSpotServiceabilityAgentUtil.getVMInstance();

            Method getKlassOopSizeMethod = null;
            Method isCompressedKlassOopsEnabledMethod = null;
            Method getNarrowKlassBaseMethod = null;
            Method getNarrowKlassShiftMethod = null;

            try {
                getKlassOopSizeMethod = vmClass.getMethod("getKlassPtrSize");
                isCompressedKlassOopsEnabledMethod = vmClass.getMethod("isCompressedKlassPointersEnabled");
                getNarrowKlassBaseMethod = universeClass.getMethod("getNarrowKlassBase");
                getNarrowKlassShiftMethod = universeClass.getMethod("getNarrowKlassShift");
            } catch (NoSuchMethodException e) {
                // There is nothing to do, seems target JVM is not Java 8
            }

            int addressSize = (int) vm.getOopSize();
            int objectAlignment = vm.getObjectAlignmentInBytes();

            int oopSize = vm.getHeapOopSize();
            boolean compressedOopsEnabled = vm.isCompressedOopsEnabled();
            long narrowOopBase = Universe.getNarrowOopBase();
            int narrowOopShift = Universe.getNarrowOopShift();

            /*
             * If compressed klass references is not supported (before Java 8),
             * use compressed oop references values instead of them.
             */

            int klassOopSize = getKlassOopSizeMethod != null ? 
                    (Integer) getKlassOopSizeMethod.invoke(vm) : oopSize;
            boolean compressedKlassOopsEnabled = isCompressedKlassOopsEnabledMethod != null ? 
                    (Boolean) isCompressedKlassOopsEnabledMethod.invoke(vm) : compressedOopsEnabled;
            long narrowKlassBase = getNarrowKlassBaseMethod != null ? 
                    (Long) getNarrowKlassBaseMethod.invoke(null) : narrowOopBase;
            int narrowKlassShift = getNarrowKlassShiftMethod != null ? 
                    (Integer) getNarrowKlassShiftMethod.invoke(null) : narrowOopShift;

            return new HotSpotSACompressedReferencesResult(addressSize, objectAlignment, 
                    oopSize, compressedOopsEnabled, narrowOopBase, narrowOopShift, 
                    klassOopSize, compressedKlassOopsEnabled, narrowKlassBase, narrowKlassShift);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

}
