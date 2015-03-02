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

import com.google.common.collect.Multimap;
import org.apache.curator.test.TestingServer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ServicePurgeClientTest {
    private TestingServer server;
    private ServicePurgeClient client;

    @Before
    public void setup() throws Exception {
        System.setProperty("curator-dont-log-connection-problems", "true");
        server = new TestingServer();
        client = new ServicePurgeClient(server.getConnectString());
    }

    @After
    public void teardown() {
        try {
            client.close();
            server.close();
        } catch(Throwable e) {
            // TODO (soup): Figure out what to do here
        }
    }

    @Test
    public void testGetPurgeSerivesWithNothingLoggedReturnsEmptyMap() throws Exception {
        Multimap<String, String> purgeList = client.getPurgeServices();
        Assert.assertEquals(0, purgeList.size());
    }

    @Test
    public void testAddPurgeServiceAndGetPurgeServices() throws Exception {
        final String appName = "Lannister";
        final String serviceName = "Tywin";
        final String serviceName2 = "Cersei";
        final String appName2 = "Baratheon";
        final String serviceName3 = "Robert";

        client.addPurgeService(appName, serviceName);
        client.addPurgeService(appName, serviceName2);
        client.addPurgeService(appName2, serviceName3);
        Multimap<String, String> purgeList = client.getPurgeServices();
        Assert.assertTrue(purgeList.containsKey(appName));
        Assert.assertEquals(2, purgeList.keySet().size());
        Assert.assertEquals(3, purgeList.size());
        Assert.assertEquals(2, purgeList.get(appName).size());
    }

    @Test
    public void testAddPurgeServiceDuplicatesOverwriteNotDuplicateEntries() throws Exception {
        final String appName = "Lannister";
        final String serviceName = "Tywin";
        client.addPurgeService(appName, serviceName);
        Multimap<String, String> purgeList = client.getPurgeServices();
        Assert.assertEquals(1, purgeList.size());
        purgeList = null;
        client.addPurgeService(appName, serviceName);
        purgeList = client.getPurgeServices();
        Assert.assertEquals(1, purgeList.size());
    }

    @Test
    public void TestRemovePurgeService() throws Exception {
        final String appName = "Lannister";
        final String serviceName = "Tywin";
        client.addPurgeService(appName, serviceName);
        Multimap<String, String> purgeList = client.getPurgeServices();
        Assert.assertEquals(1, purgeList.size());
        purgeList = null;
        client.removePurgeService(appName, serviceName);
        purgeList = client.getPurgeServices();
        Assert.assertEquals(0, purgeList.size());
    }

    @Test
    public void TestRemovePurgeServiceOnNonexistingServiceDoesNothing() throws Exception {
        final String appName = "Lannister";
        final String serviceName = "Tywin";
        final String serviceName2 = "Cersei";
        client.addPurgeService(appName, serviceName);
        Multimap<String, String> purgeList = client.getPurgeServices();
        Assert.assertEquals(1, purgeList.size());
        purgeList = null;
        client.removePurgeService(appName, serviceName2);
        purgeList = client.getPurgeServices();
        Assert.assertEquals(1, purgeList.size());
    }

    @Test
    public void TestGetPurgeServicesForApplication() throws Exception {
        final String appName = "Lannister";
        final String serviceName = "Tywin";
        final String serviceName2 = "Cersei";
        final String appName2 = "Baratheon";
        final String serviceName3 = "Robert";

        client.addPurgeService(appName, serviceName);
        client.addPurgeService(appName, serviceName2);
        client.addPurgeService(appName2, serviceName3);

        List<String> servicesForAppName = client.getPurgeServicesForApplication(appName);
        List<String> servicesForAppName2 = client.getPurgeServicesForApplication(appName2);
        List<String> servicesForNonExistingAppName = client.getPurgeServicesForApplication("DNE");
        Assert.assertEquals(2, servicesForAppName.size());
        Assert.assertEquals(1, servicesForAppName2.size());
        Assert.assertEquals(0, servicesForNonExistingAppName.size());
    }
}
