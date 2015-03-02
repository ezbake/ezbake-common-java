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

package ezbakehelpers.ezconfigurationhelpers.elasticsearch;

import com.google.common.collect.ImmutableList;
import ezbake.configuration.constants.EzBakePropertyConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/21/14
 * Time: 2:39 PM
 */
public class ElasticsearchConfigurationHelperTest {

    @Test
    public void testGetHosts() {
        Properties properties = new Properties();

        ElasticsearchConfigurationHelper emptyHelper = new ElasticsearchConfigurationHelper(properties);
        Assert.assertTrue(emptyHelper.getElasticsearchHosts().isEmpty());
        Assert.assertNull(emptyHelper.getElasticsearchHost());
        Assert.assertNull(emptyHelper.getElasticsearchHostWithPort());

        properties.put(EzBakePropertyConstants.ELASTICSEARCH_HOST, "host1");
        ElasticsearchConfigurationHelper singleHostHelper = new ElasticsearchConfigurationHelper(properties);
        Assert.assertEquals(ImmutableList.of("host1"), singleHostHelper.getElasticsearchHosts());
        Assert.assertEquals("host1", singleHostHelper.getElasticsearchHost());
        Assert.assertEquals("host1:9300", singleHostHelper.getElasticsearchHostWithPort());

        properties.put(EzBakePropertyConstants.ELASTICSEARCH_HOST, "host1,host2");
        ElasticsearchConfigurationHelper multiHostHelper = new ElasticsearchConfigurationHelper(properties);
        Assert.assertEquals(ImmutableList.of("host1", "host2"), multiHostHelper.getElasticsearchHosts());
        Assert.assertEquals("host1,host2", multiHostHelper.getElasticsearchHost());
        Assert.assertEquals("host1:9300,host2:9300", multiHostHelper.getElasticsearchHostWithPort());

        properties.put(EzBakePropertyConstants.ELASTICSEARCH_HOST, "host1    ,   host2");
        multiHostHelper = new ElasticsearchConfigurationHelper(properties);
        Assert.assertEquals(ImmutableList.of("host1", "host2"), multiHostHelper.getElasticsearchHosts());
        Assert.assertEquals("host1,host2", multiHostHelper.getElasticsearchHost());
        Assert.assertEquals("host1:9300,host2:9300", multiHostHelper.getElasticsearchHostWithPort());

        properties.put(EzBakePropertyConstants.ELASTICSEARCH_HOST, "host1    ,,   host2,");
        multiHostHelper = new ElasticsearchConfigurationHelper(properties);
        Assert.assertEquals(ImmutableList.of("host1", "host2"), multiHostHelper.getElasticsearchHosts());
        Assert.assertEquals("host1,host2", multiHostHelper.getElasticsearchHost());
        Assert.assertEquals("host1:9300,host2:9300", multiHostHelper.getElasticsearchHostWithPort());
    }
}
