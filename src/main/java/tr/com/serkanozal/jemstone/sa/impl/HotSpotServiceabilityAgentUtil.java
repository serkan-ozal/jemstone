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

package tr.com.serkanozal.jemstone.sa.impl;

import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.runtime.VM;

/**
 * Hotspot Serviceability Agent utility. It was designed for doing some utility stuff.
 *
 * @author Serkan Ozal
 */
public final class HotSpotServiceabilityAgentUtil {

    public static final String HOTSPOT_AGENT_CLASSNAME = "sun.jvm.hotspot.HotSpotAgent";
    public static final String VM_CLASSNAME = "sun.jvm.hotspot.runtime.VM";
    public static final String UNIVERSE_CLASSNAME = "sun.jvm.hotspot.memory.Universe";

    private HotSpotServiceabilityAgentUtil() {

    }

    public static VM getVMInstance() {
        return VM.getVM();
    }

    public static HotSpotAgent getHotSpotAgentInstance() {
        return new HotSpotAgent();
    }

    public static Class<?> getVmClass() throws ClassNotFoundException {
        return Class.forName(VM_CLASSNAME);
    }

    public static Class<?> getUniverseClass() throws ClassNotFoundException {
        return Class.forName(UNIVERSE_CLASSNAME);
    }

}
