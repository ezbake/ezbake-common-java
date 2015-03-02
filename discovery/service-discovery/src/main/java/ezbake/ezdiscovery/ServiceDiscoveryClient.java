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

import com.google.common.net.HostAndPort;

import ezbakehelpers.ezconfigurationhelpers.application.EzBakeApplicationConfigurationHelper;
import ezbakehelpers.ezconfigurationhelpers.zookeeper.ZookeeperConfigurationHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;


public class ServiceDiscoveryClient implements ServiceDiscovery
{

    // Our curator constant
    private CuratorFramework zkClient = null;


    // The namespace in zookeeper which we will use
    public final static String NAMESPACE = "ezDiscovery";
    public final static String COMMON_SERVICE_APP_NAME = "common_services";


    protected final static String ENDPOINTS_ZK_PATH = "endpoints";

    protected final static String SECURITY_ZK_PATH = "security";
    protected final static String SECURITY_ID_NODE = "security_id";


    // Zookeeper retry constants
    private final static int MAX_NUM_OF_TRIES = 5;
    private final static int MILLISECONDS_BETWEEN_RETRIES = 10000;

    public ServiceDiscoveryClient(Properties configuration) {
       this(new ZookeeperConfigurationHelper(configuration).getZookeeperConnectionString());
    }

    /**
     * This establishes the connection to zookeeper so that we can look up services.
     */
    public ServiceDiscoveryClient(String zookeeperConnectString)
    {
        zkClient = CuratorFrameworkFactory.builder()
                                          .connectString(zookeeperConnectString)
                                          .namespace(NAMESPACE)
                                          .retryPolicy(new RetryNTimes(MAX_NUM_OF_TRIES, MILLISECONDS_BETWEEN_RETRIES))
                                          .build();
        zkClient.start();
    }

    public void registerEndpoint(final Properties configuration, final String endPoint) throws Exception {
        EzBakeApplicationConfigurationHelper appHelper = new EzBakeApplicationConfigurationHelper(configuration);
        registerEndpoint(appHelper.getApplicationName(), appHelper.getServiceName(), endPoint);
    }

    @Override
    public void registerEndpoint(final String serviceName, final String point) throws Exception {
        registerEndpoint(COMMON_SERVICE_APP_NAME, serviceName, point);
    }

