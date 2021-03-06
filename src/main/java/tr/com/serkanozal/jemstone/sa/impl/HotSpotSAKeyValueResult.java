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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResult;

@SuppressWarnings("serial")
public class HotSpotSAKeyValueResult implements HotSpotServiceabilityAgentResult {
    
    private final Map<String, Serializable> keyValueMap = new HashMap<String, Serializable>();
    
    public HotSpotSAKeyValueResult addResult(String key, Serializable value) {
        keyValueMap.put(key, value);
        return this;
    }
    
    public Serializable getResult(String key) {
        return keyValueMap.get(key);
    }
    
    public Map<String, Serializable> getResults() {
        return Collections.unmodifiableMap(keyValueMap);
    }

    @Override
    public String toString() {
        return "HotSpotSAKeyValueResult [" + keyValueMap + "]";
    }
    
}
