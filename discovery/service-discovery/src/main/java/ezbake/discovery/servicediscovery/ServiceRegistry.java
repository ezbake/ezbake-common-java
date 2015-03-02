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
import java.util.List;

/**
 * ServiceRegistry provides a mechanism to find applications, services, and
 * instances for those services.
 * <p/>
 * Each ezBake application provides one or more services, each of which may be
 * communicated with using one or more protocols. Finally, each protocol for
 * each service for each application may be hosted on one or more instances.
 * <p/>
 * Services may also be unassociated with an application, in which case they
 * are called "Common Services". Common services also may have multiple
 * protocols and multiple instances.
 * <p/>
 * Implementations of ServiceRegistry may place limitations on the forms of
 * application names and service names, but in general, names of the form
 * "[A-Za-z][A-Za-z0-9\-]+" are acceptable.
 */
@Beta
@SuppressWarnings("unused")
public interface ServiceRegistry {

    /**
     * Register an instance for a discoverable service.
     *
     * A service may have one or more instances.
     *
     * @param instance instance to register
     * @throws DiscoveryException if the client cannot communicate with the back-end discovery storage
     */
    void registerInstance(ServiceInstance instance) throws DiscoveryException;

    /**
     * Unregister an instance for a discoverable service.
     *
     * @param instance instance to unregister
     * @throws DiscoveryException if the client cannot communicate with the back-end discovery storage
     */
    void unregisterInstance(ServiceInstance instance) throws DiscoveryException;

    /**
     * List all applications registered with the client.
     *
     * @return a list of strings containing the application names
     * @throws DiscoveryException if the client cannot communicate with the back-end discovery storage
     */
    List<String> listApplications() throws DiscoveryException;

    /**
     * List all services provided by a given application.
     *
     * @param applicationName name of application
     * @return list of services provided by the application
     * @throws DiscoveryException if the client cannot communicate with the back-end discovery storage
     */
    List<String> listServices(String applicationName) throws DiscoveryException;

    /**
     * List types provided by an application and service.
     *
     * @param applicationName name of application
     * @param serviceName name of service
     * @return list of services of the given type provided by the application
     * @throws DiscoveryException if the client cannot communicate with the back-end discovery storage
     */
    List<String> listServiceTypes(String applicationName, String serviceName) throws DiscoveryException;

    /**
     * List the instances for a service in an application.
     *
     * @param applicationName name of application or empty if the service is a common service
     * @param serviceName name of service
     * @param serviceType type of service
     * @return a list of hosts and ports providing the given service
     * @throws DiscoveryException if the client cannot communicate with the back-end discovery storage
     */
    List<ServiceInstance> listInstances(String applicationName,
                                               String serviceName,
                                               String serviceType) throws DiscoveryException;
}
