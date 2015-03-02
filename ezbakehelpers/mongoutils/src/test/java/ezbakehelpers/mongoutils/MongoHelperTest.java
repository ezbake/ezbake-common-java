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

package ezbakehelpers.mongoutils;

import com.mongodb.MongoCredential;
import ezbake.configuration.constants.EzBakePropertyConstants;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/8/14
 * Time: 9:07 AM
 */
public class MongoHelperTest {

    @Test
    public void testCredentials() {
        Properties properties = new Properties();
        properties.setProperty(EzBakePropertyConstants.MONGODB_USER_NAME, "user");
        properties.setProperty(EzBakePropertyConstants.MONGODB_PASSWORD, "password");
        properties.setProperty(EzBakePropertyConstants.MONGODB_DB_NAME, "database");

        MongoHelper mh = new MongoHelper(properties);
        MongoCredential credential = mh.getMongoCredential();
        Assert.assertEquals(properties.getProperty(EzBakePropertyConstants.MONGODB_USER_NAME), credential.getUserName());
        Assert.assertEquals(properties.getProperty(EzBakePropertyConstants.MONGODB_DB_NAME), credential.getSource());
        Assert.assertArrayEquals(properties.getProperty(EzBakePropertyConstants.MONGODB_PASSWORD).toCharArray(),
                credential.getPassword());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCredentialUserNamePreconditions() {
        Properties properties = new Properties();
        properties.setProperty(EzBakePropertyConstants.MONGODB_PASSWORD, "password");
        properties.setProperty(EzBakePropertyConstants.MONGODB_DB_NAME, "database");
        MongoHelper mh = new MongoHelper(properties);
        mh.getMongoCredential();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCredentialPasswordPreconditions() {
        Properties properties = new Properties();
        properties.setProperty(EzBakePropertyConstants.MONGODB_USER_NAME, "username");
        properties.setProperty(EzBakePropertyConstants.MONGODB_DB_NAME, "database");
        MongoHelper mh = new MongoHelper(properties);
        mh.getMongoCredential();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCredentialDbNamePreconditions() {
        Properties properties = new Properties();
        properties.setProperty(EzBakePropertyConstants.MONGODB_USER_NAME, "username");
        properties.setProperty(EzBakePropertyConstants.MONGODB_PASSWORD, "password");
        MongoHelper mh = new MongoHelper(properties);
        mh.getMongoCredential();
    }

}
