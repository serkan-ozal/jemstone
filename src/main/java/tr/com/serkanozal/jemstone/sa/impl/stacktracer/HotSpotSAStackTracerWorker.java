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

package tr.com.serkanozal.jemstone.sa.impl.stacktracer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.debugger.AddressException;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.debugger.UnalignedAddressException;
import sun.jvm.hotspot.debugger.UnmappedAddressException;
import sun.jvm.hotspot.memory.SystemDictionary;
import sun.jvm.hotspot.oops.ConstantPool;
import sun.jvm.hotspot.oops.Field;
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.oops.LocalVariableTableElement;
import sun.jvm.hotspot.oops.Method;
import sun.jvm.hotspot.oops.Symbol;
import sun.jvm.hotspot.oops.TypeArray;
import sun.jvm.hotspot.runtime.BasicType;
import sun.jvm.hotspot.runtime.JavaThread;
import sun.jvm.hotspot.runtime.JavaVFrame;
import sun.jvm.hotspot.runtime.StackValue;
import sun.jvm.hotspot.runtime.StackValueCollection;
import sun.jvm.hotspot.runtime.Threads;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.utilities.SystemDictionaryHelper;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.util.ReflectionUtil;

@SuppressWarnings("serial")
public class HotSpotSAStackTracerWorker 
        implements HotSpotServiceabilityAgentWorker<HotSpotSAStackTracerParameter,  
                                                    HotSpotSAStackTracerResult> {
    
    private static final String JEMSTONE_HOTSPOT_SA_PACKAGE_PREFIX = "tr.com.serkanozal.jemstone.sa";
    
    // Java type codes
    private static final int JVM_SIGNATURE_BOOLEAN = 'Z';
    private static final int JVM_SIGNATURE_CHAR    = 'C';
    private static final int JVM_SIGNATURE_BYTE    = 'B';
    private static final int JVM_SIGNATURE_SHORT   = 'S';
    private static final int JVM_SIGNATURE_INT     = 'I';
    private static final int JVM_SIGNATURE_LONG    = 'J';
    private static final int JVM_SIGNATURE_FLOAT   = 'F';
    private static final int JVM_SIGNATURE_DOUBLE  = 'D';
    private static final int JVM_SIGNATURE_ARRAY   = '[';
    private static final int JVM_SIGNATURE_CLASS   = 'L';
    
    private static java.lang.reflect.Method getAddressMethod;
    private static java.lang.reflect.Constructor<?> instanceKlassConstructor;

    private long byteArrayBaseOffset;
    private long booleanArrayBaseOffset;
    private long charArrayBaseOffset;
    private long shortArrayBaseOffset;
    private long intArrayBaseOffset;
    private long floatArrayBaseOffset;
    private long longArrayBaseOffset;
    private long doubleArrayBaseOffset;
    private long objectArrayBaseOffset;
    
    private int byteSize;
    private int booleanSize;
    private int charSize;
    private int shortSize;
    private int intSize;
    private int floatSize;
    private int longSize;
    private int doubleSize;
    private int oopSize;
    private int objectMarkHeaderSize;
    
    private Field stringValueArrayField;
    private int arrayLengthOffset;
    private boolean compressedOopsEnabled;
    
    static {
        try {
            getAddressMethod = Method.class.getDeclaredMethod("getAddress");
        } catch (NoSuchMethodException e) {
            
        } catch (SecurityException e) {
            
        }
        
        try {
            instanceKlassConstructor = InstanceKlass.class.getConstructor(Address.class);
        } catch (NoSuchMethodException e) {

        } catch (SecurityException e) {

        }
    }

    private void init(HotSpotServiceabilityAgentContext context) {
        VM vm = context.getVM(); 
        
        byteArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_BYTE);
        booleanArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_BOOLEAN);
        charArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_CHAR);
        shortArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_SHORT);
        intArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_INT);
        floatArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_FLOAT);
        longArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_LONG);
        doubleArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_DOUBLE);
        objectArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_OBJECT);
        
        byteSize = (int) context.getVM().getObjectHeap().getByteSize();
        booleanSize = (int) context.getVM().getObjectHeap().getBooleanSize();
        charSize = (int) context.getVM().getObjectHeap().getCharSize();
        shortSize = (int) context.getVM().getObjectHeap().getShortSize();
        intSize = (int) context.getVM().getObjectHeap().getIntSize();
        floatSize = (int) context.getVM().getObjectHeap().getFloatSize();
        longSize = (int) context.getVM().getObjectHeap().getLongSize();
        doubleSize = (int) context.getVM().getObjectHeap().getDoubleSize();
        oopSize = (int) context.getVM().getHeapOopSize();
        objectMarkHeaderSize = (int) context.getVM().getAddressSize();
        
        try {
            InstanceKlass stringKlass = SystemDictionary.getStringKlass();
            stringValueArrayField = 
                stringKlass.findField("value", char[].class.getName());
        } catch (Throwable t) {
            // String support disabled
        }
        
        compressedOopsEnabled = vm.isCompressedOopsEnabled();
        Type type = vm.getTypeDataBase().lookupType("arrayOopDesc");
        int typeSize = (int)type.getSize();
        if (compressedOopsEnabled) {
            arrayLengthOffset = (int) (typeSize - VM.getVM().getIntSize());
        } else {
            arrayLengthOffset = typeSize;
        }
    }
    
    @Override
    public HotSpotSAStackTracerResult run(HotSpotServiceabilityAgentContext context,
                                          HotSpotSAStackTracerParameter param) {
        init(context);
        
        HotSpotSAStackTracerResult result = new HotSpotSAStackTracerResult();
        Set<String> threadNames = param != null ? param.getThreadNames() : null;
        Threads threads = context.getVM().getThreads();
        for (JavaThread cur = threads.first(); cur != null; cur = cur.next()) {
            if (cur.isJavaThread() && (threadNames == null || threadNames.contains(cur.getThreadName()))) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                PrintStream tty = new PrintStream(os);
                try {
                    for (JavaVFrame vf = cur.getLastJavaVFrameDbg(); vf != null; vf = vf.javaSender()) {
                        try {
                            Method method = vf.getMethod();
                            String methodNameAndSignature = method.externalNameAndSignature();
                            if (methodNameAndSignature.startsWith(JEMSTONE_HOTSPOT_SA_PACKAGE_PREFIX)) {
                                os = null;
                                tty = null;
                                continue;
                            }
                            if (os == null) {
                                os = new ByteArrayOutputStream();
                                tty = new PrintStream(os);
                            }
                            tty.print("    |- " + methodNameAndSignature + " @bci=" + vf.getBCI());
    
                            int lineNumber = method.getLineNumberFromBCI(vf.getBCI());
                            if (lineNumber != -1) {
                                tty.print(", line=" + lineNumber);
                            }
    
                            Address pc = vf.getFrame().getPC();
                            if (pc != null) {
                                tty.print(", pc=" + pc);
                            }
    
                            if (getAddressMethod != null) {
                                tty.print(", Method*=" + getAddressMethod.invoke(method));
                            }
                            
                            if (vf.isCompiledFrame()) {
                                tty.print(" (Compiled frame");
                                if (vf.isDeoptimized()) {
                                  tty.print(" [deoptimized]");
                                }
                            }
                            if (vf.isInterpretedFrame()) {
                                tty.print(" (Interpreted frame");
                            }
                            if (vf.mayBeImpreciseDbg()) {
                                tty.print("; information may be imprecise");
                            }
                            tty.println(")");
                            
                            ConstantPool cp = method.getConstants();
                            int paramCount = (int) method.getSizeOfParameters();
                            StackValueCollection localValues = vf.getLocals();
                            int vfBCI = vf.getBCI();

                            if (method.hasLocalVariableTable()) {
                                tty.println("\tparameters:");
                                tty.println(String.format("\t       %-25s %-30s %s", "name", "value", "type"));
                                tty.println("\t    ==============================================================================");
                                
                                LocalVariableTableElement[] localVariables = method.getLocalVariableTable();
                                int index = 0;
                                for (LocalVariableTableElement localVariable : localVariables) {
                                    if (index == paramCount) {
                                        tty.println("\tlocal variables:");
                                        tty.println(String.format("\t       %-25s %-30s %s", "name", "value", "type"));
                                        tty.println("\t    ==============================================================================");
                                    }
                                    
                                    if (localVariable.getStartBCI() + localVariable.getLength() < vf.getBCI()) {
                                        continue;
                                    }
                                    
                                    Symbol localVariableName = 
                                            method.getLocalVariableName(localVariable.getStartBCI(), 
                                                                        localVariable.getSlot());
                                    String localVariableNameStr = localVariableName.asString();
                                    if ("this".equals(localVariableNameStr)) {
                                        continue;
                                    }
                                    
                                    Symbol localVariableType = 
                                            cp.getSymbolAt(localVariable.getDescriptorCPIndex());
                                    String localVariableTypeStr = localVariableType.asString();
                                    Class<?> localVariableTypeClass = 
                                            ReflectionUtil.signatureToClass(localVariableTypeStr);
                                    
                                    tty.print("\t    |- ");
                                    tty.print(String.format("%-25s ", localVariableNameStr));
                                    if (localVariable.getStartBCI() >= vfBCI) {
                                        tty.print(String.format("%-30s %s", 
                                                  "<not initialized>",
                                                  ReflectionUtil.normalizeSignature(localVariableTypeStr))); 
                                    } else {
                                        int localVariableIndex = index;
                                        if (localVariableTypeClass.equals(long.class) || 
                                                localVariableTypeClass.equals(double.class)) {
                                            localVariableIndex++;
                                        }
                                        printLocalVariable(tty, localValues, localVariableTypeStr, 
                                                           localVariable.getSlot(), localVariableIndex);
                                    }
                                    tty.println();
                                    
                                    index++;
                                    if (localVariableTypeClass.equals(long.class) || 
                                            localVariableTypeClass.equals(double.class)) {
                                        index++;
                                    } 
                                }
                            } else {
                                tty.println("\tlocal values:");
                                tty.println(String.format("\t       %-25s %-30s %s", "order", "value", "type"));
                                tty.println("\t    ==============================================================================");
                                for (int i = 0; i < localValues.size(); i++) {
                                    tty.print("\t    |- ");
                                    tty.print(String.format("%-25d ", i));
                                    StackValue localValue = localValues.get(i);
                                    int type = localValue.getType();
                                    switch (type) {
                                        case BasicType.tInt:
                                            long value = localValue.getInteger();
                                            tty.print(String.format("%-30s %s", 
                                                        value + " (" + "0x" + Long.toHexString(value) + ")", 
                                                        "<primitive>"));
                                            break;
                                        case BasicType.tObject:
                                            tty.print(String.format("%-30s %s", 
                                                        localValue.getObject() + " (address)", 
                                                        "<object>"));
                                            break;  
                                        case BasicType.tConflict:
                                            tty.print(String.format("%-30s %s", "<unknown>", "<conflict>"));
                                            break;
                                        default:
                                            tty.print(String.format("%-30s %s", "<unknown>", "<unknown>"));
                                            break;
                                    }
                                    tty.println();
                                }
                            }
                        } catch (Exception e) {
                            tty.println("Error occurred during thread walking: " + e.getMessage());
                        } 
                    }
                } catch (AddressException e) {
                    tty.println("Error accessing address 0x" + Long.toHexString(e.getAddress()));
                } catch (Exception e) {
                    tty.println("Error occurred during stack walking: " + e.getMessage());
                } finally {
                    tty.flush();
                    
                    String output = os.toString();
                    result.addStackTrace(cur.getThreadName(), output.substring(0, output.length()));
                    tty.close();
                }
            }
        }    
        return result;
    }
   
    private void printLocalVariable(PrintStream tty, StackValueCollection values, String type, 
            int slot, int index) throws Exception {
        Class<?> valueClass = ReflectionUtil.signatureToClass(type);
        if (valueClass.isPrimitive()) {
            printPrimitiveValue(tty, values, slot, index, valueClass);
        } else if (valueClass.equals(String.class)) { 
            printStringValue(tty, values, slot);
        } else if (valueClass.isArray()) { 
            Class<?> elementClass = valueClass.getComponentType();
            if (elementClass.isPrimitive()) {
                printPrimitiveArrayValue(tty, values, type, slot, index, 
                                         valueClass, elementClass);
            } else {
                printComplexArrayValue(tty, values, type, slot, index, 
                                       valueClass, elementClass);
            }
        } else {
            printComplexValue(tty, values, type, slot, index, valueClass);
        }
    }
    
    private void printPrimitiveValue(PrintStream tty, StackValueCollection values, 
            int slot, int index, Class<?> valueClass) {
        if (valueClass.equals(byte.class)) {
            byte val = values.byteAt(slot);
            tty.print(String.format("%-30d %s", val, "byte"));
        } else if (valueClass.equals(boolean.class)) {
            boolean val = values.booleanAt(slot);
            tty.print(String.format("%-30s %s", val, "boolean"));
        } else if (valueClass.equals(char.class)) {
            char val = values.charAt(slot);
            tty.print(String.format("%-30c %s", val, "char"));
        } else if (valueClass.equals(short.class)) {
            short val = values.shortAt(slot);
            tty.print(String.format("%-30d %s", val, "short"));
        } else if (valueClass.equals(int.class)) {
            int val = values.intAt(slot);
            tty.print(String.format("%-30d %s", val, "int"));
        }  else if (valueClass.equals(float.class)) {
            float val = values.floatAt(slot);
            tty.print(String.format("%-30f %s", val, "float"));
        }  else if (valueClass.equals(long.class)) {
            long val = values.get(index).getInteger();
            tty.print(String.format("%-30d %s", val, "long"));
        } else if (valueClass.equals(double.class)) {
            double val = Double.longBitsToDouble(values.get(index).getInteger());
            tty.print(String.format("%-30f %s", val, "double"));
        } else {
            throw new IllegalArgumentException("Not primitive type: " + valueClass.getName());
        }
    }
    
    private void printStringValue(PrintStream tty, StackValueCollection values, int slot) {
        OopHandle strOopHandle = values.oopHandleAt(slot);
        if (stringValueArrayField != null) {
            OopHandle valueOopHandle = null;
            if (compressedOopsEnabled) {
                valueOopHandle = strOopHandle.getCompOopHandleAt(stringValueArrayField.getOffset());
            } else {
                valueOopHandle = strOopHandle.getOopHandleAt(stringValueArrayField.getOffset());
            }
            StringBuilder sb = new StringBuilder();
            int length = valueOopHandle.getJIntAt(arrayLengthOffset);
            for (int i = 0; i < length; i++) {
                long offset = charArrayBaseOffset + i * charSize;
                sb.append(valueOopHandle.getJCharAt(offset));
            }
            tty.print(String.format("%-30s %s",
                          sb.toString(),
                          String.class.getName()));
        } else {
            tty.print(String.format("%-30s %s",
                        strOopHandle + " (address)",
                        String.class.getName()));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void printComplexValue(PrintStream tty, StackValueCollection values,  String type, 
            int slot, int index, Class<?> valueClass) {
        OopHandle oop = values.oopHandleAt(slot);
        tty.print(String.format("%-30s %s",
                oop + " (address)",
                ReflectionUtil.normalizeSignature(type)));
        tty.println();
        
        InstanceKlass klass = SystemDictionaryHelper.findInstanceKlass(valueClass.getName());
        List<Field> fields = klass.getAllFields();
        tty.println("\t\tfields:");
        tty.println(String.format("\t\t       %-25s %-30s %s", "name", "value", "type"));
        tty.println("\t\t    ==============================================================================");
        for (Field field : fields) {
            tty.print("\t\t    |- ");
            Symbol fieldSignature = field.getSignature();
            String fieldType = ReflectionUtil.normalizeSignature(fieldSignature.asString());
            char typeCode = (char) fieldSignature.getByteAt(0);
            String fieldName = field.getID().getName();
            switch (typeCode) {
                case JVM_SIGNATURE_BOOLEAN:
                    tty.print(String.format("%-25s %-30s %s", 
                                fieldName,
                                oop.getJBooleanAt(field.getOffset()), 
                                "boolean"));
                    break;
                case JVM_SIGNATURE_CHAR:
                    tty.print(String.format("%-25s %-30c %s", 
                                fieldName,
                                oop.getJCharAt(field.getOffset()), 
                                "char"));
                    break;
                case JVM_SIGNATURE_BYTE:
                    tty.print(String.format("%-25s %-30d %s", 
                                fieldName,
                                oop.getJByteAt(field.getOffset()), 
                                "byte"));
                    break;
                case JVM_SIGNATURE_SHORT:
                    tty.print(String.format("%-25s %-30d %s", 
                                fieldName,
                                oop.getJShortAt(field.getOffset()), 
                                "short"));
                    break;
                case JVM_SIGNATURE_INT:
                    tty.print(String.format("%-25s %-30d %s",
                                fieldName,
                                oop.getJIntAt(field.getOffset()), 
                                "int"));
                    break;
                case JVM_SIGNATURE_FLOAT:
                    tty.print(String.format("%-25s %-30f %s", 
                                fieldName,
                                oop.getJFloatAt(field.getOffset()), 
                                "float"));
                    break;
                case JVM_SIGNATURE_LONG:
                    tty.print(String.format("%-25s %-30d %s", 
                                fieldName, 
                                oop.getJLongAt(field.getOffset()), 
                                "long"));
                    break;
                case JVM_SIGNATURE_DOUBLE:
                    tty.print(String.format("%-25s %-30f %s", 
                                fieldName,
                                oop.getJDoubleAt(field.getOffset()), 
                                "double"));
                    break;
                case JVM_SIGNATURE_CLASS:
                case JVM_SIGNATURE_ARRAY: {
                    if (String.class.getName().equals(fieldType)) {
                        OopHandle strOopHandle;
                        if (compressedOopsEnabled) {
                            strOopHandle = oop.getCompOopHandleAt(field.getOffset());
                        } else {
                            strOopHandle = oop.getOopHandleAt(field.getOffset());
                        }
                        if (stringValueArrayField != null) {
                            OopHandle valueOopHandle;
                            if (compressedOopsEnabled) {
                                valueOopHandle = strOopHandle.getCompOopHandleAt(stringValueArrayField.getOffset());
                            } else {
                                valueOopHandle = strOopHandle.getOopHandleAt(stringValueArrayField.getOffset());
                            }
                            StringBuilder sb = new StringBuilder();
                            int length = valueOopHandle.getJIntAt(arrayLengthOffset);
                            for (int i = 0; i < length; i++) {
                                long offset = charArrayBaseOffset + i * charSize;
                                sb.append(valueOopHandle.getJCharAt(offset));
                            }
                            tty.print(String.format("%-25s %-30s %s",
                                        fieldName,
                                        sb.toString(),
                                        String.class.getName()));
                        } else {
                            tty.print(String.format("%-25s %-30s %s",
                                        fieldName,
                                        strOopHandle + " (address)",
                                        String.class.getName()));
                        }
                    } else {
                        OopHandle handle;
                        if (compressedOopsEnabled) {
                          handle = oop.getCompOopHandleAt(field.getOffset());
                        } else {
                          handle = oop.getOopHandleAt(field.getOffset());
                        }
                        tty.print(String.format("%-25s %-30s %s",
                                    fieldName,
                                    handle + " (address)",
                                    fieldType));
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type code: " + typeCode);
            }
            tty.println();
        }
    }
    
    private void printPrimitiveArrayValue(PrintStream tty, StackValueCollection values,  String type, 
            int slot, int index, Class<?> valueClass, Class<?> elementClass) {
        OopHandle arrayOopHandle = values.oopHandleAt(slot);

        tty.println(String.format("%-30s %s",
                        arrayOopHandle + " (address)",
                        ReflectionUtil.normalizeSignature(type)));
        
        tty.println("\t\telements:");
        tty.println("\t\t    ==============================================================================");
        int length = arrayOopHandle.getJIntAt(arrayLengthOffset);
        if (elementClass.equals(byte.class)) {
            for (int i = 0; i < length; i++) {
                long offset = byteArrayBaseOffset + i * byteSize;
                tty.println("\t\t    |- [" + i + "]: " + arrayOopHandle.getJByteAt(offset));
            }
        } else if (elementClass.equals(boolean.class)) {
            for (int i = 0; i < length; i++) {
                long offset = booleanArrayBaseOffset + i * booleanSize;
                tty.println("\t\t    |- [" + i + "]: " + arrayOopHandle.getJBooleanAt(offset));
            }
        } else if (elementClass.equals(char.class)) {
            for (int i = 0; i < length; i++) {
                long offset = charArrayBaseOffset + i * charSize;
                tty.println("\t\t    |- [" + i + "]: " + arrayOopHandle.getJCharAt(offset));
            }
        } else if (elementClass.equals(short.class)) {
            for (int i = 0; i < length; i++) {
                long offset = shortArrayBaseOffset + i * shortSize;
                tty.println("\t\t    |- [" + i + "]: " + arrayOopHandle.getJShortAt(offset));
            }
        } else if (elementClass.equals(int.class)) {
            for (int i = 0; i < length; i++) {
                long offset = intArrayBaseOffset + i * intSize;
                tty.println("\t\t    |- [" + i + "]: " + arrayOopHandle.getJIntAt(offset));
            }
        } else if (elementClass.equals(float.class)) {
            for (int i = 0; i < length; i++) {
                long offset = floatArrayBaseOffset + i * floatSize;
                tty.println("\t\t    |- [" + i + "]: " + arrayOopHandle.getJFloatAt(offset));
            }
        } else if (elementClass.equals(long.class)) {
            for (int i = 0; i < length; i++) {
                long offset = longArrayBaseOffset + i * longSize;
                tty.println("\t\t    |- [" + i + "]: " + arrayOopHandle.getJLongAt(offset));
            }
        } else if (elementClass.equals(double.class)) {
            for (int i = 0; i < length; i++) {
                long offset = doubleArrayBaseOffset + i * doubleSize;
                tty.println("\t\t    |- [" + i + "]: " + arrayOopHandle.getJDoubleAt(offset));
            }
        } else {
            throw new IllegalArgumentException(elementClass.getName() + 
                    " is not primitive element type for array");
        }
    }
    
    private void printComplexArrayValue(PrintStream tty, StackValueCollection values,  String type, 
            int slot, int index, Class<?> valueClass, Class<?> elementClass) 
                    throws ClassNotFoundException, UnmappedAddressException, UnalignedAddressException, 
                           InstantiationException, IllegalAccessException, 
                           IllegalArgumentException, InvocationTargetException {
        OopHandle arrayOopHandle = values.oopHandleAt(slot);

        tty.println(String.format("%-30s %s",
                        arrayOopHandle + " (address)",
                        ReflectionUtil.normalizeSignature(type)));
        
        tty.println("\t\telements:");
        tty.println("\t\t    ==============================================================================");
        int length = arrayOopHandle.getJIntAt(arrayLengthOffset);
        for (int i = 0; i < length; i++) {
            long offset = objectArrayBaseOffset + i * oopSize;
            OopHandle elementOopHandle;
            InstanceKlass elementKlass = null;
            String elementType;
            if (compressedOopsEnabled) {
                elementOopHandle = arrayOopHandle.getCompOopHandleAt(offset);
                if (instanceKlassConstructor != null) {
                    elementKlass =  
                            (InstanceKlass) instanceKlassConstructor.
                                newInstance(elementOopHandle.getCompOopAddressAt(objectMarkHeaderSize));
                } 
            } else {
                elementOopHandle = arrayOopHandle.getOopHandleAt(offset);
                if (instanceKlassConstructor != null) {
                    elementKlass =  
                            (InstanceKlass) instanceKlassConstructor.
                                newInstance(elementOopHandle.getAddressAt(objectMarkHeaderSize));
                } 
            }
            if (elementKlass != null) {
                elementType = Class.forName(elementKlass.getName().asString().replace("/", ".")).getName();
            } else {
                elementType = elementClass.getName();
            }
            if (String.class.getName().equals(elementType)) {
                if (stringValueArrayField != null) {
                    OopHandle valueOopHandle;
                    if (compressedOopsEnabled) {
                        valueOopHandle = elementOopHandle.getCompOopHandleAt(stringValueArrayField.getOffset());
                    } else {
                        valueOopHandle = elementOopHandle.getOopHandleAt(stringValueArrayField.getOffset());
                    }
                    StringBuilder sb = new StringBuilder();
                    int strLength = valueOopHandle.getJIntAt(arrayLengthOffset);
                    for (int j = 0; j < strLength; j++) {
                        long charOffset = charArrayBaseOffset + j * charSize;
                        sb.append(valueOopHandle.getJCharAt(charOffset));
                    }
                    tty.println("\t\t    |- [" + i + "]: " + sb.toString() + " (" + String.class.getName() + ")");
                } else {
                    tty.println("\t\t    |- [" + i + "]: " + elementOopHandle + " (" + String.class.getName() + ")");
                }
            } else {
                tty.println("\t\t    |- [" + i + "]: " + elementOopHandle + " (" + elementType + ")");
            }
        }
    }
    
}
