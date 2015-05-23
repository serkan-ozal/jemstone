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

package tr.com.serkanozal.jemstone.scanner;

import tr.com.serkanozal.jemstone.scanner.impl.JemstoneScannerImpl;

/**
 * Factory for getting and setting global {@link JemstoneScanner} implementation.
 * 
 * @author Serkan Ozal
 */
public final class JemstoneScannerFactory {
	
	private static JemstoneScanner jemstoneScanner = new JemstoneScannerImpl();
	
	private JemstoneScannerFactory() {
		
	}
	
	/**
	 * Gets the global {@link JemstoneScanner} implementation.
	 * 
	 * @return the global {@link JemstoneScanner} implementation
	 */
	public static JemstoneScanner getJemstoneScanner() {
        return jemstoneScanner;
    }
	
	/**
	 * Sets the global {@link JemstoneScanner} implementation.
	 * 
	 * @param jemstoneScanner the new global {@link JemstoneScanner} 
	 *                        implementation to be set
	 */
	public static void setJemstoneScanner(JemstoneScanner jemstoneScanner) {
        JemstoneScannerFactory.jemstoneScanner = jemstoneScanner;
    }
	
}
