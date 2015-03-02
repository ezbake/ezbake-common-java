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

package ezbake.ezdiscovery;

import java.io.Closeable;
import java.io.IOException;

import java.util.List;
import java.util.Properties;

public interface ServiceDiscovery extends Closeable {
    void registerEndpoint(final Properties configuration, final String endPoint) throws Exception;
    void registerEndpoint(final String serviceName, final String endPoint) throws Exception;
    void registerEndpoint(final String appName, final String serviceName, final String endPoint) throws Exception;

    void unregisterEndpoint(final Properties configuration, final String endPoint) throws Exception;
    void unregisterEndpoint(final String serviceName, final String endPoint) throws Exception;
    void unregisterEndpoint(final String appName, final String serviceName, final String endPoint) throws Exception;

    List<String> getApplications() throws Exception;
    List<String> getServices() throws Exception;
    List<String> getServices(final String appName) throws Exception;
    List<String> getEndpoints(final String serviceName) throws Exception;
    List<String> getEndpoints(final String appName, final String serviceName) throws Exception;

    void setSecurityIdForApplication(String applicationName, String securityId) throws IOException;
    void setSecurityIdForCommonService(String serviceName, String securityId) throws IOException;
    String getSecurityIdForApplication(String applicationName) throws IOException;
    String getSecurityIdForCommonService(String serviceName) throws IOException;

    boolean isServiceCommon( final String serviceName) throws Exception;
}
