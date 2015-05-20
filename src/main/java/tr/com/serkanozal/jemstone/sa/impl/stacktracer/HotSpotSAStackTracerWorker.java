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
import java.util.Set;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.debugger.AddressException;
import sun.jvm.hotspot.debugger.OopHandle;
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
    
    private static java.lang.reflect.Method getAddressMethod;
    
    private InstanceKlass stringKlass;
    private Field stringValueArrayField;
    private long charArrayBaseOffset;
    private int charSize;
    private int arrayLengthOffset;
    private boolean compressedOopsEnabled;
    
    static {
        try {
            getAddressMethod = Method.class.getDeclaredMethod("getAddress");
        } catch (NoSuchMethodException e) {
            getAddressMethod = null;
        } catch (SecurityException e) {
            getAddressMethod = null;
        }
    }

    private void init(HotSpotServiceabilityAgentContext context) {
        stringKlass = 
                SystemDictionaryHelper.findInstanceKlass(String.class.getName());
        stringValueArrayField = 
                stringKlass.findField("value", char[].class.getName());
        charArrayBaseOffset = TypeArray.baseOffsetInBytes(BasicType.T_CHAR);
        charSize = (int) context.getVM().getObjectHeap().getCharSize();
        VM vm = context.getVM(); 
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
            int slot, int index) {
        Class<?> valueClass = ReflectionUtil.signatureToClass(type);
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
        } else if (valueClass.equals(String.class)) { 
            OopHandle strOopHandle = values.oopHandleAt(slot);
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
                          ReflectionUtil.normalizeSignature(type)));
        } else {
            tty.print(String.format("%-30s %s",
                          values.oopHandleAt(slot) + " (address)",
                          ReflectionUtil.normalizeSignature(type)));
        }
    }
    
}
