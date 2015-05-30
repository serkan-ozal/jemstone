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
import tr.com.serkanozal.jemstone.sa.HotSpotSAPluginInvalidArgumentException;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentConfig;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentPlugin;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResultProcessor;

public class HotSpotSAStackTracerPlugin 
        implements HotSpotServiceabilityAgentPlugin<
            HotSpotSAStackTracerParameter,  
            HotSpotSAStackTracerResult,
            HotSpotSAStackTracerWorker> {

    public static final String PLUGIN_ID = "HotSpot_Stack_Tracer";
    
    private static final JavaVersion[] SUPPORTED_JAVA_VERSION = 
            new JavaVersion[] { 
                JavaVersion.ALL_VERSIONS 
            };
    private static final String USAGE = 
            Jemstone.class.getName() + " " + 
                "(-i " + "\"" + PLUGIN_ID + "\"" + " <process_id> [thread_name]*)" + 
                " | " + 
                "(-p " + HotSpotSAStackTracerPlugin.class.getName() + " <process_id> [thread_name]*)" + 
           "\n" +
           "\t- empty thread_name(s) means that use all threads";
    
    private int processId = HotSpotServiceabilityAgentConfig.CONFIG_NOT_SET;
    
    @Override
    public String getId() {
        return PLUGIN_ID;
    }
    
    @Override
    public String getUsage() {
        return USAGE;
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
        if (args == null || args.length == 0) {
            throw new HotSpotSAPluginInvalidArgumentException(
                    PLUGIN_ID, "Process id is required");
        }
        processId = Integer.parseInt(args[0]);
        Set<String> threadNames = null;
        if (args.length > 1) {
            threadNames = new HashSet<String>();
            for (int i = 1; i < args.length; i++) {
                threadNames.add(args[i]);
            }
        }
        return new HotSpotSAStackTracerParameter(threadNames);
    }

    @Override
    public HotSpotServiceabilityAgentConfig getConfig() {
        if (processId != HotSpotServiceabilityAgentConfig.CONFIG_NOT_SET) {
            HotSpotServiceabilityAgentConfig config = new HotSpotServiceabilityAgentConfig();
            // Only set "process id" and don't touch others ("pipeline size", "timeout").
            // So default configurations will be used for them ("pipeline size", "timeout").
            config.setProcessId(processId);
            return config;
        } else {
            // Use default configuration, so just returns "null"
            return null;
        }
    }
    
    @Override
    public HotSpotServiceabilityAgentResultProcessor<HotSpotSAStackTracerResult> getResultProcessor() {
        // Use default result processor (print to console)
        return null;
    }

}
