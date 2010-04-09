/*
 * Copyright (C) 2010 Oracle Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.gemini.jpa;

import java.io.Closeable;
import java.lang.reflect.Array;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;

/**
 * Utility class containing functions that are generally useful during 
 * development and at runtime.
 * 
 * @author mkeith
 */
public class GeminiUtil {
    
    /*==================*/
    /* Static constants */
    /*==================*/
    
    public static String JPA_JDBC_DRIVER_PROPERTY = "javax.persistence.jdbc.driver";
    public static String JPA_JDBC_URL_PROPERTY = "javax.persistence.jdbc.url";
    public static String JPA_JDBC_USER_PROPERTY = "javax.persistence.jdbc.user";
    public static String JPA_JDBC_PASSWORD_PROPERTY = "javax.persistence.jdbc.password";
    
    /*============================*/
    /* Helper and Utility methods */
    /*============================*/

    // Function to obtain the version from a bundle
    public static String bundleVersion(Bundle b) {
        return b.getVersion().toString();
    }
    
    // Function to return a package String formatted with "." instead of "/"
    public static String formattedPackageString(String s, char beingReplaced, char replacer) {
        String formatted = s;
        // Replace all instances of character
        if (formatted.indexOf(beingReplaced) >= 0) 
            formatted = formatted.replace(beingReplaced, replacer);
        // Tack on trailing character if needed
        if (formatted.charAt(formatted.length()-1) != replacer) 
            formatted = formatted + replacer;
        return formatted;
    }
    
    // Function to close a stream (as much as it can be closed)
    public static void closeStream(Closeable c) {
        try { c.close(); } catch (Exception ex){}
    }

    /*================================*/
    /* Status and debugging functions */
    /*================================*/
    
    // Function to throw a runtime exception (throws exception)
    public static void fatalError(String s, Throwable t) { 
        System.out.println("*** FATAL ERROR *** " + s);
        if (t != null) 
            t.printStackTrace(System.out);
        throw new RuntimeException(s,t); 
    }

    // Function to indicate a warning condition (non-terminating)
    public static void warning(String msg) {
        warning(msg, "");
    }

    // Function to indicate a warning condition (non-terminating)
    public static void warning(String msg, Throwable t) {
        String msg2 = (t != null ? (" Exception: " + t) : "");
        warning(msg, msg2);
    }

    // Function to indicate a warning condition (non-terminating)
    public static void warning(String msg, String msg2) {
        String outputMsg = "WARNING: " + msg + msg2;  
        System.out.println(outputMsg);
    }

    // Function to print out debug strings for XML parsing purposes
    public static void debugXml(String... msgs) { 
        if (System.getProperty("GEMINI_DEBUG_XML") != null) {
            debug(msgs);
        }
    }
    
    // Function to print out series of debug strings
    public static void debug(String... msgs) { 
        if (System.getProperty("GEMINI_DEBUG") != null) {
            StringBuilder sb = new StringBuilder();
            for (String msg : msgs) sb.append(msg);
            System.out.println(sb.toString()); 
        }
    }

    // Function to print out a string and an object.
    // Handles some objects specially and prints out more info
    public static void debug(String msg, Object obj) { 
        if (System.getProperty("GEMINI_DEBUG") != null) {
            if (obj == null) {
                System.out.println(msg + String.valueOf(obj));
            } else if (ClassLoader.class.isAssignableFrom(obj.getClass())) {
                System.out.println(msg + obj);
                ClassLoader p = (ClassLoader)obj;
                while (p.getParent() != null) {
                    System.out.println("  Parent loader: " + p.getParent());
                    p = p.getParent();
                }
            } else if (Bundle.class.isAssignableFrom(obj.getClass())) {
                    Bundle b = (Bundle) obj;
                    System.out.println(msg + " bundle=" + b.getSymbolicName() + 
                                             " id=" + b.getBundleId()+ 
                                             " state=" + stringBundleStateFromInt(b.getState()));
            } else if (BundleEvent.class.isAssignableFrom(obj.getClass())) {
                    BundleEvent event = (BundleEvent) obj;
                    System.out.println(msg + " bundle=" + event.getBundle().getSymbolicName() + 
                            ", event=" + stringBundleEventFromInt(event.getType())); 
            } else if (obj.getClass().isArray()) {
                System.out.println(msg);
                int len = ((Object[])obj).length;
                for (int i=0; i<len; i++) {
                    System.out.print("  ");
                    System.out.println(String.valueOf(Array.get(obj, i)));                    
                }
            } else {
                System.out.println(msg + String.valueOf(obj));
            }
        }
    }
    
    public static String stringBundleStateFromInt(int bundleState) {
        switch (bundleState) {
            case 1: return "UNINSTALLED";
            case 2: return "INSTALLED";
            case 4: return "RESOLVED";
            case 8: return "STARTING";
            case 16: return "STOPPING";
            case 32: return "ACTIVE";
            default: return "UNDEFINED_STATE";
        }
    }
    
    public static String stringBundleEventFromInt(int eventType) {
        switch (eventType) {
            case 1: return "INSTALLED";
            case 2: return "STARTED";
            case 4: return "STOPPED";
            case 8: return "UPDATED";
            case 16: return "UNINSTALLED";
            case 32: return "RESOLVED";
            case 64: return "UNRESOLVED";
            case 128: return "STARTING";
            case 256: return "STOPPING";
            case 512: return "LAZY_ACTIVATION";
            default: return "UNDEFINED_EVENT";
        }
    }
}