    /**
     * Register a service end point for service discovery
     *
     *@param appName the name of the application that we are registering the service for
     *@param serviceName the name of the service that we are registering
     *@param point the host:port number of a service end point
     *
     *@throws Exception for any zookeeper errors
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    @Override
    public void registerEndpoint(final String appName, final String serviceName, final String point) throws Exception {
        HostAndPort.fromString(point); // use this to validate that we have a somewhat valid host and port string

        final String path = ServiceDiscoveryClient.makeZKPath(appName, serviceName, ENDPOINTS_ZK_PATH, point);
        try {
            zkClient.delete().forPath(path);
        } catch(KeeperException.NoNodeException e) {
            // Do nothing here, since if it doesn't exists its all good, as we are going to create it
        }
        zkClient.create().creatingParentsIfNeeded().forPath(path);
    }

    public void unregisterEndpoint(Properties configuration, String endPoint) throws Exception {
        EzBakeApplicationConfigurationHelper appHelper = new EzBakeApplicationConfigurationHelper(configuration);
        unregisterEndpoint(appHelper.getApplicationName(), appHelper.getServiceName(), endPoint);
    }

    @Override
    public void unregisterEndpoint(final String serviceName, final String point) throws Exception
    {
        unregisterEndpoint(COMMON_SERVICE_APP_NAME, serviceName, point);
    }

    /**
     * Unregister a service end point for service discovery
     *
     *@param appName the name of the application that we are registering the service for
     *@param serviceName the name of the service that we are registering
     *@param point the host:port number of a service end point
     *
     *@throws Exception for any zookeeper errors
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    @Override
    public void unregisterEndpoint(final String appName, final String serviceName, final String point) throws Exception
    {
        final String path = ServiceDiscoveryClient.makeZKPath(appName, serviceName, ENDPOINTS_ZK_PATH, point);
        try {
            zkClient.delete().forPath(path);
        } catch(KeeperException.NoNodeException e) {
            /*
             *  If the node doesn't exist when we unregister, lets just swallow the excpetion for now
             *  while we wait for this method  to return something to the user.
            */
        }
    }

    /**
     * Shut down our connection to zookeeper
     */
    @Override
    public void close()
    {
        zkClient.close();
    }

    /**
     * Get all applications registered with the client
     *
     * @return a list of strings containing the application names
     * @throws Exception on any errors
     */
    @Override
    public List<String> getApplications() throws Exception {
        return zkClient.getChildren().forPath("");
    }

    public List<String> getServices() throws Exception {
        return getServices(COMMON_SERVICE_APP_NAME);
    }

    /**
     * Get all services registered under a given application
     *
     * @param appName the application for which to list services
     *
     * @return a list of strings corresponding to the service names for the application
     * @throws Exception on zookeeper errors
     */
    @Override
    public List<String> getServices(final String appName) throws Exception {
        String zkPath = ServiceDiscoveryClient.makeZKPath(appName);
        if(zkClient.checkExists().forPath(zkPath) == null) {
            return Collections.emptyList();
        }
        return zkClient.getChildren().forPath(zkPath);
    }

    /**
     * Gets the endpoints for ezbake common services
     *
     * @param serviceName the name of the common service that we want to retrieve endpoints for
     *
     * @throws Exception if there was a problem getting the endpoints
     */
    @Override
    public List<String> getEndpoints(final String serviceName) throws Exception
    {
        return getEndpoints(COMMON_SERVICE_APP_NAME, serviceName);
    }

    /**
     * Get the end points for a service in an application
     *
     *@param appName the name of the application that we are registering the service for
     *@param serviceName the name of the service that we are registering
     *
     *@throws Exception for any zookeeper errors
     *
     *@return a list of strings containt host:port strings for the service end points
     */
    @Override
    public List<String> getEndpoints(final String appName, final String serviceName) throws Exception
    {
        String zkPath = ServiceDiscoveryClient.makeZKPath(appName, serviceName, ENDPOINTS_ZK_PATH);
        if(zkClient.checkExists().forPath(zkPath) == null) {
            return Collections.emptyList();
        }
        return zkClient.getChildren().forPath(zkPath);
    }

    /**
     * Determine if a service is common or not
     *
     *@param serviceName the name of the service
     *
     *@throws Exception for any zookeeper errors
     *
     *@return boolean
     */
    @Override
    public boolean isServiceCommon( final String serviceName) throws Exception
    {
        String zkPath = ServiceDiscoveryClient.makeZKPath(COMMON_SERVICE_APP_NAME, serviceName);
        if(zkClient.checkExists().forPath(zkPath) == null) {
            return false;
        }
        return true;
    }

    public static String makeZKPath(String ... paths)
    {
        if(paths.length == 0)
        {
            return ZKPaths.makePath("", "");
        }

        if(paths.length == 1)
        {
            return ZKPaths.makePath(paths[0], "");
        }

        String zkPath = paths[0];
        if(zkPath.indexOf('/', 1) != -1) {
            throw new RuntimeException("zookeeper parts should not contain '/' " + zkPath);
        }

        for(int i = 1;i < paths.length; ++i)
        {
            String child = paths[i];

            if(child == null && i < paths.length) {
                throw new NullPointerException();
            }

            if(child.contains("/")) {
                throw new RuntimeException("zookeeper parts should not contain '/' " + child);
            }

            zkPath = ZKPaths.makePath(zkPath, child);
        }

        return zkPath;
    }

    /**
     * Sets the security Id for an application
     *
     *@param applicationName the name of the application that we are setting it's security Id
     *@param securityId the security Id we are setting
     *
     *@throws IOException for any zookeeper errors
     */
    @Override
    public void setSecurityIdForApplication(String applicationName, String securityId) throws IOException
    {
        if(applicationName == null || securityId == null) {
            throw new IOException("application name or securityId should not be null!");
        }
        String zkNode = ServiceDiscoveryClient.makeZKPath(applicationName, SECURITY_ZK_PATH, SECURITY_ID_NODE);
        setNodeForPath(zkNode);

        try {
        	zkClient.setData().forPath(zkNode, securityId.getBytes());
        }
        catch(Exception e) {
        	throw new IOException(e);
        }
    }

    /**
     * Sets the security Id for a common service
     *
     *@param serviceName the name of the common service that we are setting it's security Id
     *@param securityId the security Id we are setting
     *
     *@throws IOException for any zookeeper errors
     */
    @Override
    public void setSecurityIdForCommonService(String serviceName, String securityId) throws IOException {
        if(securityId == null || serviceName == null) {
            throw new IOException("service name or securityId should not be null!");
        }
        String zkNode = ServiceDiscoveryClient.makeZKPath(COMMON_SERVICE_APP_NAME, serviceName, SECURITY_ZK_PATH, SECURITY_ID_NODE);

        setNodeForPath(zkNode);

        try {
        	zkClient.setData().forPath(zkNode, securityId.getBytes());
        }
        catch(Exception e) {
        	throw new IOException(e);
        }

    }

    /**
     * Gets the security Id for an application
     *
     *@param applicationName the name of the application that we are getting it's security Id
     *
     *@throws IOException for any zookeeper errors
     *
     *@return the security Id of the application
     */
    @Override
    public String getSecurityIdForApplication(String applicationName) throws IOException
    {
        String zkNode = ServiceDiscoveryClient.makeZKPath(applicationName, SECURITY_ZK_PATH, SECURITY_ID_NODE);
        byte[] secIdData = null;

        try {
        	secIdData = zkClient.getData().forPath(zkNode);
        }
        catch(Exception e) {
        	throw new IOException(e);
        }

        return new String(secIdData);
    }

    /**
     * Gets the security Id for a common service
     *
     *@param serviceName the name of the common service that we are getting it's security Id
     *
     *@throws IOException for any zookeeper errors
     *
     *@return the security Id of the common service
     */
    @Override
    public String getSecurityIdForCommonService(String serviceName) throws IOException
    {
        String zkNode = ServiceDiscoveryClient.makeZKPath(COMMON_SERVICE_APP_NAME, serviceName, SECURITY_ZK_PATH, SECURITY_ID_NODE);

        byte[] secIdData = null;

        try {
        	secIdData = zkClient.getData().forPath(zkNode);
        }
        catch(Exception e) {
        	throw new IOException(e);
        }

        return new String(secIdData);
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private void setNodeForPath(String node) throws IOException
    {
        try {
            try {
                zkClient.delete().forPath(node);
            } catch(KeeperException.NoNodeException e) {
                // Do nothing here, since if it doesn't exists its all good, as we are going to create it
            }

            //create path and assign node
            zkClient.create().creatingParentsIfNeeded().forPath(node);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
