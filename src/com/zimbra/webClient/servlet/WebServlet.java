/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Web Client
 * Copyright (C) 2014 Zimbra, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.webClient.servlet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.zimbra.common.consul.CatalogRegistration;
import com.zimbra.common.consul.ConsulClient;
import com.zimbra.common.consul.ConsulServiceLocator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.servicelocator.ServiceLocator;
import com.zimbra.common.servicelocator.ZimbraServiceNames;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.util.BuildInfo;


/**
 * The WebServlet and WebAdminServlet servlets are used as a central place to hook
 * lifecycle events for these two webapps. This creates a place to perform Service Locator integration.
 */
@SuppressWarnings("serial")
public class WebServlet extends HttpServlet {
    protected ServiceLocator serviceLocator;
    protected String httpServiceID, httpsServiceID;

    public WebServlet() {
        super ();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            serviceLocator = new ConsulServiceLocator(new ConsulClient(Provisioning.getInstance().getLocalServer().getConsulURL()));
        } catch (ServiceException e) {
            throw new ServletException("Failed instantiating Consul service locator", e);
        }
        registerWithServiceLocator();
    }

    @Override
    public void destroy() {
        deregisterWithServiceLocator();
        super.destroy();
    }

    /**
     * Register with service locator.
     *
     * @see https://www.consul.io/docs/agent/http.html#_v1_catalog_register
     */
    protected void registerWithServiceLocator() throws ServletException {

        // Read protocol and port configuration
        int httpPort = 0, httpsPort = 0;
        String protocolMode;
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            protocolMode = (String) envCtx.lookup("protocolMode");

            String str = (String) envCtx.lookup("httpPort");
            httpPort = new Integer(str != null ? str : ZCServlet.DEFAULT_HTTP_PORT);

            str = (String) envCtx.lookup("httpsPort");
            httpsPort = new Integer(str != null ? str : ZCServlet.DEFAULT_HTTPS_PORT);
        } catch (NamingException e) {
            throw new ServletException(e.getLocalizedMessage(), e);
        }

        // Register http endpoint
        if (ZCServlet.PROTO_HTTP.equals(protocolMode) || ZCServlet.PROTO_MIXED.equals(protocolMode)) {
            httpServiceID = registerWithServiceLocator(ZimbraServiceNames.WEB, httpPort, "http");
        }

        // Register https endpoint
        if (ZCServlet.PROTO_HTTPS.equals(protocolMode) || ZCServlet.PROTO_MIXED.equals(protocolMode)) {
            httpsServiceID = registerWithServiceLocator(ZimbraServiceNames.WEB, httpsPort, "https");
        }
    }

    protected String registerWithServiceLocator(String serviceName, int port, String checkScheme) {
        String serviceID = serviceName + ":" + port;
        CatalogRegistration.Service service = new CatalogRegistration.Service(serviceID, serviceName, port);
        service.tags.add(BuildInfo.MAJORVERSION + "." + BuildInfo.MINORVERSION + ".x");
        service.tags.add(BuildInfo.MAJORVERSION + "." + BuildInfo.MINORVERSION + "." + BuildInfo.MICROVERSION);
        if ("https".equals(checkScheme)) {
            service.tags.add("ssl");
        }
        String url = checkScheme + "://localhost:" + port + "/";
        CatalogRegistration.Check check = new CatalogRegistration.Check(serviceID + ":health", serviceName);
        check.script = "/opt/zimbra/libexec/zmhealthcheck-web " + url;
        check.interval = "1m";
        service.check = check;
        serviceLocator.registerSilent(service);
        return serviceID;
    }

    /**
     * De-register with service locator.
     *
     * @see https://www.consul.io/docs/agent/http.html#_v1_catalog_deregister
     */
    protected void deregisterWithServiceLocator() {
        if (httpServiceID != null) {
            serviceLocator.deregisterSilent(httpServiceID);
            httpServiceID = null;
        }
        if (httpsServiceID != null) {
            serviceLocator.deregisterSilent(httpsServiceID);
            httpsServiceID = null;
        }
    }
}
