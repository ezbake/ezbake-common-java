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
import ezbake.common.ssl.SSLContextUtil;
import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.crypto.PKeyCrypto;
import ezbake.crypto.RSAKeyCrypto;
import ezbakehelpers.ezconfigurationhelpers.application.EzBakeApplicationConfigurationHelper;
import ezbakehelpers.ezconfigurationhelpers.ssl.SslConfigurationHelper;
import org.apache.commons.io.IOUtils;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 2/24/14
 * Time: 11:14 AM
 */
public class EzSSL {
    private static final Logger log = LoggerFactory.getLogger(EzSSL.class);

    private static final String CLASSPATH_PREFIX = "/ssl";
    private static Boolean sslDefaultContextIsSet = false;

    /**
     *
     * @param configuration
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws KeyManagementException
     * @throws KeyStoreException
     */
    public static void setDefaultSSLContext(final Properties configuration) throws IOException, SSLContextException {
        setDefaultSSLContext(configuration, null);
    }

    /**
     *
     * @param configuration
     * @param service
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws KeyManagementException
     * @throws KeyStoreException
     */
    public static void setDefaultSSLContext(final Properties configuration, String service) throws IOException, SSLContextException {
        synchronized (sslDefaultContextIsSet) {
            if (!sslDefaultContextIsSet) {
                SSLContext.setDefault(getSSLContext(configuration, service));
                sslDefaultContextIsSet = true;
            }
        }
    }

    /**
     *
     * @param configuration
     * @return
     * @throws IOException
     * @throws UnrecoverableKeyException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public static SSLContext getSSLContext(final Properties configuration) throws IOException, SSLContextException {
        SslConfigurationHelper sslHelper = new SslConfigurationHelper(configuration);
        return SSLContextUtil.getSSLContext(
                sslHelper.getEzBakeKeyStorePath(),
                sslHelper.getKeystorePass(),
                sslHelper.getKeystoreType(),
                sslHelper.getEzBakeTrustStorePath(),
                sslHelper.getTruststorePass(),
                sslHelper.getTruststoreType()
        );
    }
    public static SSLContext getSSLContext(final Properties configuration, String serviceName) throws IOException, SSLContextException {
        SslConfigurationHelper sslHelper = new SslConfigurationHelper(configuration);
        return SSLContextUtil.getSSLContext(
                sslHelper.getServiceKeyStorePath(serviceName),
                sslHelper.getKeystorePass(serviceName),
                sslHelper.getKeystoreType(serviceName),
                sslHelper.getServiceTrustStorePath(serviceName),
                sslHelper.getTruststorePass(serviceName),
                sslHelper.getTruststoreType(serviceName)
        );
    }

    public static TSSLTransportFactory.TSSLTransportParameters getTransportParams(final Properties configuration) {
        SslConfigurationHelper sslHelper = new SslConfigurationHelper(configuration);
        TSSLTransportFactory.TSSLTransportParameters params = new TSSLTransportFactory.TSSLTransportParameters(
                sslHelper.getSslProtocol(),
                sslHelper.getSslCiphers()
        );

        File ks = getCertificatesDirectoryFile(configuration, sslHelper.getKeystoreFile());
        String ksUrl = ks.getAbsolutePath();
        params.setKeyStore(
                ksUrl,
                sslHelper.getKeystorePass(),
                null,
                sslHelper.getKeystoreType()
        );

        File ts = getCertificatesDirectoryFile(configuration, sslHelper.getTruststoreFile());
        String tsUrl = ts.getAbsolutePath();
        params.setTrustStore(
                tsUrl,
                sslHelper.getTruststorePass(),
                null,
                sslHelper.getTruststoreType()
        );

        params.requireClientAuth(sslHelper.isPeerAuthRequired());

        return params;
    }

    public static PKeyCrypto getCrypto(Properties configuration) throws IOException {
        SslConfigurationHelper sslHelper = new SslConfigurationHelper(configuration);

        PKeyCrypto crypto;
        try {
            crypto = new RSAKeyCrypto(IOUtils.toString(getISFromFileOrClasspath(configuration, sslHelper.getPrivateKeyFile())),
                    IOUtils.toString(getISFromFileOrClasspath(configuration, sslHelper.getEzSecurityPublic())));
        } catch (IOException e) {
            throw new IOException("Unable to initialize the asymmetric key crypto object for private: " + sslHelper.getPrivateKeyFile() +
                    ", public: " + sslHelper.getEzSecurityPublic(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("NoSuchAlgorithmException loading Asymmetric Keys", e);
        } catch (InvalidKeySpecException e) {
            throw new IOException("InvalidKeySpecException loading Asymmetric Keys: ", e);
        }
        return crypto;
    }


    /**
     *
     * @param source
     * @return
     * @throws IOException
     */
    public static InputStream getISFromFileOrClasspath(Properties configuration, String source) throws IOException {
        InputStream is;
        File inf = getCertificatesDirectoryFile(configuration, source);
        try {
            is = new FileInputStream(inf);
        } catch (FileNotFoundException e) {
            log.debug("Didn't find {} on filesystem. Trying classpath",inf.getPath());
            is = EzSSL.class.getClass().getResourceAsStream(inf.getPath());
            if (is == null) {
                // Need to use class.getResouce if java.lang.Class was laoded by a different classloader than EzSSL
                is = EzSSL.class.getResourceAsStream(inf.getPath());
            }
        }
        if (is == null) {
            throw new IOException("Unable to load file: " + inf.getPath() + " from filesystem, and " +
                    inf.getPath() + " not on classpath");
        }
        return is;
    }

    public static File getCertificatesDirectoryFile(Properties configuration, String file) {
        File certificateFile = getCertificatesDirectoryFile(configuration, file, false);
        if (!certificateFile.exists()) {
            log.debug("Unable to load certificates directory file: {} from file: {}", file, certificateFile);
            certificateFile = getCertificatesDirectoryFile(configuration, file, true);
            if (!certificateFile.exists()) {
                log.debug("Unable to load certificates directory file: {} from file: {}", file, certificateFile);
                certificateFile = getClasspathFile(CLASSPATH_PREFIX, file);
            }
        }
        log.debug("Using {} for certificates directory file: {}", certificateFile, file);

        return certificateFile;
    }
    protected static File getCertificatesDirectoryFile(Properties configuration, String file, boolean withSecId) {
        File f = new File(file);
        String certificateDirectory = getCertDir(configuration);
        String securityId = configuration.getProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID);

        // Either return a file path directly off the certificate dir or with security id between
        if (!withSecId) {
            f = new File(certificateDirectory, file);
        } else if (securityId != null) {
            f = new File(certificateDirectory, securityId);
            f = new File(f, file);
        }

        return f;
    }

    protected static File getClasspathFile(String prefix, String file) {
        File path = new File(file);
        if (prefix != null && !prefix.isEmpty()) {
            path = new File(prefix, file);
        }
        return path;
    }


    /**
     * Get the EzBake certificates directory from the ezconfig properties. If the property is null or empty use the
     * default classpath prefix.
     *
     * @param properties ezconfig properties
     * @return the path to the ezbake certificates directory
     */
    public static String getCertDir(Properties properties) {
        String certs = new EzBakeApplicationConfigurationHelper(properties).getCertificatesDir();
        if (certs == null || certs.isEmpty()) {
            certs = CLASSPATH_PREFIX;
        }
        return certs;
    }



}
