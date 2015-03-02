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

import com.google.common.net.HostAndPort;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.annotations.Beta;

/**
 * A ServiceRegistry that stores its data in ZooKeeper.
 *
 * Endpoints are stored in the ZooKeeper tree in paths of the form:
 * <pre>
 *     /"ezDiscovery"/applicationName/serviceName/serviceType/hostname:port
 * </pre>
 * where "ezDiscovery" is fixed string (and doesn't contain quotation marks
 * when stored in ZooKeeper). Because ZooKeeper uses "/" to denote a hierarchy
 * of nodes, "/" must not be contained in an application name or service name.
 *
 * To maintain compatibility with Service Discovery in EzBake 1.3, if the
 * service type is "thrift", then the words "endpoints" is used in place of
 * the actual service type.
 */
@Beta
public class ZooKeeperServiceRegistry implements ServiceRegistry, Closeable
{
    private final CuratorFramework client;
    private final InterProcessReadWriteLock readWriteLock;

    private static final int DEFAULT_MAX_NUM_OF_TRIES = 5;
    private static final int DEFAULT_TIMEOUT = 1000;
    private static final String EZ_DISCOVERY_NAMESPACE = "/ezDiscovery";
    private static final String GLOBAL_LOCK_PATH = "/ezDiscovery-internal/global-read-write-lock";

    /**
     * This establishes the connection to zookeeper so that we can look up services.
     */
    public ZooKeeperServiceRegistry(String zookeeperConnectString) {
        // We don't use .namespace() since we need the data stored in its own
        // path to maintain backwards-compatibility. We fake namespaces on our
        // own in formatZooKeeperApplicationPath.
        client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperConnectString)
                .retryPolicy(new RetryNTimes(DEFAULT_MAX_NUM_OF_TRIES, DEFAULT_TIMEOUT))
                .build();
        client.start();

