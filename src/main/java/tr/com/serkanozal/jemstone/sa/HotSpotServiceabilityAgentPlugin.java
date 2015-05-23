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

package tr.com.serkanozal.jemstone.sa;

/**
 * Interface for plugins on top of HotSpot Serviceability Agent support.
 * {@link HotSpotServiceabilityAgentPlugin} is the structured, packaged 
 * and well defined way of {@link HotSpotServiceabilityAgentWorker} implementations. 
 * 
 * @see HotSpotServiceabilityAgentParameter
 * @see HotSpotServiceabilityAgentResult
 * @see HotSpotServiceabilityAgentWorker
 * 
 * @author Serkan Ozal
 */
public interface HotSpotServiceabilityAgentPlugin
        <P extends HotSpotServiceabilityAgentParameter, 
         R extends HotSpotServiceabilityAgentResult,
         W extends HotSpotServiceabilityAgentWorker<P, R>> {
    
    enum JavaVersion {
        
        JAVA_6(0x01),
        JAVA_7(0x02),
        JAVA_8(0x04),
        ALL_VERSIONS(0x01 | 0x02 | 0x04);
        
        int code;
        
        JavaVersion(int code) {
            this.code = code;
        }
        
        public int getCode() {
            return code;
        }
        
    }
    
    /**
     * Gets the id of this {@link HotSpotServiceabilityAgentPlugin}. Must be unique.
     * 
     * @return the id of this {@link HotSpotServiceabilityAgentPlugin}
     */
    String getId();
    
    /**
     * Gets the usage of this {@link HotSpotServiceabilityAgentPlugin}. Must be unique.
     * 
     * @return the usage of this {@link HotSpotServiceabilityAgentPlugin}
     */
    String getUsage();
    
    /**
     * Gets the supported Java versions to be run.
     * 
     * @return the supported Java versions. 
     *         <code>null</code> and empty array also means {@link JavaVersion#ALL_VERSIONS}.
     * @see JavaVersion
     */
    JavaVersion[] getSupportedJavaVersions();
    
    /**
     * Gets the {@link HotSpotServiceabilityAgentWorker} to be run.
     * 
     * @return the {@link HotSpotServiceabilityAgentWorker} to be run
     */
    W getWorker();
    
    /**
     * Creates and gets the {@link HotSpotServiceabilityAgentParameter} from arguments.
     * This method is used for using this {@link HotSpotServiceabilityAgentPlugin} 
     * from command-line for getting required parameter to run 
     * {@link HotSpotServiceabilityAgentWorker}.
     * 
     * @param args the arguments for creating {@link HotSpotServiceabilityAgentParameter}
     * @return the created {@link HotSpotServiceabilityAgentParameter} from arguments 
     *         if there is any specific parameter to run {@link HotSpotServiceabilityAgentWorker},
     *         otherwise <code>null</code>
     */
    P getParamater(String[] args);
    
    /**
     * Gets the configuration of {@link HotSpotServiceabilityAgentWorker} to be run.
     * 
     * @return the {@link HotSpotServiceabilityAgentConfig} instance 
     *         if there is any specific configuration, otherwise <code>null</code>
     */
    HotSpotServiceabilityAgentConfig getConfig();
    
    /**
     * Gets the {@link HotSpotServiceabilityAgentResultProcessor} instance 
     * to process output of {@link HotSpotServiceabilityAgentWorker} execution.
     * 
     * @return the {@link HotSpotServiceabilityAgentResultProcessor} instance 
     *         if there is any specific result processing requirement, 
     *         otherwise <code>null</code> to indicate that use default result processor 
     *         which prints the result to standard output (console).
     */
    HotSpotServiceabilityAgentResultProcessor<R> getResultProcessor();

}
