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
 * Interface for processing {@link HotSpotServiceabilityAgentResult} 
 * as output of {@link HotSpotServiceabilityAgentWorker} execution.
 * 
 * @see HotSpotSACompressedReferencesResult
 *
 * @author Serkan Ozal
 */
public interface HotSpotServiceabilityAgentResultProcessor<R extends HotSpotServiceabilityAgentResult> {

    /**
     * Processes the given {@link HotSpotServiceabilityAgentResult}.
     * 
     * @param result the {@link HotSpotServiceabilityAgentResult} to be processed
     */
    void processResult(R result);
    
}
