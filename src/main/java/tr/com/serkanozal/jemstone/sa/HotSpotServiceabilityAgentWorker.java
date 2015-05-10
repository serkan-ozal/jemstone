package tr.com.serkanozal.jemstone.sa;

import java.io.Serializable;

/**
 * <p>
 * Interface for workers which do some stuff via HotSpot Serviceability Agent
 * API on HotSpot internals.
 * </p>
 * 
 * <p>
 * {@link HotSpotServiceabilityAgentWorker} implementations must be fully
 * (including its fields) serializable. So if there is any field will not be
 * serialized, it must be ignored or serialization logic must be customized.
 * Please see <a href="www.oracle.com/technetwork/articles/java/javaserial-1536170.html">here</a>
 * for more details.
 * </p>
 *
 * @param <P> type of the {@link HotSpotServiceabilityAgentParameter} parameter
 * @param <R> type of the {@link HotSpotServiceabilityAgentResult} result
 *
 * @see HotSpotServiceabilityAgentResult
 * 
 * @author Serkan Ozal
 */
public interface HotSpotServiceabilityAgentWorker<
            P extends HotSpotServiceabilityAgentParameter,
            R extends HotSpotServiceabilityAgentResult>
        extends Serializable {

    /**
     * Runs {@link HotSpotServiceabilityAgentWorker}'s own logic over 
     * HotSpot Serviceability Agent.
     * 
     * @param context the context to hold the required HotSpot SA instances to be used
     * @param param   the {@link HotSpotServiceabilityAgentParameter} instance 
     *                to be used by this worker as parameter 
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result
     */
    R run(HotSpotServiceabilityAgentContext context, P param);

}
