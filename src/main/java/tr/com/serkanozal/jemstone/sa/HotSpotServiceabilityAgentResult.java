package tr.com.serkanozal.jemstone.sa;

import java.io.Serializable;

/**
 * <p>
 * Interface for types of {@link HotSpotServiceabilityAgentWorker#run()} return.
 * It is designed to hold all results under a hierarchy.
 * </p>
 * 
 * <p>
 * {@link HotSpotServiceabilityAgentResult} implementations must be fully
 * (including its fields) serializable. So if there is any field will not be
 * serialized, it must be ignored or serialization logic must be customized.
 * Please see <a href="www.oracle.com/technetwork/articles/java/javaserial-1536170.html">here</a>
 * for more details.
 * </p>
 *
 * @author Serkan Ozal
 */
public interface HotSpotServiceabilityAgentResult extends Serializable {

}
