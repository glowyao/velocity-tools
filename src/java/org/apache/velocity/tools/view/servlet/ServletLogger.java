/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.velocity.tools.view.servlet;


import javax.servlet.ServletContext;

import org.apache.velocity.runtime.log.LogSystem;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;

/**
 *  Simple wrapper for the servlet log
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 */
public class ServletLogger implements LogSystem
{
    protected ServletContext servletContext = null;

    public static final String PREFIX = " Velocity ";

    /**
     * Construct a logger for VelocityViewServlets. Instances of this class
     * assume that the ServletContext will be available through the 
     * Application Attributes of Velocity, under the
     * VelocityViewServlet.SERVLET_CONTEXT_KEY.
     */
    public ServletLogger()
    {
    }

    /**
     * init()
     * 
     * @throws IllegalStateException if the ServletContext is not available
     *         in the application attributes under the appropriate key.
     */
    public void init( RuntimeServices rs ) 
        throws Exception
    {
        Object obj = rs.getApplicationAttribute(VelocityViewServlet.SERVLET_CONTEXT_KEY);
        if (obj == null)
        {
            throw new IllegalStateException("Could not retrieve ServletContext from application attributes!");
        }
        servletContext = (ServletContext)obj;
    }

    /**
     * Send a log message from Velocity.
     */
    public void logVelocityMessage(int level, String message)
    {
        switch (level) 
        {
            case LogSystem.WARN_ID:
                servletContext.log( PREFIX + RuntimeConstants.WARN_PREFIX + message );
                break;
            case LogSystem.INFO_ID:
                servletContext.log( PREFIX + RuntimeConstants.INFO_PREFIX + message);
                break;
            case LogSystem.DEBUG_ID:
                servletContext.log( PREFIX + RuntimeConstants.DEBUG_PREFIX + message);
                break;
            case LogSystem.ERROR_ID:
                servletContext.log( PREFIX + RuntimeConstants.ERROR_PREFIX + message);
                break;
            default:
                servletContext.log( PREFIX + " : " + message);
                break;
        }
    }

}