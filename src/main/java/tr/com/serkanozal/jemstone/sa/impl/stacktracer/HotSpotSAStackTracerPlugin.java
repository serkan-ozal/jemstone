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

import java.util.HashSet;
import java.util.Set;

import tr.com.serkanozal.jemstone.Jemstone;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentConfig;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentPlugin;

public class HotSpotSAStackTracerPlugin 
        implements HotSpotServiceabilityAgentPlugin<
            HotSpotSAStackTracerParameter,  
            HotSpotSAStackTracerResult,
            HotSpotSAStackTracerWorker> {

    public static final String PLUGIN_ID = "HotSpot Stack Tracer";
    private static final JavaVersion[] SUPPORTED_JAVA_VERSION = 
            new JavaVersion[] { 
                JavaVersion.ALL_VERSIONS 
            };
    
    @Override
    public String getId() {
        return PLUGIN_ID;
    }
    
    @Override
    public String getUsage() {
        return "Usage: " + Jemstone.class.getName() + " " + 
                    "(-i " + PLUGIN_ID + " [thread_name]*)" + 
                    " | " + 
                    "(-p " + getClass().getName() + " [thread_name]*)" + 
               "\n" +
               "- empty thread_name(s) means that use all threads";
    }

    @Override
    public JavaVersion[] getSupportedJavaVersions() {
        return SUPPORTED_JAVA_VERSION;
    }

    @Override
    public HotSpotSAStackTracerWorker getWorker() {
        return new HotSpotSAStackTracerWorker();
    }

    @Override
    public HotSpotSAStackTracerParameter getParamater(String[] args) {
        Set<String> threadNames = null;
        if (args != null && args.length > 0) {
            threadNames = new HashSet<String>();
            for (String arg : args) {
                threadNames.add(arg);
            }
        }
        return new HotSpotSAStackTracerParameter(threadNames);
    }

    @Override
    public HotSpotServiceabilityAgentConfig getConfig() {
        // Use default configuration, so just returns "null"
        return null;
    }

}
