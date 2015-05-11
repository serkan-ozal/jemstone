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

import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter;

@SuppressWarnings("serial")
public class HotSpotSAInstanceCountParameter implements HotSpotServiceabilityAgentParameter {

    private final String className;

    public HotSpotSAInstanceCountParameter(String className) {
        this.className = className;
    }
    
    public HotSpotSAInstanceCountParameter(Class<?> clazz) {
        this.className = clazz.getName();
    }

    public String getClassName() {
        return className;
    }
    
    @Override
    public String toString() {
        return "HotSpotSAInstanceCountParameter [" +
                "className=" + className + "]";
    }

}
