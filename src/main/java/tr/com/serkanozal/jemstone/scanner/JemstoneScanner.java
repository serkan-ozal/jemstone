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

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Scans and find classes as requested filters (annotations, subtypes, etc ...) at classpath.
 * 
 * @author Serkan Ozal
 */
public interface JemstoneScanner {
	
    /**
     * Scans the classes annotated with the specified <code>annotationClass</code> annotation 
     * at classpath and gets them.
     * 
     * @param annotationClass the annotation that requested classes must tagged with it
     * @return the classes annotated with the specified <code>annotationClass</code> annotation
     */
	Set<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotationClass);
	
	/**
	 * Scans the classes sub-type of the the specified <code>superClass</code> class (or interface)
	 * at classpath and gets them.
	 * 
	 * @param superClass the super class (or interface) that requested classes must be a sub-type of with it
	 * @return the classes sub-type of the the specified <code>superClass</code> class (or interface)
	 */
	<T> Set<Class<? extends T>> getSubTypedClasses(Class<T> superClass);
	
}
