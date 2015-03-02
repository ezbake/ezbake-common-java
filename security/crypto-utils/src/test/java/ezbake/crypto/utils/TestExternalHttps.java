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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import ezbake.configuration.constants.EzBakePropertyConstants;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;


/**
 * User: jhastings
 * Date: 3/5/14
 * Time: 5:01 PM
 */
public class TestExternalHttps {

    private static HttpClient getSSLHttpClient() {
        HttpClient httpClient;

        Properties serverProps = new Properties();
        serverProps.setProperty(EzBakePropertyConstants.EZBAKE_CERTIFICATES_DIRECTORY, "src/test/resources");
        serverProps.setProperty(EzBakePropertyConstants.EZBAKE_SECURITY_ID, "appId");
        serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_FILE, "keystore.jks");
        serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_TYPE, "JKS");
        serverProps.setProperty(EzBakePropertyConstants.EZBAKE_APPLICATION_KEYSTORE_PASS, "password");

        try {
            httpClient = HttpClients.custom().setSslcontext(EzSSL.getSSLContext(serverProps)).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("IO Exception reading keystore/truststore file: " + e.getMessage());
        }
        return httpClient;
    }

    @Test @Ignore
    public void testWithCustomKeystore() throws UnirestException {
        Unirest.setHttpClient(getSSLHttpClient());
        HttpResponse<String> s = Unirest.get("https://www.twitter.com").asString();
        Assert.assertNotNull(s.getBody());
    }

    @Test @Ignore
    public void testTwitter() throws UnirestException {
        HttpResponse<String> s = Unirest.get("https://www.twitter.com").asString();
        Assert.assertNotNull(s.getBody());
    }

}