        readWriteLock = new InterProcessReadWriteLock(client, GLOBAL_LOCK_PATH);
    }

    /**
     * Shut down our connection to zookeeper
     */
    @Override
    public void close() {
        client.close();
    }

    @Override
    public void registerInstance(ServiceInstance instance) throws DiscoveryException {
        String path = formatZooKeeperEndpointPath(instance);
        InterProcessLock writeLock = readWriteLock.writeLock();
        Exception error = null;

        try {
            writeLock.acquire();

            if (client.checkExists().forPath(path) == null) {
                client.create().creatingParentsIfNeeded().forPath(path);
            }
        } catch(Exception e) {
            error = e;
        } finally {
            safelyRelease(writeLock, error);
        }
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    @Override
    public void unregisterInstance(ServiceInstance instance) throws DiscoveryException {
        String path = formatZooKeeperEndpointPath(instance);
        InterProcessLock writeLock = readWriteLock.writeLock();
        Exception error = null;

        try {
            writeLock.acquire();
            client.delete().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            // Don't care since we were trying to delete the node anyway
        } catch(Exception e) {
            error = e;
        } finally {
            safelyRelease(writeLock, error);
        }
    }

    @Override
    public List<String> listApplications() throws DiscoveryException {
        List<String> applicationNames = childrenOrEmpty(EZ_DISCOVERY_NAMESPACE);
        applicationNames.remove(ServiceDiscoveryConstants.COMMON_SERVICES_APPLICATION_NAME);

        return applicationNames;
    }

    @Override
    public List<String> listServices(String applicationName) throws DiscoveryException {
        return childrenOrEmpty(formatZooKeeperApplicationPath(applicationName));
    }

    @Override
    public List<String> listServiceTypes(String applicationName, String serviceName) throws DiscoveryException {
        List<String> serviceTypes = childrenOrEmpty(formatZooKeeperServicePath(applicationName, serviceName));
        // 1.3 compatibility
        if (serviceTypes.contains("endpoints")) {
            serviceTypes.remove("endpoints");
            serviceTypes.add("thrift");
        }

        return serviceTypes;
    }

    @Override
    public List<ServiceInstance> listInstances(String applicationName, String serviceName, String serviceType)
            throws DiscoveryException {
        String path = formatZooKeeperServiceTypePath(applicationName, serviceName, serviceType);
        List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        InterProcessLock readLock = readWriteLock.readLock();
        Exception maybeError = null;

        try {
            List<String> children;
            readLock.acquire();

            if (client.checkExists().forPath(path) == null) {
                children = Collections.emptyList();
            } else {
                children = client.getChildren().forPath(path);
            }

            for (String c : children) {
                HostAndPort hostAndPort = HostAndPort.fromString(c);
                instances.add(new BasicServiceInstance(applicationName, serviceName, serviceType,
                        hostAndPort.getHostText(), hostAndPort.getPort()));
            }

        } catch (Exception e) {
            maybeError = e;
        } finally {
            safelyRelease(readLock, maybeError);
        }

        return instances;
    }

    /**
     * Return a list of the children of a path or empty if the path does not exist.
     *
     * @param path ZooKeeper path to return children of
     * @return list of children of the path or empty if the path does not exist
     * @throws DiscoveryException on ZooKeeper error
     */
    private List<String> childrenOrEmpty(String path) throws DiscoveryException {
        List<String> children = null;
        InterProcessLock readLock = readWriteLock.readLock();
        Exception error = null;

        try {
            readLock.acquire();

            if (client.checkExists().forPath(path) == null) {
                children = Collections.emptyList();
            } else {
                children = client.getChildren().forPath(path);
            }
        } catch (Exception e) {
            error = e;
        } finally {
            safelyRelease(readLock, error);
        }

        return children;
    }

    /**
     * Allow a lock to be released within a finally block.
     *
     * If parentError is non-null, it is thrown so that the original cause of
     * the exception can be reported back to the caller rather than hiding it
     * behind a potential error when releasing the lock.
     *
     * @param lock lock to release
     * @param parentError the exception causing us to enter the finally block or null if none exists
     * @throws DiscoveryException if the lock cannot be released
     */
    private void safelyRelease(InterProcessLock lock, Exception parentError) throws DiscoveryException {
        try {
            lock.release();
        } catch (Exception e) {
            if (parentError != null) {
                throw new DiscoveryException(parentError);
            } else {
                throw new DiscoveryException(e);
            }
        }
    }

    private static boolean isValidName(String name) {
      return name != null && !name.isEmpty() && !name.contains("/");
    }

    private static boolean isCommonService(String applicationName) {
        return applicationName.isEmpty();
    }

    private static String formatInstance(ServiceInstance instance) {
        return String.format("%s:%d", instance.getHost(), instance.getPort());
    }

    private static String formatZooKeeperApplicationPath(String applicationName) {
        if (isCommonService(applicationName)) {
            applicationName = ServiceDiscoveryConstants.COMMON_SERVICES_APPLICATION_NAME;
        }

        checkArgument(isValidName(applicationName));

        return ZKPaths.makePath(EZ_DISCOVERY_NAMESPACE, applicationName);
    }

    private static String formatZooKeeperServicePath(String applicationName, String serviceName) {
        checkNotNull(serviceName);
        checkArgument(isValidName(serviceName));

        return ZKPaths.makePath(formatZooKeeperApplicationPath(applicationName), serviceName);
    }

    private static String formatZooKeeperServiceTypePath(String applicationName,
                                                         String serviceName,
                                                         String serviceType) {
        checkNotNull(serviceType);
        checkArgument(isValidName(serviceType));

        // 1.3 compatibility
        if (serviceType.equals("thrift")) {
            serviceType = "endpoints";
        }

        return ZKPaths.makePath(formatZooKeeperServicePath(applicationName, serviceName), serviceType);
    }

    private static String formatZooKeeperServiceTypePath(ServiceInstance instance) {
        return formatZooKeeperServiceTypePath(instance.getApplicationName(), instance.getServiceName(),
                instance.getServiceType());
    }

    private static String formatZooKeeperEndpointPath(ServiceInstance instance) {
        checkNotNull(instance);
        checkNotNull(instance.getHost());
        checkArgument(isValidName(instance.getHost()));
        checkArgument(instance.getPort() > 0);

        return ZKPaths.makePath(formatZooKeeperServiceTypePath(instance), formatInstance(instance));
    }
}
