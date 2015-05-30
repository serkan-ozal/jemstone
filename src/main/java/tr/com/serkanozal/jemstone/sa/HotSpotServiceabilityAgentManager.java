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

import java.util.Collection;

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
     * Get the requested {@link HotSpotServiceabilityAgentPlugin} with the specified <code>id</code>.
     * 
     * @return the requested {@link HotSpotServiceabilityAgentPlugin} has the specified <code>id</code>
     */
    @SuppressWarnings("rawtypes")
    <P extends HotSpotServiceabilityAgentPlugin>
    P getPlugin(String id);
    
    /**
     * Registers the specified {@link HotSpotServiceabilityAgentPlugin}.
     * 
     * @param plugin the {@link HotSpotServiceabilityAgentPlugin} instance to be registered
     */
    @SuppressWarnings("rawtypes")
    <P extends HotSpotServiceabilityAgentPlugin>
    void registerPlugin(P plugin);
    
    /**
     * Deregisters the existing {@link HotSpotServiceabilityAgentPlugin} 
     * specified with the <code>id</code>.
     * 
     * @param id the id of {@link HotSpotServiceabilityAgentPlugin} to be deregistered
     */
    void deregisterPlugin(String id);
    
    /**
     * Lists registered {@link HotSpotServiceabilityAgentPlugin} implementations.
     * 
     * @return the registered {@link HotSpotServiceabilityAgentPlugin} implementations
     */
    @SuppressWarnings("rawtypes")
    <P extends HotSpotServiceabilityAgentPlugin>
    Collection<P> listPlugins();
    
    /**
     * Runs the {@link HotSpotServiceabilityAgentPlugin} has specified <code>id</code>.
     * 
     * @param id the id of {@link HotSpotServiceabilityAgentPlugin} to be run
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of plugin execution
     */
    <R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id);
    
    /**
     * Runs the {@link HotSpotServiceabilityAgentPlugin} has specified <code>id</code>.
     * 
     * @param id        the id of {@link HotSpotServiceabilityAgentPlugin} to be run
     * @param config    the execution configuration
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of plugin execution
     */
    <R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id, HotSpotServiceabilityAgentConfig config);

    /**
     * Runs the {@link HotSpotServiceabilityAgentPlugin} has specified <code>id</code> 
     * with given {@link HotSpotServiceabilityAgentParameter}.
     * 
     * @param id    the id of {@link HotSpotServiceabilityAgentPlugin} to be run
     * @param param the {@link HotSpotServiceabilityAgentParameter} instance to be used as parameter 
     *              by the plugin
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of plugin execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id, P param);
    
    /**
     * Runs the {@link HotSpotServiceabilityAgentPlugin} has specified <code>id</code> 
     * with given {@link HotSpotServiceabilityAgentParameter}.
     * 
     * @param id        the id of {@link HotSpotServiceabilityAgentPlugin} to be run
     * @param param     the {@link HotSpotServiceabilityAgentParameter} instance to be used as parameter 
     *                  by the plugin
     * @param config    the execution configuration             
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of plugin execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id, P param, HotSpotServiceabilityAgentConfig config);
    
    /**
     * Runs the {@link HotSpotServiceabilityAgentPlugin} has specified <code>id</code>
     * with given <code>args</code>.
     * 
     * @param id    the id of {@link HotSpotServiceabilityAgentPlugin} to be run
     * @param args  the arguments these are used as parameter by plugin
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of plugin execution
     */
    <R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id, String[] args);
    
    /**
     * Runs the given {@link HotSpotServiceabilityAgentPlugin}.
     * 
     * @param plugin the {@link HotSpotServiceabilityAgentPlugin} to be run
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of plugin execution
     */
    <P extends HotSpotServiceabilityAgentParameter, 
     R extends HotSpotServiceabilityAgentResult,
     W extends HotSpotServiceabilityAgentWorker<P, R>>
    R runPlugin(HotSpotServiceabilityAgentPlugin<P, R, W> plugin);
    
    /**
     * Runs the given {@link HotSpotServiceabilityAgentPlugin} with specified 
     * {@link HotSpotServiceabilityAgentParameter}.
     * 
     * @param plugin the {@link HotSpotServiceabilityAgentPlugin} to be run
     * @param param  the {@link HotSpotServiceabilityAgentParameter} instance to be used as parameter 
     *               by the plugin
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of plugin execution
     */
    <P extends HotSpotServiceabilityAgentParameter, 
     R extends HotSpotServiceabilityAgentResult,
     W extends HotSpotServiceabilityAgentWorker<P, R>>
    R runPlugin(HotSpotServiceabilityAgentPlugin<P, R, W> plugin, P param);
    
    /**
     * Runs the given {@link HotSpotServiceabilityAgentPlugin} with given <code>args</code>.
     * 
     * @param plugin the {@link HotSpotServiceabilityAgentPlugin} to be run
     * @param args   the arguments these are used as parameter by plugin
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of plugin execution
     */
    <P extends HotSpotServiceabilityAgentParameter, 
     R extends HotSpotServiceabilityAgentResult,
     W extends HotSpotServiceabilityAgentWorker<P, R>> 
    R runPlugin(HotSpotServiceabilityAgentPlugin<P, R, W> plugin, String[] args);
    
    /**
     * Gets the compressed references information as of current process
     * {@link HotSpotSACompressedReferencesResult} instance.
     * 
     * @return the compressed references information as
     *         {@link HotSpotSACompressedReferencesResult} instance
     */
    HotSpotSACompressedReferencesResult getCompressedReferences();
    
    /**
     * Gets the compressed references information of target process 
     * specified with <code>processId</code> as
     * {@link HotSpotSACompressedReferencesResult} instance.
     * 
     * @param processId id of target process to attach and 
     *                  get compressed references information of it  
     * @return the compressed references information as
     *         {@link HotSpotSACompressedReferencesResult} instance
     */
    HotSpotSACompressedReferencesResult getCompressedReferences(int processId);
    
    /**
     * Gets detailed stack trace informations of current thread.
     * 
     * @return the detailed stack trace informations
     *         {@link HotSpotSAStackTracerResult} instance
     */
    HotSpotSAStackTracerResult getStackTracesOfCurrentThread();
    
    /**
     * Gets detailed stack trace informations of given threads.
     * 
     * @param threadNames the names of the threads whose stack trace 
     *                    information will be retrieved. Empty value means all threads.
     * @return the detailed stack trace informations
     *         {@link HotSpotSAStackTracerResult} instance
     */
    HotSpotSAStackTracerResult getStackTraces(String ... threadNames);
    
    /**
     * Gets detailed stack trace informations of given threads.
     * 
     * @param processId   id of target process to attach and run on it      
     * @param threadNames the names of the threads whose stack trace 
     *                    information will be retrieved. Empty value means all threads.      
     * @return the detailed stack trace informations
     *         {@link HotSpotSAStackTracerResult} instance
     */
    HotSpotSAStackTracerResult getStackTraces(int processId, String ... threadNames);
    
    /**
     * Gives details about HotSpot Serviceability Agent support.
     * 
     * @return the string representation of HotSpot Serviceability Agent support
     */
    String details();
    
}
