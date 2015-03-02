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

package ezbake.configuration;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * User: jhastings
 * Date: 7/10/14
 * Time: 11:33 PM
 */
public class PropertiesConfigurationLoaderTest {

    @Test
    public void testLoadFromEmptyProperties() throws EzConfigurationLoaderException {
        EzConfiguration ezConfiguration = new EzConfiguration(new PropertiesConfigurationLoader(new Properties()));
        Assert.assertTrue(ezConfiguration.getProperties().isEmpty());
    }

    @Test
    public void testLoadFromProperties() throws EzConfigurationLoaderException {
        Properties p = new Properties();
        p.setProperty("test.property.one", "1");
        p.setProperty("test.property.two", "2");

        EzConfiguration ezConfiguration = new EzConfiguration(new PropertiesConfigurationLoader(p));
        Assert.assertEquals(p, ezConfiguration.getProperties());
    }
}
