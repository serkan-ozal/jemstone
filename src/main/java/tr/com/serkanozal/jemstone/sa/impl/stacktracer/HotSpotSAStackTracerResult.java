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

import java.util.HashMap;
import java.util.Map;

import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResult;

@SuppressWarnings("serial")
public class HotSpotSAStackTracerResult implements HotSpotServiceabilityAgentResult {

    private final Map<String, String> stackTraces = new HashMap<String, String>();

    public HotSpotSAStackTracerResult() {

    }

    public Map<String, String> getStackTraces() {
        return stackTraces;
    }
    
    public String getStackTrace(String threadName) {
        return stackTraces.get(threadName);
    }
    
    public void addStackTrace(String threadName, String stackTrace) {
        stackTraces.put(threadName, stackTrace);
    }
     
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HotSpotSAStackTracerResult [stackTraces=");
        for (Map.Entry<String, String> entry : stackTraces.entrySet()) {
            sb
                .append("\n")
                .append("- Thread Name: ")
                .append(entry.getKey())
                .append("\n")
                .append(entry.getValue());
        }
        sb.append("]");
        return sb.toString();
    }

}
