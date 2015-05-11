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

package tr.com.serkanozal.jemstone.sa.impl.compressedrefs;

import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResult;

@SuppressWarnings("serial")
public class HotSpotSACompressedReferencesResult implements HotSpotServiceabilityAgentResult {

    private final int addressSize;
    private final int objectAlignment;

    private final int oopSize;
    private final boolean compressedOopsEnabled;
    private final long narrowOopBase;
    private final int narrowOopShift;

    private final int klassOopSize;
    private final boolean compressedKlassOopsEnabled;
    private final long narrowKlassBase;
    private final int narrowKlassShift;

    public HotSpotSACompressedReferencesResult(int addressSize, int objectAlignment, int refSize, 
            boolean compressedRefsEnabled, long narrowBase, int narrowShift) {
        this.addressSize = addressSize;
        this.objectAlignment = objectAlignment;
        this.oopSize = refSize;
        this.compressedOopsEnabled = compressedRefsEnabled;
        this.narrowOopBase = narrowBase;
        this.narrowOopShift = narrowShift;
        this.klassOopSize = refSize;
        this.compressedKlassOopsEnabled = compressedRefsEnabled;
        this.narrowKlassBase = narrowBase;
        this.narrowKlassShift = narrowShift;
    }

    public HotSpotSACompressedReferencesResult(int addressSize, int objectAlignment, 
            int oopSize, boolean compressedOopsEnabled, long narrowOopBase, int narrowOopShift, 
            int klassOopSize, boolean compressedKlassOopsEnabled, long narrowKlassBase, int narrowKlassShift) {
        this.addressSize = addressSize;
        this.objectAlignment = objectAlignment;
        this.oopSize = oopSize;
        this.compressedOopsEnabled = compressedOopsEnabled;
        this.narrowOopBase = narrowOopBase;
        this.narrowOopShift = narrowOopShift;
        this.klassOopSize = klassOopSize;
        this.compressedKlassOopsEnabled = compressedKlassOopsEnabled;
        this.narrowKlassBase = narrowKlassBase;
        this.narrowKlassShift = narrowKlassShift;
    }

    public int getAddressSize() {
        return addressSize;
    }

    public int getObjectAlignment() {
        return objectAlignment;
    }

    public int getOopSize() {
        return oopSize;
    }

    public boolean isCompressedOopsEnabled() {
        return compressedOopsEnabled;
    }

    public long getNarrowOopBase() {
        return narrowOopBase;
    }

    public int getNarrowOopShift() {
        return narrowOopShift;
    }

    public int getKlassOopSize() {
        return klassOopSize;
    }

    public boolean isCompressedKlassOopsEnabled() {
        return compressedKlassOopsEnabled;
    }

    public long getNarrowKlassBase() {
        return narrowKlassBase;
    }

    public int getNarrowKlassShift() {
        return narrowKlassShift;
    }

    @Override
    public String toString() {
        return "HotSpotSACompressedReferencesResult [" +
                "addressSize=" + addressSize + 
                ", objectAlignment=" + objectAlignment +
                ", oopSize=" + oopSize + 
                ", compressedOopsEnabled=" + compressedOopsEnabled + 
                ", narrowOopBase=" + narrowOopBase +
                ", narrowOopShift=" + narrowOopShift + 
                ", klassOopSize=" + klassOopSize + 
                ", compressedKlassOopsEnabled=" + compressedKlassOopsEnabled + 
                ", narrowKlassBase=" + narrowKlassBase + 
                ", narrowKlassShift=" + narrowKlassShift + "]";
    }

}
