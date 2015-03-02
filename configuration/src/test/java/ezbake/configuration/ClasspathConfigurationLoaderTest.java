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
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;

/**
 * A unit test for the classpath configuration resource loader
 */
public class ClasspathConfigurationLoaderTest {

    @Test
    public void testDefaultClasspathResource() throws EzConfigurationLoaderException {
        
        /* 
         * These properties come from src/test/resources/ezbake-config.properties if properties in there change 
         * these will have to be updated. 
         */
        Properties expected = new Properties();

        expected.setProperty("accumulo.instance.name", "accumulo_instance");
        expected.setProperty("accumulo.zookeepers", "zoo1:2181,zoo2:2181");
        expected.setProperty("accumulo.use.mock", "true");
        expected.setProperty("accumulo.username", "test");
        expected.setProperty("accumulo.password", "user");

        expected.setProperty("TestNamespace.accumulo.username", "TestUserWithNamespace");

        expected.setProperty("application.name", "testapp");
        expected.setProperty("application.version", "1.3");
        expected.setProperty("service.name", "testservice");

        expected.setProperty("ezbake.security.app.id", "securityid");
        expected.setProperty("ezbake.security.ssl.dir", "../../../src/test/resources/ssl");
        expected.setProperty("ezbake.security.validate.peer", "True");
        expected.setProperty("ezbake.ssl.privatekey.file.testService", "test.priv");
        expected.setProperty("ezbake.ssl.servivekey.file.testService", "test.pub");

        expected.setProperty("thrift.use.ssl", "false");
        expected.setProperty("thrift.server.mode", "HsHa");

        expected.setProperty("mongodb.host.name", "localhost");
        expected.setProperty("mongodb.database.name", "testdatabase");

        expected.setProperty("zookeeper.connection.string", "zoo1:2181,zoo2:2181");
        expected.setProperty("test.accumulo.zookeepers", "test123.accumulo");

        EzConfiguration e = new EzConfiguration(new ClasspathConfigurationLoader());
        Assert.assertEquals(expected, e.getProperties());
    }

    @Test
    public void testPassedClasspathResource() throws EzConfigurationLoaderException {
        /*
         * These properties come from src/test/resources/test-ezbake-config.properties if properties in there change
         * these will have to be updated.
         */
        Properties expected = new Properties();
        expected.setProperty("test.property1", "one");
        expected.setProperty("test.property2", "two");
        EzConfiguration e = new EzConfiguration(new ClasspathConfigurationLoader("/test-ezbake-config.properties"));
        Assert.assertEquals(expected, e.getProperties());

    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNotLoadable() throws EzConfigurationLoaderException {
        ClasspathConfigurationLoader loader = new ClasspathConfigurationLoader("/NonExistent.properties");
        Assert.assertFalse(loader.isLoadable());
    }
    
    @Test
    public void testLoadingPropertiesNotInRootDirectory() throws EzConfigurationLoaderException {
        ClasspathConfigurationLoader loader = new ClasspathConfigurationLoader("/anotherdirectory/test.properties",
                                                                               "/anotherdirectory/test2.properties");
        /*
         * These properties come from src/test/resources/anotherdirectory/test.properties and  
         * src/test/resources/anotherdirectory/test2.properties if those change these will need to be updated.
         */
        Properties expected = new Properties(); 
        expected.setProperty("test.1", "1");
        expected.setProperty("test.2", "2");
        expected.setProperty("test.3", "3");
        expected.setProperty("test.4", "4");
        EzConfiguration e = new EzConfiguration(loader);
        Assert.assertEquals(expected, e.getProperties()); 
    }

    @Test
    public void testMultipleResourcesThatOverrideEachOther() throws EzConfigurationLoaderException {
        /*
         * These properties come from src/test/resources/overridingpropertiesdir/a.properties,
         * src/test/resources/overrideingpropertiesdir/b.properties, and
         * src/test/resources/overridingproperties/c.properties if those change these properties will need to be
         * updated.
         */
        Properties expected = new Properties();
        expected.setProperty("a.property", "a"); 
        expected.setProperty("b.property", "b"); 
        expected.setProperty("c.property", "c"); 
        expected.setProperty("a.property2", "a2"); 
        expected.setProperty("b.property2", "b2"); 
        expected.setProperty("c.property2", "c2"); 
        expected.setProperty("override.property", "c"); 
        
        ClasspathConfigurationLoader loader = new ClasspathConfigurationLoader("/overridingpropertiesdir/a.properties",    
                                                                               "/overridingpropertiesdir/b.properties",
                                                                               "/overridingpropertiesdir/c.properties");
        
        EzConfiguration e = new EzConfiguration(loader);
        Assert.assertEquals(expected, e.getProperties());
    } 

}
