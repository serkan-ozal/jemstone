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

package tr.com.serkanozal.jemstone.util;

import java.io.Serializable;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ReflectionUtilTest {

    @SuppressWarnings("serial")
    public static class SampleClass implements Comparable<SampleClass>, Serializable {

        private int i;
        private String s;

        public SampleClass() {

        }

        public SampleClass(int i) {
            this.i = i;
        }

        public SampleClass(String s) {
            this.s = s;
        }

        public SampleClass(int i, String s) {
            this.i = i;
            this.s = s;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        @Override
        public int compareTo(SampleClass o) {
            if (i < o.i) {
                return -1;
            } else if (i > o.i) {
                return +1;
            } else {
                return 0;
            }
        }
    }

    @Test
    public void getField() {
        Assert.assertEquals("i", ReflectionUtil.getField(SampleClass.class, "i").getName());
    }

    @Test
    public void getAllFields() {
        Assert.assertEquals(2, ReflectionUtil.getAllFields(SampleClass.class).size());
    }

    @Test
    public void isPrimitiveType() {
        Assert.assertTrue(ReflectionUtil.isPrimitiveType(int.class));
        Assert.assertTrue(ReflectionUtil.isPrimitiveType(Integer.class));
        Assert.assertFalse(ReflectionUtil.isPrimitiveType(SampleClass.class));
    }

    @Test
    public void isNonPrimitiveType() {
        Assert.assertFalse(ReflectionUtil.isNonPrimitiveType(int.class));
        Assert.assertFalse(ReflectionUtil.isNonPrimitiveType(Integer.class));
        Assert.assertTrue(ReflectionUtil.isNonPrimitiveType(SampleClass.class));
    }

    @Test
    public void isComplexType() {
        Assert.assertFalse(ReflectionUtil.isComplexType(int.class));
        Assert.assertTrue(ReflectionUtil.isComplexType(SampleClass.class));
    }

    @Test
    public void isCollectionType() {
        Assert.assertFalse(ReflectionUtil.isCollectionType(String.class));
        Assert.assertTrue(ReflectionUtil.isCollectionType(List.class));
    }

    @Test
    public void getNonPrimitiveType() {
        Assert.assertEquals(Boolean.class,
                ReflectionUtil.getNonPrimitiveType(boolean.class));
        Assert.assertEquals(Byte.class,
                ReflectionUtil.getNonPrimitiveType(byte.class));
        Assert.assertEquals(Character.class,
                ReflectionUtil.getNonPrimitiveType(char.class));
        Assert.assertEquals(Short.class,
                ReflectionUtil.getNonPrimitiveType(short.class));
        Assert.assertEquals(Integer.class,
                ReflectionUtil.getNonPrimitiveType(int.class));
        Assert.assertEquals(Float.class,
                ReflectionUtil.getNonPrimitiveType(float.class));
        Assert.assertEquals(Long.class,
                ReflectionUtil.getNonPrimitiveType(long.class));
        Assert.assertEquals(Double.class,
                ReflectionUtil.getNonPrimitiveType(double.class));
        Assert.assertEquals(String.class,
                ReflectionUtil.getNonPrimitiveType(String.class));
    }

    @Test
    public void isDecimalType() {
        Assert.assertTrue(ReflectionUtil.isDecimalType(byte.class));
        Assert.assertTrue(ReflectionUtil.isDecimalType(short.class));
        Assert.assertTrue(ReflectionUtil.isDecimalType(int.class));
        Assert.assertTrue(ReflectionUtil.isDecimalType(long.class));
        Assert.assertFalse(ReflectionUtil.isCollectionType(double.class));
    }

}
