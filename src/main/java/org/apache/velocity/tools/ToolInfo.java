package org.apache.velocity.tools;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * Manages data needed to create instances of a tool. New instances
 * are returned for every call to create(obj).
 *
 * @author Nathan Bubna
 * @author <a href="mailto:henning@schmiedehausen.org">Henning P. Schmiedehausen</a>
 * @version $Id: ToolInfo.java 511959 2007-02-26 19:24:39Z nbubna $
 */
//TODO: make this class serializable
public class ToolInfo
{
    private String key;
    private Class clazz;
    private boolean restrictToIsExact;
    private String restrictTo;
    private Map<String,Object> properties;
    private Method setup = null;

    /**
     * Creates a new instance using the minimum required info
     * necessary for a tool.
     */
    public ToolInfo(String key, String classname)
    {
        setKey(key);
        setType(classname);
    }

    /**
     * Creates a new instance using the minimum required info
     * necessary for a tool.
     */
    public ToolInfo(String key, Class clazz)
    {
        setKey(key);
        setClass(clazz);
    }


    /***********************  Mutators *************************/

    public void setKey(String key)
    {
        this.key = key;
        if (this.key == null)
        {
            throw new NullPointerException("Key cannot be null");
        }
    }

    /**
     * If an instance of the tool cannot be created from
     * the classname passed to this method, it will throw an exception.
     *
     * @param classname the fully qualified java.lang.Class name of the tool
     */
    public void setType(String classname)
    {
        try
        {
            // first, make sure we can get the Class
            setClass(Utils.getClass(classname));
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new IllegalArgumentException("Could not load Class: "+classname, cnfe);
        }
    }

    /**
     * Tries to create an instance of the specified Class, then looks for a
     * setup(Map<String,Object>) method.
     *
     * @param clazz the java.lang.Class of the tool
     */
    public void setClass(Class clazz)
    {
        try
        {
            // make sure we can get an instance
            // make sure we can get an instance
            clazz.newInstance();

            // ok, we'll accept this one
            setClass(clazz);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Could not create an instance of "+clazz, e);
        }

        try
        {
            // check for a setup(Map) method
            this.setup =
                clazz.getMethod("setup", new Class[]{ Map.class });
            /* TODO? if we have performance issues with copyProperties,
                     look at possibly checking for and caching these common setters
                        setContext(VelocityContext)
                        setVelocityEngine(VelocityEngine)
                        setLog(Log)
                        setLocale(Locale)
                     these four are tricky since we may not want servlet deps here
                        setServletRequest(ServletRequest)
                        setSession(HttpSession)
                        setServletResponse(ServletResponse)
                        setServletContext(ServletContext)
            */
        }
        catch (NoSuchMethodException nsme)
        {
            // ignore this one
        }
    }

    /**
     * @param path the full or partial request path restriction of the tool
     */
    public void restrictTo(String path)
    {
        if (path.equals("*"))
        {
            // match all paths
            this.restrictTo = null;
        }
        else if(path.endsWith("*"))
        {
            // match some paths
            restrictToIsExact = false;
            this.restrictTo = path.substring(0, path.length() - 1);
        }
        else
        {
            // match one path
            restrictToIsExact = true;
            this.restrictTo = path;
        }
    }

    /**
     * Adds a map of properties from a parent scope to the properties
     * for this tool.  Only new properties will be added; any that
     * are already set for this tool will be ignored.
     */
    public void addProperties(Map<String,Object> parentProps)
    {
        // only add those new properties for which we
        // do not already have a value. first prop set wins.
        Map<String,Object> properties = getProps();
        for (String key : parentProps.keySet())
        {
            if (!properties.containsKey(key))
            {
                properties.put(key, parentProps.get(key));
            }
        }
    }

    /**
     * Puts a new property for this tool.
     */
    public Object putProperty(String name, Object value)
    {
        return getProps().put(name, value);
    }

