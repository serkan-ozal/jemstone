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

package tr.com.serkanozal.jemstone;

import java.io.PrintStream;
import java.util.Set;

import tr.com.serkanozal.jemstone.sa.HotSpotSAPluginInvalidArgumentException;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentManager;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentPlugin;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResult;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotServiceabilityAgentManagerImpl;
import tr.com.serkanozal.jemstone.scanner.JemstoneScanner;
import tr.com.serkanozal.jemstone.scanner.JemstoneScannerFactory;
import tr.com.serkanozal.jemstone.util.ReflectionUtil;

/**
 * Main class for Jemstone framework.
 * 
 * @author Serkan Ozal
 */
public final class Jemstone {

    private static HotSpotServiceabilityAgentManager hotSpotSAManager = 
            HotSpotServiceabilityAgentManagerImpl.getInstance();
    
    static {
        init();
    }

    private Jemstone() {
        
    }
    
    private static void init() {
        registerPluginsAtClasspath();
    }
    
    @SuppressWarnings("rawtypes")
    private static void registerPluginsAtClasspath() {
        JemstoneScanner scanner = JemstoneScannerFactory.getJemstoneScanner();
        Set<Class<? extends HotSpotServiceabilityAgentPlugin>> pluginClasses = 
                scanner.getSubTypedClasses(HotSpotServiceabilityAgentPlugin.class);
        if (pluginClasses != null) {
            for (Class<? extends HotSpotServiceabilityAgentPlugin> pluginClass : pluginClasses) {
                hotSpotSAManager.registerPlugin(ReflectionUtil.getInstance(pluginClass));
            }
        }
    }
    
    /**
     * Gets the global {@link HotSpotServiceabilityAgentManager} implementation.
     * 
     * @return the global {@link HotSpotServiceabilityAgentManager} implementation.
     */
    public static HotSpotServiceabilityAgentManager getHotSpotServiceabilityAgentManager() {
        return hotSpotSAManager;
    }

    /**
     * Sets the global {@link HotSpotServiceabilityAgentManager} implementation.
     * 
     * @param hotSpotSAManager the new global {@link HotSpotServiceabilityAgentManager} 
     *                         implementation to be set
     */
    public static void setHotSpotServiceabilityAgentManager(
            HotSpotServiceabilityAgentManager hotSpotSAManager) {
        Jemstone.hotSpotSAManager = hotSpotSAManager;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) {
        if (args.length == 0) {
            invalidUsage();
        } else {
            if ("-i".equals(args[0])) {
                if (args.length < 2) {
                    invalidUsage();
                } else {
                    String pluginId = args[1];
                    String[] pluginArgs = new String[args.length - 2]; 
                    System.arraycopy(args, 2, pluginArgs, 0, pluginArgs.length);
                    try {
                        HotSpotServiceabilityAgentResult result = 
                                hotSpotSAManager.runPlugin(pluginId, pluginArgs);
                        System.out.println(result);
                    } catch (HotSpotSAPluginInvalidArgumentException e) {
                        handlePluginInvalidArgumentException(e, hotSpotSAManager.getPlugin(pluginId));
                    }
                }
            } else if ("-p".equals(args[0])) {
                if (args.length < 2) {
                    invalidUsage();
                } else {
                    String pluginClassName = args[1];
                    HotSpotServiceabilityAgentPlugin plugin = ReflectionUtil.getInstance(pluginClassName);
                    String[] pluginArgs = new String[args.length - 2]; 
                    System.arraycopy(args, 2, pluginArgs, 0, pluginArgs.length);
                    try {
                        HotSpotServiceabilityAgentResult result = 
                                hotSpotSAManager.runPlugin(plugin, pluginArgs);
                        System.out.println(result);
                    } catch (HotSpotSAPluginInvalidArgumentException e) {
                        handlePluginInvalidArgumentException(e,plugin);
                    }    
                } 
            } else if ("-h".equals(args[0]) || "-help".equals(args[0])) {
                printUsage(false);
            } else {
                invalidUsage();
            }
        }
    }
    
    @SuppressWarnings("rawtypes")
    private static void handlePluginInvalidArgumentException(HotSpotSAPluginInvalidArgumentException exception, 
            HotSpotServiceabilityAgentPlugin plugin) {
        exception.printStackTrace();
        if (plugin != null) {
            System.err.println("Invalid usage of plugin " + plugin.getId());
            System.err.println(plugin.getUsage());
        }
    }
    
    private static void invalidUsage() {
        System.err.println("Invalid arguments !");
        printUsage(true);
    }
    
    private static void printUsage(boolean error) {
        PrintStream ps;
        if (error) {
            ps = System.err;
        } else {
            ps = System.out;
        }
        ps.println("Usage: " + Jemstone.class.getName() + " " + 
                "(-i <plugin_id> [arg]*)" + 
                " | " + 
                "(-p <plugin_class_name> [arg]*)");
    }
    
}
