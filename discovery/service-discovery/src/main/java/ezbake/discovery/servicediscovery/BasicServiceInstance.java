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

package ezbake.discovery.servicediscovery;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Beta
@SuppressWarnings("unused")
public class BasicServiceInstance implements ServiceInstance {
    private String applicationName;
    private String serviceName;
    private String serviceType;
    private String host;
    private int port;

    public BasicServiceInstance(String applicationName, String serviceName, String serviceType, String host, int port) {
        checkNotNull(applicationName);
        checkNotNull(serviceName);
        checkNotNull(serviceType);
        checkNotNull(host);
        checkArgument(port > 0);

        this.applicationName = applicationName;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.host = host;
        this.port = port;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getServiceType() {
        return serviceType;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public  boolean isCommonService() {
        return getApplicationName().equals(ServiceDiscoveryConstants.COMMON_SERVICES_APPLICATION_NAME);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }

        BasicServiceInstance rhs = (BasicServiceInstance) obj;
        return new EqualsBuilder()
                .append(applicationName, rhs.applicationName)
                .append(serviceName, rhs.serviceName)
                .append(serviceType, rhs.serviceType)
                .append(host, rhs.host)
                .append(port, rhs.port)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(applicationName)
                .append(serviceName)
                .append(serviceType)
                .append(host)
                .append(port)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("applicationName", applicationName)
                .append("serviceName", serviceName)
                .append("serviceType", serviceType)
                .append("host", host)
                .append("port", port)
                .toString();
    }
}
