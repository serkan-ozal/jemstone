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

import java.util.Set;

import tr.com.serkanozal.jemstone.sa.impl.compressedrefs.HotSpotSACompressedReferencesResult;
import tr.com.serkanozal.jemstone.sa.impl.compressedrefs.HotSpotSACompressedReferencesWorker;
import tr.com.serkanozal.jemstone.sa.impl.stacktracer.HotSpotSAStackTracerResult;

/**
 * Interface for managing HotSpot SA API based stuff.
 * 
 * @see HotSpotServiceabilityAgentWorker
 * @see HotSpotServiceabilityAgentResult
 * 
 * @see HotSpotSACompressedReferencesWorker
 * @see HotSpotSACompressedReferencesResult
 *
 * @author Serkan Ozal
 */
public interface HotSpotServiceabilityAgentManager {

    int ATTACH_TO_CURRENT_PROCESS = -1;
    
    /**
     * Returns <code>true</code> if HotSpot Serviceability Agent support is enable, 
     * otherwise <code>false</code>.
     * 
     * @return the enable state of HotSpot Serviceability Agent support
     */
    boolean isEnable();
    
    /**
     * Executes given typed {@link HotSpotServiceabilityAgentWorker} on HotSpot
     * agent process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param workerClass the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotSpotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass);
    
    /**
     * Executes given typed {@link HotSpotServiceabilityAgentWorker} on HotSpot
     * agent process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param workerClass the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param       the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotSpotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass, P param);
    
    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker);
    
    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param  the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker, P param);
    
    /**
     * Executes given typed {@link HotSpotServiceabilityAgentWorker} on HotSpot
     * agent process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     *
     * @param workerClass           the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param timeoutInMsecs        the timeout in milliseconds to wait at most for terminating
     *                              connection between current process and HotSpot agent process
     * @param pipelineSizeInBytes   the maximum size of pipeline in bytes for getting result 
     *                              from HotSpot SA process
     * @param processId             id of target process to attach and run on it                                                
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotspotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass, 
                int timeoutInMsecs, int pipelineSizeInBytes, int processId);
    
    /**
     * Executes given typed {@link HotSpotServiceabilityAgentWorker} on HotSpot
     * agent process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     *
     * @param workerClass           the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param                 the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @param timeoutInMsecs        the timeout in milliseconds to wait at most for terminating
     *                              connection between current process and HotSpot agent process
     * @param pipelineSizeInBytes   the maximum size of pipeline in bytes for getting result 
     *                              from HotSpot SA process 
     * @param processId             id of target process to attach and run on it  
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotspotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass, 
                P param, int timeoutInMsecs, int pipelineSizeInBytes, int processId);

    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker                the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param timeoutInMsecs        the timeout in milliseconds to wait at most for terminating
     *                              connection between current process and HotSpot agent process
     * @param pipelineSizeInBytes   the maximum size of pipeline in bytes for getting result 
     *                              from HotSpot SA process 
     * @param processId             id of target process to attach and run on it                               
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker,  
                int timeoutInMsecs, int pipelineSizeInBytes, int processId);
    
    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker                the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param                 the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @param timeoutInMsecs        the timeout in milliseconds to wait at most for terminating
     *                              connection between current process and HotSpot agent process
     * @param pipelineSizeInBytes   the maximum size of pipeline in bytes for getting result 
     *                              from HotSpot SA process 
     * @param processId             id of target process to attach and run on it                             
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker, P param, 
                int timeoutInMsecs, int pipelineSizeInBytes, int processId);
    
    /**
     * Gets the compressed references information as
     * {@link HotSpotSACompressedReferencesResult} instance.
     * 
     * @return the compressed references information as
     *         {@link HotSpotSACompressedReferencesResult} instance
     */
    HotSpotSACompressedReferencesResult getCompressedReferences();
    
    /**
     * Gets detailed stack trace informations of given threads.
     * 
     * @param threadNames the names of the threads whose stack trace 
     *                    information will be retrieved
     * @return the detailed stack trace informations
     *         {@link HotSpotSAStackTracerResult} instance
     */
    HotSpotSAStackTracerResult getStackTraces(Set<String> threadNames);
    
    /**
     * Gets detailed stack trace informations of given threads.
     * 
     * @param threadNames the names of the threads whose stack trace 
     *                    information will be retrieved
     * @param processId   id of target process to attach and run on it                      
     * @return the detailed stack trace informations
     *         {@link HotSpotSAStackTracerResult} instance
     */
    HotSpotSAStackTracerResult getStackTraces(Set<String> threadNames, int processId);
    
    /**
     * Gives details about HotSpot Serviceability Agent support.
     * 
     * @return the string representation of HotSpot Serviceability Agent support
     */
    String details();
    
}
