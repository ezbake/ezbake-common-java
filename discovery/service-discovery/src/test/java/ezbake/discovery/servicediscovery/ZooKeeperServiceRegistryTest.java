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

import com.google.common.collect.ImmutableSet;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ZooKeeperServiceRegistryTest
{
    private TestingServer server;
    private ZooKeeperServiceRegistry client;

    @Before
    public void setup() throws Exception {
        System.setProperty("curator-dont-log-connection-problems", "true");
        server = new TestingServer();
        client = new ZooKeeperServiceRegistry(server.getConnectString());
    }

    @After
    public void teardown() {
        try {
            client.close();
        } catch (Throwable t) {
            // Empty
        } finally {
            try {
                server.close();
            } catch (Throwable t) {
                // Empty
            }
        }
    }

    private static <T> void assertSingletonEquals(T expected, Collection<T> collection) {
        // Gives us nicer error messages
        assertEquals(ImmutableSet.of(expected), ImmutableSet.copyOf(collection));
    }

    private static void assertEmpty(Collection c) {
        assertTrue(c.isEmpty());
    }

    @Test
    public void testRegisterEndpoint() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "thrift", "h", 1);
        client.registerInstance(i);

        List<ServiceInstance> instances = client.listInstances("a", "s", "thrift");
        assertSingletonEquals(i, instances);
    }

    @Test
    public void testRegisterMultipleEndpoint() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "thrift", "h", 1);
        ServiceInstance j = new BasicServiceInstance("a", "s", "thrift", "h", 2);
        client.registerInstance(i);
        client.registerInstance(j);

        List<ServiceInstance> instances = client.listInstances("a", "s", "thrift");
        assertEquals(ImmutableSet.of(i, j), ImmutableSet.copyOf(instances));
    }

    @Test
    public void testRegisterDuplicateEndpoint() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "thrift", "h", 1);
        client.registerInstance(i);
        client.registerInstance(i);

        List<ServiceInstance> instances = client.listInstances("a", "s", "thrift");
        assertSingletonEquals(i, instances);
    }

    @Test
    public void testRegisterMultipleTypes() throws DiscoveryException {
        ServiceInstance ir = new BasicServiceInstance("a", "s", "rest", "h", 1);
        client.registerInstance(ir);
        ServiceInstance it = new BasicServiceInstance("a", "s", "thrift", "h", 2);
        client.registerInstance(it);

        List<ServiceInstance> instances = client.listInstances("a", "s", "rest");
        assertSingletonEquals(ir, instances);

        instances = client.listInstances("a", "s", "thrift");
        assertSingletonEquals(it, instances);
    }

    @Test
    public void testUnregisterEndpoint() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "thrift", "h", 1);
        client.registerInstance(i);

        List<ServiceInstance> instances = client.listInstances("a", "s", "thrift");
        assertSingletonEquals(i, instances);

        client.unregisterInstance(i);
        instances = client.listInstances("a", "s", "thrift");
        assertEmpty(instances);
    }

    @Test
    public void testUnregisterDuplicateEndpoint() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "thrift", "h", 1);
        client.registerInstance(i);

        List<ServiceInstance> instances = client.listInstances("a", "s", "thrift");
        assertSingletonEquals(i, instances);

        client.unregisterInstance(i);
        instances = client.listInstances("a", "s", "thrift");
        assertEmpty(instances);

        client.unregisterInstance(i);
        instances = client.listInstances("a", "s", "thrift");
        assertEmpty(instances);
    }

    @Test
    public void testUnregisterNonexistentEndpoint() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "thrift", "h", 1);
        client.unregisterInstance(i);
        List<ServiceInstance> instances = client.listInstances("a", "s", "thrift");
        assertTrue(instances.isEmpty());
    }

    @Test
    public void testUnregisterMultipleTypes() throws DiscoveryException {
        ServiceInstance ir = new BasicServiceInstance("a", "s", "rest", "h", 1);
        client.registerInstance(ir);
        ServiceInstance it = new BasicServiceInstance("a", "s", "thrift", "h", 2);
        client.registerInstance(it);

        client.unregisterInstance(ir);
        List<ServiceInstance> instances = client.listInstances("a", "s", "rest");
        assertEmpty(instances);

        instances = client.listInstances("a", "s", "thrift");
        assertSingletonEquals(it, instances);
    }

    // Differs from BasicServiceInstance in that it doesn't validate anything
    private class NonvalidatingServiceInstance implements ServiceInstance {
        private final String applicationName;
        private final String serviceName;
        private final String serviceType;
        private final String host;
        private final int port;

        public NonvalidatingServiceInstance(String applicationName,
                                            String serviceName,
                                            String serviceType,
                                            String host,
                                            int port) {
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
        public boolean isCommonService() {
            return getApplicationName().equals(ServiceDiscoveryConstants.COMMON_SERVICES_APPLICATION_NAME);
        }
    }

    @Test(expected=NullPointerException.class)
    public void testRejectNullApplicationName() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance(null, "s", "thrift", "h", 1);
        client.registerInstance(i);
    }

    @Test(expected=NullPointerException.class)
    public void testRejectNullServiceName() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", null, "thrift", "h", 1);
        client.registerInstance(i);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRejectEmptyServiceName() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", "", "thrift", "h", 1);
        client.registerInstance(i);
    }

    @Test(expected=NullPointerException.class)
    public void testRejectNullServiceType() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", "s", null, "h", 1);
        client.registerInstance(i);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRejectEmptyServiceType() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", "s", "", "h", 1);
        client.registerInstance(i);
    }

    @Test(expected=NullPointerException.class)
    public void testRejectNullHost() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", "s", "thrift", null, 1);
        client.registerInstance(i);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRejectEmptyHost() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", "s", "thrift", "", 1);
        client.registerInstance(i);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRejectNonPositivePort() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", "s", "thrift", "h", 0);
        client.registerInstance(i);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRejectApplicationSlash() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a/b", "s", "thrift", "h", 1);
        client.registerInstance(i);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRejectServiceNameSlash() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", "s/t", "thrift", "h", 1);
        client.registerInstance(i);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRejectServiceTypeSlash() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", "s", "x/y", "h", 1);
        client.registerInstance(i);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRejectHostSlash() throws DiscoveryException {
        ServiceInstance i = new NonvalidatingServiceInstance("a", "s", "x", "h/i", 1);
        client.registerInstance(i);
    }

    @Test
    public void testListApplications() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "thrift", "h", 1);
        ServiceInstance j = new BasicServiceInstance("b", "s", "thrift", "h", 2);
        client.registerInstance(i);
        client.registerInstance(j);

        List<String> applications = client.listApplications();
        assertEquals(ImmutableSet.of("a", "b"), ImmutableSet.copyOf(applications));
    }

    @Test
    public void testListApplicationsWithCommonService() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "thrift", "h", 1);
        ServiceInstance j = new BasicServiceInstance("", "s", "thrift", "h", 2);
        client.registerInstance(i);
        client.registerInstance(j);

        List<String> applications = client.listApplications();
        assertSingletonEquals("a", applications);
    }

    @Test
    public void testListServices() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "thrift", "h", 1);
        ServiceInstance j = new BasicServiceInstance("a", "t", "thrift", "h", 2);
        client.registerInstance(i);
        client.registerInstance(j);

        List<String> services = client.listServices("a");
        assertEquals(ImmutableSet.of("s", "t"), ImmutableSet.copyOf(services));
    }

    @Test
    public void testListServiceTypes() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("a", "s", "rest", "h", 1);
        ServiceInstance j = new BasicServiceInstance("a", "s", "thrift", "h", 2);
        client.registerInstance(i);
        client.registerInstance(j);

        List<String> serviceTypes = client.listServiceTypes("a", "s");
        assertEquals(ImmutableSet.of("rest", "thrift"), ImmutableSet.copyOf(serviceTypes));
    }

    @Test
    public void testListApplicationsWithNoApplicationsRegistered() throws DiscoveryException {
        List<String> applications = client.listApplications();

        assertEmpty(applications);
    }

    @Test
    public void testListApplicationsWithOnlyCommonServiceRegistered() throws DiscoveryException {
        ServiceInstance i = new BasicServiceInstance("", "s", "thrift", "h", 2);
        client.registerInstance(i);
        List<String> applications = client.listApplications();
        assertEmpty(applications);
    }

    @Test
    public void testListServicesWithNotServicesRegistered() throws DiscoveryException {
        List<String> services = client.listServices("a");
        assertEmpty(services);

        services = client.listServices("");
        assertEmpty(services);
    }
}
