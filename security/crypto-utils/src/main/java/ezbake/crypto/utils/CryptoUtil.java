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

import com.google.common.base.Joiner;
import ezbake.crypto.RSAKeyCrypto;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;

/**
 * User: jhastings
 * Date: 10/10/13
 * Time: 4:11 PM
 */
public class CryptoUtil {
    private static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);

    public static final String PEM_DASHES = "-----";
    public static final String PEM_BEGIN = "BEGIN";
    public static final String PEM_END = "END";
    public static final String PEM_KEY = "KEY";
    public static final String PEM_X509 = "CERTIFICATE";

    public static byte[] der(String pem) {
        return decode(stripPEMString(pem));
    }

    public static String stripPEMString(String pem) {
        return stripPEMString(PEM_KEY, pem);
    }

    public static String stripPEMString(String pemHeader, String pemString) {
        String ret = pemString;
        pemString = pemString.replaceAll("\r\n", "\n");
        pemString = pemString.replaceAll("[ ]+", "");
        if (pemString.startsWith(PEM_DASHES+PEM_BEGIN)
                && (pemString.endsWith(pemHeader+PEM_DASHES)
                || pemString.endsWith(pemHeader+PEM_DASHES+"\n"))) {
            ret = "";
            String[] lines = pemString.split("\n");
            for (int i = 1, len = lines.length-1; i < len; ++i) {
                ret += lines[i];
            }
        }
        ret = ret.replaceAll("\n", "");

        return ret;
    }

    protected static String prepareX509String(String x509) {
        String prepared = x509;
        String header = Joiner.on("").join(PEM_DASHES, PEM_BEGIN, " ", PEM_X509, PEM_DASHES);
        int index = x509.indexOf(header);
        if (index >= 0) {
            // X509 needs a newline after the header
            if (x509.charAt(index+header.length()) != '\n') {
                prepared = new StringBuilder(x509).insert(index+header.length(), "\n").toString();
            }
        } else {
            // Add the header
            prepared = header + "\n" + x509;
        }

        // Make sure the footer is there
        String footer = Joiner.on("").join(PEM_DASHES, PEM_END, " ", PEM_X509, PEM_DASHES);
        index = x509.indexOf(footer);
        if (index < 0) {
            prepared = prepared + footer;
        }
        return prepared;// prepared.replaceAll(" ", "");
    }

    public static String encode(byte[] data) {
        return new String(Base64.encode(data));
    }

    public static byte[] decode(String encoded) {
        return Base64.decode(encoded.getBytes());
    }


    public static byte[] load_p12(String ca, String cert, String privateKey) throws IOException {
        byte[] pkcs12_bytes;
        try {
            ca = prepareX509String(ca);
            cert = prepareX509String(cert);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(cert.getBytes()));
            Certificate cacert = certificateFactory.generateCertificate(new ByteArrayInputStream(ca.getBytes()));

            RSAKeyCrypto pkcrypto = new RSAKeyCrypto(privateKey, true);

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, "password".toCharArray());
            ks.setKeyEntry("application", pkcrypto.getPrivateKey(), "password".toCharArray(), new Certificate[]{certificate, cacert});

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ks.store(bos, "password".toCharArray());

            pkcs12_bytes = bos.toByteArray();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Unable to load pkcs12", e);
            throw new IOException("Failed to load pkcs12 from ca, cert, and private key", e);
        }

        return pkcs12_bytes;
    }
    public static byte[] load_jks(String x509cert) throws IOException {
        byte[] jks_bytes;
        try {
            x509cert = prepareX509String(x509cert);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(x509cert.getBytes()));

            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, "password".toCharArray());
            ks.setCertificateEntry("ezbakeca", certificate);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ks.store(bos, "password".toCharArray());

            jks_bytes = bos.toByteArray();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            logger.error("Unable to load jks", e);
            throw new IOException("Failed to load jks from ca", e);
        }

        return jks_bytes;
    }

}
