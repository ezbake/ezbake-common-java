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

import ezbake.configuration.constants.EzBakePropertyConstants;
import ezbake.security.utils.thrift.PingPong;
import ezbake.security.utils.thrift.PingPongHandler;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 3/6/14
 * Time: 8:31 AM
 */
public class TestThriftSSL {
    private static Logger log = LoggerFactory.getLogger(TestThriftSSL.class);
    private static int port = 9583;


    static Thread tserver;

    @BeforeClass
    public static void startThriftServer() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, InterruptedException {
        tserver = new Thread(new PingPongServer());
        tserver.start();
        Thread.sleep(2000);
    }

    @AfterClass
    public static void stopThriftServer() {
        if (tserver != null) {
            tserver.interrupt();
        }
    }

    @Test
    @Ignore("Needs to move to thrift-utils. Just keeping this here until that happens")
    public void testPing() {
        /*Properties props = new Properties();
        props.setProperty(ApplicationConfiguration.CERTIFICATES_DIRECTORY_KEY, "src/test/resources");
        props.setProperty(ApplicationConfiguration.SECURITY_ID_KEY, "appId");
        props.setProperty(SecurityConfiguration.APPLICATION_TRUSTSTORE_FILE, "keystore.jks");
        props.setProperty(SecurityConfiguration.APPLICATION_TRUSTSTORE_TYPE, "JKS");
        props.setProperty(SecurityConfiguration.APPLICATION_TRUSTSTORE_PASS, "password");
        props.setProperty(SecurityConfiguration.APPLICATION_KEYSTORE_FILE, "keystore.jks");
        props.setProperty(SecurityConfiguration.APPLICATION_KEYSTORE_TYPE, "JKS");
        props.setProperty(SecurityConfiguration.APPLICATION_KEYSTORE_PASS, "password");
        props.setProperty(SecurityConfiguration.SSL_PEER_AUTH_REQUIRED, String.valueOf(false));
        EZConfiguration config = new EZConfiguration(props);

        SecurityConfiguration securityConfiguration = SecurityConfiguration.fromConfiguration(config);
        TSSLTransportFactory.TSSLTransportParameters params = EzSSL.getTransportParams(securityConfiguration);

        TTransport transport = null;
        try {
            transport = EzSSLTransportFactory.getClientSocket("localhost", port, 0, params);
            log.debug("PingPong client connection to server on {} : {}", "localhost", port);
            TProtocol protocol = new TBinaryProtocol(transport);
            PingPong.Client client = new PingPong.Client(protocol);
            Assert.assertEquals("pong", client.ping());
        } catch (TTransportException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (TException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (transport != null) {
                transport.close();
            }
        }*/
    }

    static class PingPongServer implements Runnable {
        TSSLTransportFactory.TSSLTransportParameters params;

        public PingPongServer() throws NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException, KeyStoreException, IOException {
            Properties serverProps = new Properties();
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, "src/test/resources");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "appId");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_FILE, "keystore.jks");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_TYPE, "JKS");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_PASS, "password");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_FILE, "truststore.jks");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_TYPE, "JKS");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_PASS, "password");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_SSL_PEER_AUTH_REQUIRED, String.valueOf(false));

            params = EzSSL.getTransportParams(serverProps);
        }

        
        public void run() {
            PingPongHandler handler = new PingPongHandler();
            PingPong.Processor processor = new PingPong.Processor(handler);

            TServerTransport serverTransport;
            TServer server = null;
            try {
                serverTransport = TSSLTransportFactory.getServerSocket(port, 0, InetAddress.getByName(null), params);
                server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));
                log.debug("PingPong server starting on {} : {}", InetAddress.getByName(null), port);
                server.serve();
            } catch (TTransportException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } finally {
                if (server != null) {
                    server.stop();
                }
            }

        }
    }
}
