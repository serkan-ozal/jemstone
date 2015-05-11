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

import tr.com.serkanozal.jemstone.sa.impl.compressedrefs.HotSpotSACompressedReferencesResult;
import tr.com.serkanozal.jemstone.sa.impl.compressedrefs.HotSpotSACompressedReferencesWorker;
import tr.com.serkanozal.jemstone.sa.impl.instancecount.HotSpotSAInstanceCountResult;

/**
 *  Interface for managing HotSpot SA API based stuff.
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
     * @param workerClass    the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param timeoutInMsecs the timeout in milliseconds to wait at most for terminating
     *                       connection between current process and HotSpot agent process.
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotspotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass, 
                int timeoutInMsecs);
    
    /**
     * Executes given typed {@link HotSpotServiceabilityAgentWorker} on HotSpot
     * agent process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     *
     * @param workerClass    the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param          the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @param timeoutInMsecs the timeout in milliseconds to wait at most for terminating
     *                       connection between current process and HotSpot agent process.
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotspotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass, 
                P param, int timeoutInMsecs);

    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker         the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param timeoutInMsecs the timeout in milliseconds to wait at most for terminating
     *                       connection between current process and HotSpot agent process.
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker,  int timeoutInMsecs);
    
    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker         the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param          the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @param timeoutInMsecs the timeout in milliseconds to wait at most for terminating
     *                       connection between current process and HotSpot agent process.
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
        R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker, P param, int timeoutInMsecs);
    
    /**
     * Gets the compressed references information as
     * {@link HotSpotSACompressedReferencesResult} instance.
     * 
     * @return the compressed references information as
     *         {@link HotSpotSACompressedReferencesResult} instance
     */
    HotSpotSACompressedReferencesResult getCompressedReferences();
    
    /**
     * Calculates the instance count of given {@link Class} and return it in 
     * {@link HotSpotSAInstanceCountResult} instance.
     * 
     * @param clazz {@link Class} whose instance count will be calculated
     * 
     * @return the instance count information as
     *         {@link HotSpotSAInstanceCountResult} instance
     */
    HotSpotSAInstanceCountResult getInstanceCount(Class<?> clazz);
    
    /**
     * Gives details about HotSpot Serviceability Agent support.
     * 
     * @return the string representation of HotSpot Serviceability Agent support
     */
    String details();
    
}
