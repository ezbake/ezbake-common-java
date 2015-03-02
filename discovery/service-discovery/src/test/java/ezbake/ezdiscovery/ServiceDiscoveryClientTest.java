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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Deprecated
public class ServiceDiscoveryClientTest
{
    private TestingServer server;
    private ServiceDiscoveryClient client;

    @Before
    public void setup() throws Exception
    {
        System.setProperty("curator-dont-log-connection-problems", "true");
        server = new TestingServer();
        client = new ServiceDiscoveryClient(server.getConnectString());
    }

    @After
    public void teardown()
    {
        try {
            client.close();
            server.close();
        } catch(Throwable e) {
            // TODO (soup): Figure out what to do here
        }


    }

    @Test
    public void testMakeZKPath()
    {
        Assert.assertTrue(client.makeZKPath("").equals("/"));
        Assert.assertTrue(client.makeZKPath("cookie").equals("/cookie"));
        Assert.assertTrue(client.makeZKPath("seasme", "street", "cookie", "monster", "is", "awesome")
                                .equals("/seasme/street/cookie/monster/is/awesome"));
    }

    @Test(expected=NullPointerException.class)
    public void testMakeZKPathNullInBetween()
    {
        client.makeZKPath("cookie", null, "monster", "is", "awesome");
    }


    @Test
    public void testRegistionUnRegistration() throws Exception
    {
        final String appName = "seasme_street";
        final String serviceName = "cookie_monster";
        client.registerEndpoint(appName, serviceName, "bigbird:2181");
        client.registerEndpoint(appName, serviceName, "elmo:2181");
        List<String> endPoints = client.getEndpoints(appName, serviceName);
        Assert.assertEquals(2, endPoints.size());
        Assert.assertEquals(Sets.newHashSet("elmo:2181", "bigbird:2181"), Sets.newHashSet(endPoints));

        client.unregisterEndpoint(appName, serviceName, "bigbird:2181");
        endPoints = client.getEndpoints(appName, serviceName);
        Assert.assertTrue(Iterables.elementsEqual(ImmutableList.of("elmo:2181"),
                                                  endPoints));

        client.unregisterEndpoint(appName, serviceName, "elmo:2181");
        endPoints = client.getEndpoints(appName, serviceName);
        Assert.assertTrue(endPoints.isEmpty());
    }

    @Test
    public void testUnregisterEndpointsThatDontExist() throws Exception
    {
        final String appName = "seasme_street";
        final String serviceName = "cookie_monster";

        // Make sure we don't throw any exceptions
        client.unregisterEndpoint(appName, serviceName, "does_not_exist:1234");
    }

    @Test
    public void testGetApplications() throws Exception {
        client.registerEndpoint("App1", "service", "bigbird:2181");
        client.registerEndpoint("App2", "service", "bigbird:2181");

        List<String> apps = client.getApplications();
        Collections.sort(apps);

        Assert.assertEquals(2, apps.size());
        Assert.assertTrue(Iterables.elementsEqual(ImmutableList.of("App1", "App2"), apps));
    }

    @Test
    public void testGetServices() throws Exception {
        final String appName = "App1";

        client.registerEndpoint(appName, "service1", "bigbird:2181");
        client.registerEndpoint(appName, "service2", "bigbird:2182");

        List<String> services = client.getServices(appName);
        Collections.sort(services);

        Assert.assertEquals(2, services.size());
        Assert.assertTrue(Iterables.elementsEqual(ImmutableList.of("service1", "service2"), services));

    }

    @Test
    public void testGetApplicationsWithNoApplications() throws Exception {
        List<String> apps = client.getApplications();
        Assert.assertTrue(apps.isEmpty());
    }


    @Test
    public void testGetServicesWithNotServicesRegistered() throws Exception {
        List<String> services = client.getServices();
        Assert.assertTrue(services.isEmpty());
    }

    @Test
    public void testGetEndpointsWithNothingRegistered() throws Exception {
        List<String> endPoints = client.getEndpoints("NONEXISTENT_SERVICE");
        Assert.assertTrue(endPoints.isEmpty());
    }

    @Test(expected=NullPointerException.class)
    public void testNullServiceRegistration() throws Exception {
        client.registerEndpoint("app", null, "bigbird:2181");
    }

    @Test(expected=RuntimeException.class)
    public void addSlashInService() throws Exception {
        client.registerEndpoint("app", "soup/foo", "bigbird:2181");
    }

    @Test
    public void addForwardSlashInAppFirstChar() throws Exception {
        String appName = "/app";
        String serviceName = "soup";
        client.registerEndpoint(appName, serviceName, "bigbird:2181");
        List<String> endPoints = client.getEndpoints(appName, serviceName);
        Assert.assertTrue(endPoints.size() == 1);
        Assert.assertTrue(Iterables.elementsEqual(ImmutableList.of("bigbird:2181"),
                                                  endPoints));
    }

    @Test(expected=RuntimeException.class)
    public void addSlashInApp() throws Exception {
        client.registerEndpoint("/ap/p", "soup", "bigbird:2181");
    }

    @Test(expected=RuntimeException.class)
    public void addSlashInEndpoint() throws Exception {
        client.registerEndpoint("/app", "soup", "bigbird://2181");
    }

    @Test
    public void setGetSecurityIdForApplicationTest() {
        try {
            String appName = "abby_cadabby";
            String id = "ID0001";

            client.setSecurityIdForApplication(appName, id);
            Assert.assertEquals(id, client.getSecurityIdForApplication(appName));
            Assert.assertTrue(Iterables.elementsEqual(ImmutableList.of(appName),
                                                     client.getApplications()));
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void setGetSecurityIdForCommonService() {
        try {
            String serviceName = "telly_monster";
            String id = "ID0002";

            client.setSecurityIdForCommonService(serviceName, id);
            Assert.assertEquals(id, client.getSecurityIdForCommonService(serviceName));
            Assert.assertTrue(Iterables.elementsEqual(ImmutableList.of(serviceName),
                                                                      client.getServices()));
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test(expected=IOException.class)
    public void testNullSecurityIdForApplilcation() throws Exception {
        client.setSecurityIdForApplication("abby_cadabby", null);
    }

    @Test(expected=IOException.class)
    public void testNullSecurityIdForCommonService() throws Exception {
        client.setSecurityIdForCommonService("telly_monster", null);
    }

    @Test
    public void testIsServiceCommonFalse() throws Exception
    {
        final String appName = "seasme_street";
        final String serviceName = "cookie_monster";
        client.registerEndpoint(appName, serviceName, "bigbird:2182");
        client.registerEndpoint(appName, serviceName, "elmo:2182");
        boolean isCommon = client.isServiceCommon(serviceName);
        Assert.assertFalse(isCommon);
    }

    @Test
    public void testIsServiceCommonTrue() throws Exception
    {
        final String serviceName = "Sheldon";
        client.registerEndpoint(serviceName, "bigbird:2182");
        client.registerEndpoint(serviceName, "elmo:2182");
        boolean isCommon = client.isServiceCommon(serviceName);
        Assert.assertTrue(isCommon);
    }
}
