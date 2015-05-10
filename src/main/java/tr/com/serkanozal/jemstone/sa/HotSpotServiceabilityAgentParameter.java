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
