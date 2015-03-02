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

package ezbake.common.ssl;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import ezbake.common.io.ClasspathResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * User: jhastings
 * Date: 2/24/14
 * Time: 11:29 AM
 */
public class SSLContextUtil {
    private static final Logger logger = LoggerFactory.getLogger(SSLContextUtil.class);

    public static KeyStore loadKeystore(String keystore, String password) throws SSLContextException, IOException {
        return loadKeystore(keystore, password, KeyStore.getDefaultType());
    }

    public static KeyStore loadKeystore(String keystore, String password, String type) throws SSLContextException, IOException {
        return loadKeystore(getInputStream(keystore), password, type);
    }

    public static KeyStore loadKeystore(InputStream keystore, String password, String type) throws SSLContextException, IOException {
        Preconditions.checkNotNull(keystore);
        Preconditions.checkNotNull(password);
        Preconditions.checkNotNull(type);

        KeyStore store;
        try {
            store = KeyStore.getInstance(type);
            store.load(keystore, password.toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            throw new SSLContextException("Failed to load keystore: "+keystore, e);
        }
        return store;
    }


    /**
     *
     * @param keystore
     * @param keystorePass
     * @param truststore
     * @param truststorePass
     * @return an SSL Context configured with the keystore and truststore
     * @throws SSLContextException
     * @throws IOException
     */
    public static SSLContext getSSLContext(
            String keystore,
            String keystorePass,
            String truststore,
            String truststorePass
    ) throws SSLContextException, IOException {
        return getSSLContext(
                keystore,
                keystorePass,
                KeyStore.getDefaultType(),
                truststore,
                truststorePass,
                KeyStore.getDefaultType()
        );
    }

    /**
     *
     * @param keystore
     * @param keystorePass
     * @param keystoreType
     * @param truststore
     * @param truststorePass
     * @param truststoreType
     * @return an SSL Context configured with the keystore and truststore
     * @throws SSLContextException
     * @throws IOException
     */
    public static SSLContext getSSLContext(
            String keystore,
            String keystorePass,
            String keystoreType,
            String truststore,
            String truststorePass,
            String truststoreType
    ) throws SSLContextException, IOException {
        SSLContext context;

        KeyStore keyKeyStore = null;
        KeyStore trustKeyStore = null;

        if (keystore != null) {
            keyKeyStore = SSLContextUtil.loadKeystore(keystore, keystorePass, keystoreType);
        }
        if (truststore != null) {
            trustKeyStore = SSLContextUtil.loadKeystore(truststore, truststorePass, truststoreType);
        }

        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyKeyStore, keystorePass.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustKeyStore);

            context = SSLContext.getInstance("SSL");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            throw new SSLContextException("Failed loading SSLContext", e);
        }

        return context;
    }

    protected static InputStream getInputStream(String path) throws IOException {

        InputStream stream;
        try {
            stream = Files.asByteSource(new File(path)).openStream();
        } catch (IOException e) {
            // Failed to open file stream. Try the classpath
            if (!path.startsWith("/")) {
                path = "/"+path;
            }
            URL resourceUrl = ClasspathResources.getResource(path);
            if (resourceUrl == null) {
                throw new IOException("Unable to load resource: " + path + " - not found");
            }
            stream = resourceUrl.openStream();
        }
        return stream;
    }
}
