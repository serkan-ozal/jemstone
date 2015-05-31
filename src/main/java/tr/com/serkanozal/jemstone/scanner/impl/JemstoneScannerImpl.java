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

package tr.com.serkanozal.jemstone.scanner.impl;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import tr.com.serkanozal.jemstone.scanner.JemstoneScanner;
import tr.com.serkanozal.jemstone.util.ClasspathUtil;

/**
 * {@link org.reflections.Reflections} based {@link JemstoneScanner} implementation.
 * 
 * @author Serkan Ozal
 */
public class JemstoneScannerImpl implements JemstoneScanner {

    private final Reflections reflections = 
            new Reflections(
                    new ConfigurationBuilder().
                            setUrls(ClasspathUtil.getClasspathUrls()));

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotationClass) {
        return reflections.getTypesAnnotatedWith(annotationClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Set<Class<? extends T>> getSubTypedClasses(Class<T> superClass) {
        return reflections.getSubTypesOf(superClass);
    }

}
