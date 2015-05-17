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

package tr.com.serkanozal.jemstone.sa.impl.stacktracer;

import java.util.Set;

import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter;

@SuppressWarnings("serial")
public class HotSpotSAStackTracerParameter implements HotSpotServiceabilityAgentParameter {

    private final Set<String> threadNames;

    public HotSpotSAStackTracerParameter() {
        // No "threadNames" means all threads
        this.threadNames = null;
    }
    
    public HotSpotSAStackTracerParameter(Set<String> threadNames) {
        this.threadNames = threadNames;
    }
    
    public Set<String> getThreadNames() {
        return threadNames;
    }
    
    @Override
    public String toString() {
        return "HotSpotSAStackTracerParameter [" +
                "threadNames=" + threadNames + "]";
    }

}
