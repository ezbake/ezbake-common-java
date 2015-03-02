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

import com.google.common.annotations.Beta;
/**
 * An instance of a discoverable service.
 */
@Beta
public interface ServiceInstance {
    /**
     * Return the application name this service is part of.
     * @return application name this service is part of
     */
    String getApplicationName();

    /**
     * Return the service name of this instance.
     */
    String getServiceName();

    /**
     * Return the type of service type of this instance.
     */
    String getServiceType();

    /**
     * Return the hostname this instance is running on.
     * @return host running this instance
     */
    String getHost();

    /**
     * Return the port this instance is running on.
     * @return port associated with this instance
     */
    int getPort();

    /**
     * Return whether or not service instance is a common service or not
     * @return boolean of whether or not this is a comment service
     */
    boolean isCommonService();

}
