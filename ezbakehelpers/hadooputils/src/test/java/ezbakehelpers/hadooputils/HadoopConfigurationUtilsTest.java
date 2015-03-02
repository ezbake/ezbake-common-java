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

package ezbakehelpers.hadooputils;

import org.apache.hadoop.conf.Configuration;
import java.util.Properties;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;


public class HadoopConfigurationUtilsTest {

    @Test
    public void testConfigurationFromProperties() {
        Properties props = new Properties();
        props.setProperty("myTestProperty", "foo");
        props.setProperty("myTestProperty2", "bar");
        Configuration configuration = HadoopConfigurationUtils.configurationFromProperties(props);
        String testProp1 = configuration.get("myTestProperty");
        Assert.assertTrue(testProp1 != null && testProp1.equals("foo"));
        String testProp2 = configuration.get("myTestProperty2");
        Assert.assertTrue(testProp2 != null && testProp2.equals("bar"));
    }

    @Test
    public void testPropertiesFromConfiguration() {
        Properties testProps = HadoopConfigurationUtils.propertiesFromConfiguration(null);
        Assert.assertTrue(testProps.isEmpty());
        Configuration configuration = new Configuration();
        configuration.set("myTestProperty", "foo");
        configuration.set("myTestProperty2", "bar");
        testProps = HadoopConfigurationUtils.propertiesFromConfiguration(configuration);
        String testProp1 = testProps.getProperty("myTestProperty");
        Assert.assertTrue(testProp1 != null && testProp1.equals("foo"));
        String testProp2 = testProps.getProperty("myTestProperty2");
        Assert.assertTrue(testProp2 != null && testProp2.equals("bar"));
    }

}
