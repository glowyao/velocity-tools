/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.velocity.tools.struts;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.view.tools.LinkTool;
import org.apache.velocity.tools.struts.StrutsUtils;

/**
 * <p>View tool to work with URI links in Struts.</p> 
 * 
 * <p>This class is equipped to be used with a toolbox manager, for example
 * the ServletToolboxManager included with VelServlet. This class implements 
 * interface ViewTool, which allows a toolbox manager to pass the required
 * context information.</p>
 *
 * <p>This class is not thread-safe by design. A new instance is needed for
 * the processing of every template request.  This means this tool should
 * only be used in the request scope, not application or session scopes.</p>
 *
 * @author <a href="mailto:sidler@teamup.com">Gabe Sidler</a>
 * @author <a href="mailto:nathan@esha.com">Nathan Bubna</a>
 *
 * @version $Id: StrutsLinkTool.java,v 1.5 2003/07/30 19:14:55 nbubna Exp $
 */
public class StrutsLinkTool extends LinkTool
{


    /**
     * <p>Returns a copy of the link with the given action name
     * converted into a server-relative URI reference. This method 
     * does not check if the specified action really is defined. 
     * This method will overwrite any previous URI reference settings 
     * but will copy the query string.</p>
     *
     * @param action an action path as defined in struts-config.xml
     *
     * @return a new instance of StrutsLinkTool
     */
    public StrutsLinkTool setAction(String action)
    {
        return (StrutsLinkTool)copyWith(
            StrutsUtils.getActionMappingURL(application, request, action));
    }
    
    
    /**
     * <p>Returns a copy of the link with the given global forward name
     * converted into a server-relative URI reference. If the parameter 
     * does not map to an existing global forward name, <code>null</code> 
     * is returned. This method will overwrite any previous URI reference 
     * settings but will copy the query string.</p>
     *
     * @param forward a global forward name as defined in struts-config.xml
     *
     * @return a new instance of StrutsLinkTool
     */
    public StrutsLinkTool setForward(String forward)
    {
        String url = StrutsUtils.getForwardURL(request, application, forward);
        if (url == null)
        {
            Velocity.warn("In method setForward(" + forward +
                          "): Parameter does not map to a valid forward.");
            return null;
        }
        return (StrutsLinkTool)copyWith(url);
    }


}
