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
import ezbake.configuration.constants.EzBakePropertyConstants;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 3/5/14
 * Time: 11:05 AM
 */
public class TestSSLServer {
    private static int port = 9584;

    static class PingPongServer implements Runnable {
        private SSLServerSocketFactory sslServerSocketFactory;

        public PingPongServer() throws IOException, SSLContextException {
            Properties serverProps = new Properties();
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, "src/test/resources");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "appId");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_FILE, "keystore.jks");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_TYPE, "JKS");
            serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_PASS, "password");

            sslServerSocketFactory = EzSSL.getSSLContext(serverProps).getServerSocketFactory();
        }

        public void run() {
            ServerSocket socket = null;
            try {
                socket = sslServerSocketFactory.createServerSocket(port);

                String client_data;

                Socket connection = socket.accept();

                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connection.getOutputStream());

                client_data = inFromClient.readLine();
                System.out.println("Received from client: " + client_data);

                outToClient.writeBytes("pong\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class PingPongClient implements Runnable {
        private SSLSocketFactory sslSocketFactory;

        public PingPongClient() throws IOException, SSLContextException {
            Properties props = new Properties();
            props.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, "src/test/resources");
            props.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "appId");
            props.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_FILE, "keystore.jks");
            props.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_TYPE, "JKS");
            props.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_TRUSTSTORE_PASS, "password");
            sslSocketFactory = EzSSL.getSSLContext(props).getSocketFactory();
        }

 
        public void run() {
            Socket socket = null;
            try {
                socket = sslSocketFactory.createSocket("localhost", port);
                DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                outToServer.writeBytes("ping\n");
                System.out.println("Received from server: " + inFromServer.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, SSLContextException, InterruptedException {
        Thread server = new Thread(new PingPongServer());
        Thread client = new Thread(new PingPongClient());

        server.start();
        client.start();

        client.join();
        server.join();
    }

}
