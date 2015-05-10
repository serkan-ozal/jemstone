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
 * @param <R>
 *            type of the {@link HotSpotServiceabilityAgentResult}
 *
 * @see HotSpotServiceabilityAgentResult
 * 
 * @author Serkan Ozal
 */
public interface HotSpotServiceabilityAgentWorker<R extends HotSpotServiceabilityAgentResult>
        extends Serializable {

    /**
     * Runs {@link HotSpotServiceabilityAgentWorker}'s own logic over HotSpot
     * Serviceability Agent.
     * 
     * @param context
     *            the context to hold the required HotSpot SA instances to be
     *            used
     * 
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result
     */
    R run(HotSpotServiceabilityAgentContext context);

}