    protected synchronized Map<String,Object> getProps()
    {
        if (properties == null)
        {
            properties = new HashMap<String,Object>();
        }
        return properties;
    }


    /***********************  Accessors *************************/

    public String getKey()
    {
        return key;
    }

    public String getClassname()
    {
        return clazz.getName();
    }

    public Map<String,Object> getProperties()
    {
        return properties;
    }

    public boolean hasSetup()
    {
        return (this.setup != null);
    }

    /**
     * @param path the path of a template requesting this tool
     * @return <code>true</code> if the specified
     *         request path matches the restrictions of this tool.
     *         If there is no request path restriction for this tool,
     *         it will always return <code>true</code>.
     */
    public boolean hasPermission(String path)
    {
        if (this.restrictTo == null)
        {
            return true;
        }
        else if (restrictToIsExact)
        {
            return this.restrictTo.equals(path);
        }
        else if (path != null)
        {
            return path.startsWith(this.restrictTo);
        }
        return false;
    }


    /***********************  create() *************************/

    /**
     * Returns a new instance of the tool. If the tool
     * has an setup(Object) method, the new instance
     * will be initialized using the given properties combined with
     * whatever "constant" properties have been put into this
     * ToolInfo.
     */
    public Object create(Map<String,Object> dynamicProperties)
    {
        /* Get the tool instance */
        Object tool = newInstance();

        /* if the tool is configurable and we have properties... */
        Map<String,Object> combinedProps = combine(this.properties, dynamicProperties);
        if (hasSetup())
        {
            if (combinedProps != null)
            {
                setup(tool, combinedProps);
            }
        }

        //TODO: make this step optional?
        // look for specific setters
        for (String name : combinedProps.keySet())
        {
            setProperty(tool, name, combinedProps.get(name));
        }
        return tool;
    }


    /***********************  protected methods *************************/

    protected Object newInstance()
    {
        try
        {
            return clazz.newInstance();
        }
        /* we shouldn't get either of these exceptions here because
         * we already got an instance of this class during setType().
         * but to be safe, let's catch them and re-throw as RuntimeExceptions */
        catch (IllegalAccessException iae)
        {
            String message = "Unable to instantiate instance of \"" +
                  getClassname() + "\"";
            throw new IllegalStateException(message, iae);
        }
        catch (InstantiationException ie)
        {
            String message = "Exception while instantiating instance of \"" +
                  getClassname() + "\"";
            throw new IllegalStateException(message, ie);
        }
    }


    protected void setup(Object tool, Map<String,Object> properties)
    {
        try
        {
            // call the setup method on the instance
            setup.invoke(tool, new Object[]{ properties });
        }
        catch (IllegalAccessException iae)
        {
            String msg = "Unable to invoke setup(Map<String,Object>) on "+tool;
            // restricting access to this method by this class ist verboten
            throw new IllegalStateException(msg, iae);
        }
        catch (InvocationTargetException ite)
        {
            String msg = "Exception when invoking setup(Map<String,Object>) on "+tool;
            // convert to a runtime exception, and re-throw
            throw new RuntimeException(msg, ite.getCause());
        }
    }

    protected void setProperty(Object tool, String name, Object value)
    {
        try
        {
            if (PropertyUtils.isWriteable(tool, name))
            {
                //TODO? support property conversion here?
                //      heavy-handed way is BeanUtils.copyProperty(...)
                PropertyUtils.setProperty(tool, name, value);
            }
        }
        catch (Exception e)
        {
            // convert to a runtime exception, and re-throw
            throw new RuntimeException(e);
        }
    }

    //TODO? move to Utils?
    protected Map<String,Object> combine(Map<String,Object>... maps)
    {
        Map<String,Object> combined = new HashMap<String,Object>();
        for (Map<String,Object> map : maps)
        {
            if (map != null)
            {
                combined.putAll(map);
            }
        }
        return combined;
    }

}