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

import java.io.Serializable;

/**
 * <p>
 * Interface for parameters of {@link HotSpotServiceabilityAgentWorker#run()} call.
 * It is designed to hold all parameters under a hierarchy.
 * </p>
 * 
 * <p>
 * {@link HotSpotServiceabilityAgentParameter} implementations must be fully
 * (including its fields) serializable. So if there is any field will not be
 * serialized, it must be ignored or serialization logic must be customized.
 * Please see <a href="www.oracle.com/technetwork/articles/java/javaserial-1536170.html">here</a>
 * for more details.
 * </p>
 *
 * @author Serkan Ozal
 */
public interface HotSpotServiceabilityAgentParameter extends Serializable {

    @SuppressWarnings("serial")
    final class NoHotSpotServiceabilityAgentParameter implements HotSpotServiceabilityAgentParameter {
        
    }
    
    NoHotSpotServiceabilityAgentParameter VOID = new NoHotSpotServiceabilityAgentParameter();
    
}
