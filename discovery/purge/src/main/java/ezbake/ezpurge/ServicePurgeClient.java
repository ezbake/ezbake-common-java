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

package ezbake.ezpurge;

/**
 * Created by sstapleton on 7/15/14.
 */

import java.util.Collections;
import java.util.List;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import ezbake.ezdiscovery.ServiceDiscoveryClient;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException;

public class ServicePurgeClient implements EzServicePurge {

    private final static int MAX_NUM_OF_TRIES = 5;
    private final CuratorFramework zkClient;

    public final static String PURGE_NAMESPACE = "ezpurge";

    /**
     * This establishes the connection to zookeeper so that we can look up services.
     */
    public ServicePurgeClient(String zookeeperConnectionString) {
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperConnectionString)
                .namespace(PURGE_NAMESPACE)
                .retryPolicy(new RetryNTimes(MAX_NUM_OF_TRIES, 1000))
                .build();
        zkClient.start();
    }

    /**
     * Registers an application service to be purged
     *
     * @param appName the name of the application that we are setting the service to be purged
     * @param serviceName the name of the service that we are setting to be purged
     * @throws Exception for any zookeeper errors
     */
    public void addPurgeService(String appName, String serviceName) throws Exception {
        final String path = ServiceDiscoveryClient.makeZKPath(appName, serviceName);
        try {
            zkClient.delete().forPath(path);
        } catch(KeeperException.NoNodeException e) {
            // Do nothing here, since if it doesn't exists its all good, as we are going to create it
        }
        zkClient.create().creatingParentsIfNeeded().forPath(path);
    }

    /**
     * Retrieves a list of services to be purged for a given application
     * @param appName
     * @return List<String> of services
     * @throws Exception for any zookeeper errors
     */
    public List<String> getPurgeServicesForApplication(String appName) throws Exception {
        String path = ServiceDiscoveryClient.makeZKPath(appName);
        try {
            return zkClient.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException ignored) {
            //if NoNodeException error occurred then there are no results. return empty list
            return Collections.emptyList();
        }
    }

    /**
     * retrieves a map of services to applications
     * @return Multimap containing the Applications as keys and a list of services as the value
     * @throws Exception for any zookeeper errors
     */
    public Multimap<String, String> getPurgeServices() throws Exception {
        Multimap<String, String> purgeServices = ArrayListMultimap.create();
        List<String> applications = zkClient.getChildren().forPath("");
        for(String app: applications){
            String path = ServiceDiscoveryClient.makeZKPath(app);
            List<String> services = zkClient.getChildren().forPath(path);
            purgeServices.putAll(app, services);
        }
        return purgeServices;
    }

    /**
     * Removes a service from the list to be purged
     * @param appName the name of the application that we are remove the service from purge list
     * @param serviceName the name of the service that we are removing from the purge list
     * @throws Exception for any zookeeper errors
     */
    public void removePurgeService(String appName, String serviceName) throws Exception {
        final String path = ServiceDiscoveryClient.makeZKPath(appName, serviceName);
        try {
            zkClient.delete().forPath(path);
        } catch(KeeperException.NoNodeException ignored) {
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
}