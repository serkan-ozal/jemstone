package tr.com.serkanozal.jemstone.sa.impl.compressedrefs;

import java.lang.reflect.Method;

import sun.jvm.hotspot.memory.Universe;
import sun.jvm.hotspot.runtime.VM;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotServiceabilityAgentUtil;

/**
 * {@link HotSpotServiceabilityAgentWorker} implementation to find compressed
 * reference informations.
 * 
 * @author Serkan Ozal
 */
@SuppressWarnings("serial")
public class HotSpotSACompressedReferencesWorker 
        implements HotSpotServiceabilityAgentWorker<HotSpotSACompressedReferencesResult> {

    @Override
    public HotSpotSACompressedReferencesResult run(HotSpotServiceabilityAgentContext context) {
        try {
            Class<?> universeClass = HotSpotServiceabilityAgentUtil.getUniverseClass();
            Class<?> vmClass = HotSpotServiceabilityAgentUtil.getVmClass();
            VM vm = HotSpotServiceabilityAgentUtil.getVMInstance();

            Method getKlassOopSizeMethod = null;
            Method isCompressedKlassOopsEnabledMethod = null;
            Method getNarrowKlassBaseMethod = null;
            Method getNarrowKlassShiftMethod = null;

            try {
                getKlassOopSizeMethod = vmClass.getMethod("getKlassPtrSize");
                isCompressedKlassOopsEnabledMethod = vmClass.getMethod("isCompressedKlassPointersEnabled");
                getNarrowKlassBaseMethod = universeClass.getMethod("getNarrowKlassBase");
                getNarrowKlassShiftMethod = universeClass.getMethod("getNarrowKlassShift");
            } catch (NoSuchMethodException e) {
                // There is nothing to do, seems target JVM is not Java 8
            }

            int addressSize = (int) vm.getOopSize();
            int objectAlignment = vm.getObjectAlignmentInBytes();

            int oopSize = vm.getHeapOopSize();
            boolean compressedOopsEnabled = vm.isCompressedOopsEnabled();
            long narrowOopBase = Universe.getNarrowOopBase();
            int narrowOopShift = Universe.getNarrowOopShift();

            /*
             * If compressed klass references is not supported (before Java 8),
             * use compressed oop references values instead of them.
             */

            int klassOopSize = getKlassOopSizeMethod != null ? 
                    (Integer) getKlassOopSizeMethod.invoke(vm) : oopSize;
            boolean compressedKlassOopsEnabled = isCompressedKlassOopsEnabledMethod != null ? 
                    (Boolean) isCompressedKlassOopsEnabledMethod.invoke(vm) : compressedOopsEnabled;
            long narrowKlassBase = getNarrowKlassBaseMethod != null ? 
                    (Long) getNarrowKlassBaseMethod.invoke(null) : narrowOopBase;
            int narrowKlassShift = getNarrowKlassShiftMethod != null ? 
                    (Integer) getNarrowKlassShiftMethod.invoke(null) : narrowOopShift;

            return new HotSpotSACompressedReferencesResult(addressSize, objectAlignment, 
                    oopSize, compressedOopsEnabled, narrowOopBase, narrowOopShift, 
                    klassOopSize, compressedKlassOopsEnabled, narrowKlassBase, narrowKlassShift);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

}
