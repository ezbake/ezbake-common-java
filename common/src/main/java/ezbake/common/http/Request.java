/*   Copyright (C) 2013-2014 Computer Sciences Corporation
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
 * limitations under the License. */

package ezbake.common.http.request;

import com.google.common.collect.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * This Request is to be used with EzSecurity.  So it is not 100% implemented.
 *
 * This is designed to be an interface layer between JRuby and Java.
 */
public class Request implements javax.servlet.http.HttpServletRequest {

    private static String EMPTY_STRING = "";

    protected String authType = EMPTY_STRING;
    protected Cookie[] cookies = new Cookie[0];
    protected Multimap<String, String> headers = ArrayListMultimap.create();
    protected String method = EMPTY_STRING;
    protected String pathInfo = EMPTY_STRING;
    protected String pathTranslated = EMPTY_STRING;
    protected String contextPath = EMPTY_STRING;
    protected String queryString = EMPTY_STRING;
    protected String remoteUser = EMPTY_STRING;
    protected String requestedSessionId = EMPTY_STRING;
    protected String requestUri = EMPTY_STRING;
    protected String servletPath = EMPTY_STRING;
    protected Map<String, Object> attributes = Maps.newHashMap();
    protected String characterEncoding = EMPTY_STRING;
    protected int contentLength = 0;
    protected String contentType = EMPTY_STRING;
    protected Multimap<String, String> parameters = ArrayListMultimap.create();
    protected String protocol = EMPTY_STRING;
    protected String scheme = EMPTY_STRING;
    protected int serverPort = 0;
    protected String serverName = EMPTY_STRING;
    protected String remoteAddr = EMPTY_STRING;
    protected String remoteHost = EMPTY_STRING;
    protected String realPath = EMPTY_STRING;
    protected int remotePort = 0;
    protected String localName = EMPTY_STRING;
    protected String localAddr = EMPTY_STRING;
    protected int localPort = 0;


    public Request() {

    }

    @Override
    public String getAuthType() {
        return authType;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public long getDateHeader(String name) {
        String value = getHeader(name);
        if ( value == null ) return 0;
        return Long.parseLong(value);
    }

    @Override
    public String getHeader(String name) {
        return getFirst(headers, name);
    }

    private static String getFirst(Multimap<String, String> m, String name) {
        Collection<String> values = m.get(name.toUpperCase());
        if ( values == null || values.isEmpty() ) return null;
        return Iterables.getFirst(values, null);
    }

    @Override
    public Enumeration getHeaders(final String name) {
        return Iterators.asEnumeration(headers.get(name).iterator());
    }

    @Override
    public Enumeration getHeaderNames() {
        return Iterators.asEnumeration(headers.keySet().iterator());
    }

    @Override
    public int getIntHeader(String name) {
        String value = getHeader(name);
        if ( value == null ) return 0;
        return Integer.parseInt(value);
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    @Override
    public String getPathTranslated() {
        return pathTranslated;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteUser() {
        return remoteUser;
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    @Override
    public String getRequestURI() {
        return requestUri;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(requestUri);
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return requestedSessionId != null && !requestedSessionId.isEmpty();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        return Iterators.asEnumeration(attributes.keySet().iterator());
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        characterEncoding = env;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getParameter(String name) {
        return getFirst(parameters, name);
    }

    @Override
    public Enumeration getParameterNames() {
        return Iterators.asEnumeration(parameters.keySet().iterator());
    }

    @Override
    public String[] getParameterValues(String name) {
        Collection<String> strings = parameters.get(name.toUpperCase());
        return strings.toArray(new String[strings.size()]);
    }

    @Override
    public Map getParameterMap() {
        return parameters.asMap();
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return remoteHost;
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration getLocales() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public String getRealPath(String path) {
        return realPath;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public String getLocalAddr() {
        return localAddr;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }


    //**********************************
    // Setters for the interface
    //**********************************


    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    public void setHeaders(Multimap<String, String> headers) {
        this.headers.clear();
        for (Map.Entry<String, Collection<String>> h : headers.asMap().entrySet()) {
            this.headers.putAll(h.getKey().toUpperCase(), h.getValue());
        }
    }

    public void setHeadersFromArray(Iterable<Header> local_headers) {
        this.headers.clear();
        for (Header h : local_headers) {
            this.headers.put(h.getName(), h.getValue());
        }
    }

    public void addHeader(String name, String value) {
        this.headers.put(name.toUpperCase(), value);
    }

    public void removeHeaders(String name) {
        this.headers.removeAll(name.toUpperCase());
    }

    public void setMethod(String method) {
        this.method = method.toUpperCase();
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public void setPathTranslated(String pathTranslated) {
        this.pathTranslated = pathTranslated;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setParameters(Multimap<String, String> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public void removeParameters(String name) {
        this.parameters.removeAll(name);
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
}
