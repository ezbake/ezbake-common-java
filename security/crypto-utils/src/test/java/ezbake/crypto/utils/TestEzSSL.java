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

package ezbake.crypto.utils;

import ezbake.common.ssl.SSLContextException;
import ezbake.configuration.ClasspathConfigurationLoader;
import ezbake.configuration.EzConfiguration;
import ezbake.configuration.EzConfigurationLoaderException;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.crypto.PKeyCrypto;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 2/24/14
 * Time: 12:03 PM
 */
public class TestEzSSL {
    private static Properties ezConfiguration;

    private static final String SSL_JAR = "/ssl.jar";

    @BeforeClass
    public static void setupClass() throws IOException, EzConfigurationLoaderException {
        loadJar(TestEzSSL.class.getClass().getResource(SSL_JAR));
        ezConfiguration = new EzConfiguration(new ClasspathConfigurationLoader()).getProperties();
    }

    @Test
    public void testGetSslContext() throws IOException, SSLContextException {
        EzSSL.getSSLContext(ezConfiguration);
    }

    @Test
    public void testCertDirPath() {
        String path = EzSSL.getCertificatesDirectoryFile(ezConfiguration, "application.p12").getPath();
        Assert.assertEquals("src/test/resources/application.p12", path);

    }

    @Test
    public void testClasspathPath() {
        String path = EzSSL.getClasspathFile("/ssl", "application.p12").getPath();
        Assert.assertEquals("/ssl/application.p12", path);
    }

    @Test
    public void testGetCDFile() {
        File p = EzSSL.getCertificatesDirectoryFile(new Properties(), "Test");

        Assert.assertNotNull(p);
    }

    @Test
    public void testFileInConfSecurityId() {
        Properties p = new Properties();
        p.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, "src/test");
        p.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "resources");

        String path = EzSSL.getCertificatesDirectoryFile(p, "application.p12").getPath();
        Assert.assertEquals("src/test/resources/application.p12", path);
    }

    @Test
    public void testLoadCryptoClasspathJar() throws IOException {
        Properties p = new Properties();
        p.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, "src/test/resources");
        p.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "securityid");
        p.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_PRIVATE_KEY_FILE, "application.priv");
        p.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_PUBLIC_KEY_FILE, "application.pub");

        PKeyCrypto crypto = EzSSL.getCrypto(p);
        Assert.assertNotNull(crypto);
        Assert.assertTrue(crypto.hasPrivate());
        Assert.assertTrue(crypto.hasPublic());
    }

    @Test(expected=IOException.class)
    public void testLoadCryptoClasspathJarNotExisting() throws IOException {
        Properties p = new Properties();
        p.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, "src/test/resources");
        p.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "securityid");
        p.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_PRIVATE_KEY_FILE, "doesnotexist.priv");
        p.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_PUBLIC_KEY_FILE, "doesnotexist.pub");

        EzSSL.getCrypto(p);
    }

    @Test
    public void getCryptoTestKeys() throws IOException {
        Properties p = new Properties();
        p.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, "src/test/resources");
        p.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "securityid");
        EzSSL.getCrypto(p);
    }

    private static void loadJar(URL u) throws IOException {
        System.out.println("Jar: " + u);
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class[] parameters = new Class[] {URL.class};
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] {u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
}
