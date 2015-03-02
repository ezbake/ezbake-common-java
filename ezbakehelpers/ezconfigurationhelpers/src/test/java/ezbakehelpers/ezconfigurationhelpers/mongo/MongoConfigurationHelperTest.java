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

package ezbakehelpers.ezconfigurationhelpers.mongo;

import com.google.common.base.Splitter;
import ezbake.configuration.constants.EzBakePropertyConstants;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/7/14
 * Time: 8:49 AM
 */
public class MongoConfigurationHelperTest {
    static Properties configuration;

    @BeforeClass
    public static void setUp() {
        configuration = new Properties();
        configuration.put(EzBakePropertyConstants.MONGODB_HOST_NAME, "host1,host2,host3");
        configuration.put(EzBakePropertyConstants.MONGODB_PORT, 27017);
        configuration.put(EzBakePropertyConstants.MONGODB_USE_SSL, true);
        configuration.put(EzBakePropertyConstants.MONGODB_DB_NAME, "database");
        configuration.put(EzBakePropertyConstants.MONGODB_USER_NAME, "admin");
        configuration.put(EzBakePropertyConstants.MONGODB_PASSWORD, "secret");
    }

    @Test
    public void testHostName() {
        MongoConfigurationHelper helper = new MongoConfigurationHelper(configuration);
        Assert.assertEquals(
                Splitter.on(",").splitToList(configuration.getProperty(EzBakePropertyConstants.MONGODB_HOST_NAME)),
                helper.getMongoDBHostNames());
    }

    @Test
    public void testMongoConnectionString() {
        MongoConfigurationHelper helper = new MongoConfigurationHelper(configuration);
        String expected = "mongodb://admin:secret@host1:27017,host2:27017,host3:27017/database?ssl=true";
        Assert.assertEquals(expected, helper.getMongoConnectionString());
    }
}
