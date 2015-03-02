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

import org.junit.Test;

import java.io.IOException;

/**
 * User: jhastings
 * Date: 12/19/14
 * Time: 4:44 PM
 */
public class CryptoUtilTest {
    String ca = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDFjCCAf6gAwIBAgIBATANBgkqhkiG9w0BAQUFADAqMRUwEwYDVQQLEwxUaGVT\n" +
                "b3VyY2VEZXYxETAPBgNVBAMTCEV6QmFrZUNBMCAXDTE0MTEyNTE4MTk0OVoYDzIw\n" +
                "NDQxMTI1MTgxOTQ5WjAqMRUwEwYDVQQLEwxUaGVTb3VyY2VEZXYxETAPBgNVBAMT\n" +
                "CEV6QmFrZUNBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA19H/DmXI\n" +
                "aYMAXcU2v5Wyj9nlX7U4z8U5NUHNAEdJSez6//rTvck/bopwCvvVlpLTE4QImPHf\n" +
                "Rccye/zuCUEA7ZC7hOyvQAcjLdYkdeo40vkhHJa74DH463+Iz/uw6TzPWdK8okRJ\n" +
                "MW1jc8QlqXnSjFCTuTz+7h7o+QNErGHSyWWqx8nYyStCPhPgPEfFZlN8GJ5t4Z3V\n" +
                "q4jUki6XGMPjDvL6wF60MbGL7d9Q1+65rvBO7JD6NarIrATAtMCuMIqeH38OwqVj\n" +
                "ss1FBzAI1QK6iaFicO4g6kb9sfrImJcpNvLzR2PlVe67x3/gsedi099sSQfv3Tk6\n" +
                "LGvCwENj0LWYgwIDAQABo0UwQzASBgNVHRMBAf8ECDAGAQH/AgEAMA4GA1UdDwEB\n" +
                "/wQEAwIBBjAdBgNVHQ4EFgQUaEb00k4OIFiPw679dCo0AqTWTAIwDQYJKoZIhvcN\n" +
                "AQEFBQADggEBAIwj69Z6LYLdqHOBqK5rxZW0oOzgv05i5WFbN31ArNO/7fVY3Hc9\n" +
                "tqAO2CupJjF5drJSiinLy+6/dWpcfaZ7phlYdtOG7+OlL8uFAnz8wSciQwC0093A\n" +
                "LqPW3cvmwo9ECT+kGf1GOigBjETuloikOrAlZRqEC6c9bpyfuLML56cWbDkRyqvC\n" +
                "TYLb3DBEZVb2mvlGxgBpcSgViDZGqvK3TRyaVSHiGcE1coB76gXse7uR2ibbNSIS\n" +
                "1FIpSi6tqeQqdogiNywkjjf5AxHgGwGcqhNFTRSURoQI+P8HASBFSzfu+hx4Od83\n" +
                "PVuworkkw7nZT9pDUc6S7tjSVLHgxJc3aKk=\n" +
                "-----END CERTIFICATE-----";

    String caNoHeaders =
            "MIIDFjCCAf6gAwIBAgIBATANBgkqhkiG9w0BAQUFADAqMRUwEwYDVQQLEwxUaGVT\n" +
            "b3VyY2VEZXYxETAPBgNVBAMTCEV6QmFrZUNBMCAXDTE0MTEyNTE4MTk0OVoYDzIw\n" +
            "NDQxMTI1MTgxOTQ5WjAqMRUwEwYDVQQLEwxUaGVTb3VyY2VEZXYxETAPBgNVBAMT\n" +
            "CEV6QmFrZUNBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA19H/DmXI\n" +
            "aYMAXcU2v5Wyj9nlX7U4z8U5NUHNAEdJSez6//rTvck/bopwCvvVlpLTE4QImPHf\n" +
            "Rccye/zuCUEA7ZC7hOyvQAcjLdYkdeo40vkhHJa74DH463+Iz/uw6TzPWdK8okRJ\n" +
            "MW1jc8QlqXnSjFCTuTz+7h7o+QNErGHSyWWqx8nYyStCPhPgPEfFZlN8GJ5t4Z3V\n" +
            "q4jUki6XGMPjDvL6wF60MbGL7d9Q1+65rvBO7JD6NarIrATAtMCuMIqeH38OwqVj\n" +
            "ss1FBzAI1QK6iaFicO4g6kb9sfrImJcpNvLzR2PlVe67x3/gsedi099sSQfv3Tk6\n" +
            "LGvCwENj0LWYgwIDAQABo0UwQzASBgNVHRMBAf8ECDAGAQH/AgEAMA4GA1UdDwEB\n" +
            "/wQEAwIBBjAdBgNVHQ4EFgQUaEb00k4OIFiPw679dCo0AqTWTAIwDQYJKoZIhvcN\n" +
            "AQEFBQADggEBAIwj69Z6LYLdqHOBqK5rxZW0oOzgv05i5WFbN31ArNO/7fVY3Hc9\n" +
            "tqAO2CupJjF5drJSiinLy+6/dWpcfaZ7phlYdtOG7+OlL8uFAnz8wSciQwC0093A\n" +
            "LqPW3cvmwo9ECT+kGf1GOigBjETuloikOrAlZRqEC6c9bpyfuLML56cWbDkRyqvC\n" +
            "TYLb3DBEZVb2mvlGxgBpcSgViDZGqvK3TRyaVSHiGcE1coB76gXse7uR2ibbNSIS\n" +
            "1FIpSi6tqeQqdogiNywkjjf5AxHgGwGcqhNFTRSURoQI+P8HASBFSzfu+hx4Od83\n" +
            "PVuworkkw7nZT9pDUc6S7tjSVLHgxJc3aKk=\n";

    String caJustBegin = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDFjCCAf6gAwIBAgIBATANBgkqhkiG9w0BAQUFADAqMRUwEwYDVQQLEwxUaGVT\n" +
            "b3VyY2VEZXYxETAPBgNVBAMTCEV6QmFrZUNBMCAXDTE0MTEyNTE4MTk0OVoYDzIw\n" +
            "NDQxMTI1MTgxOTQ5WjAqMRUwEwYDVQQLEwxUaGVTb3VyY2VEZXYxETAPBgNVBAMT\n" +
            "CEV6QmFrZUNBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA19H/DmXI\n" +
            "aYMAXcU2v5Wyj9nlX7U4z8U5NUHNAEdJSez6//rTvck/bopwCvvVlpLTE4QImPHf\n" +
            "Rccye/zuCUEA7ZC7hOyvQAcjLdYkdeo40vkhHJa74DH463+Iz/uw6TzPWdK8okRJ\n" +
            "MW1jc8QlqXnSjFCTuTz+7h7o+QNErGHSyWWqx8nYyStCPhPgPEfFZlN8GJ5t4Z3V\n" +
            "q4jUki6XGMPjDvL6wF60MbGL7d9Q1+65rvBO7JD6NarIrATAtMCuMIqeH38OwqVj\n" +
            "ss1FBzAI1QK6iaFicO4g6kb9sfrImJcpNvLzR2PlVe67x3/gsedi099sSQfv3Tk6\n" +
            "LGvCwENj0LWYgwIDAQABo0UwQzASBgNVHRMBAf8ECDAGAQH/AgEAMA4GA1UdDwEB\n" +
            "/wQEAwIBBjAdBgNVHQ4EFgQUaEb00k4OIFiPw679dCo0AqTWTAIwDQYJKoZIhvcN\n" +
            "AQEFBQADggEBAIwj69Z6LYLdqHOBqK5rxZW0oOzgv05i5WFbN31ArNO/7fVY3Hc9\n" +
            "tqAO2CupJjF5drJSiinLy+6/dWpcfaZ7phlYdtOG7+OlL8uFAnz8wSciQwC0093A\n" +
            "LqPW3cvmwo9ECT+kGf1GOigBjETuloikOrAlZRqEC6c9bpyfuLML56cWbDkRyqvC\n" +
            "TYLb3DBEZVb2mvlGxgBpcSgViDZGqvK3TRyaVSHiGcE1coB76gXse7uR2ibbNSIS\n" +
            "1FIpSi6tqeQqdogiNywkjjf5AxHgGwGcqhNFTRSURoQI+P8HASBFSzfu+hx4Od83\n" +
            "PVuworkkw7nZT9pDUc6S7tjSVLHgxJc3aKk=\n";

    String caJustEnd =
            "MIIDFjCCAf6gAwIBAgIBATANBgkqhkiG9w0BAQUFADAqMRUwEwYDVQQLEwxUaGVT\n" +
            "b3VyY2VEZXYxETAPBgNVBAMTCEV6QmFrZUNBMCAXDTE0MTEyNTE4MTk0OVoYDzIw\n" +
            "NDQxMTI1MTgxOTQ5WjAqMRUwEwYDVQQLEwxUaGVTb3VyY2VEZXYxETAPBgNVBAMT\n" +
            "CEV6QmFrZUNBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA19H/DmXI\n" +
            "aYMAXcU2v5Wyj9nlX7U4z8U5NUHNAEdJSez6//rTvck/bopwCvvVlpLTE4QImPHf\n" +
            "Rccye/zuCUEA7ZC7hOyvQAcjLdYkdeo40vkhHJa74DH463+Iz/uw6TzPWdK8okRJ\n" +
            "MW1jc8QlqXnSjFCTuTz+7h7o+QNErGHSyWWqx8nYyStCPhPgPEfFZlN8GJ5t4Z3V\n" +
            "q4jUki6XGMPjDvL6wF60MbGL7d9Q1+65rvBO7JD6NarIrATAtMCuMIqeH38OwqVj\n" +
            "ss1FBzAI1QK6iaFicO4g6kb9sfrImJcpNvLzR2PlVe67x3/gsedi099sSQfv3Tk6\n" +
            "LGvCwENj0LWYgwIDAQABo0UwQzASBgNVHRMBAf8ECDAGAQH/AgEAMA4GA1UdDwEB\n" +
            "/wQEAwIBBjAdBgNVHQ4EFgQUaEb00k4OIFiPw679dCo0AqTWTAIwDQYJKoZIhvcN\n" +
            "AQEFBQADggEBAIwj69Z6LYLdqHOBqK5rxZW0oOzgv05i5WFbN31ArNO/7fVY3Hc9\n" +
            "tqAO2CupJjF5drJSiinLy+6/dWpcfaZ7phlYdtOG7+OlL8uFAnz8wSciQwC0093A\n" +
            "LqPW3cvmwo9ECT+kGf1GOigBjETuloikOrAlZRqEC6c9bpyfuLML56cWbDkRyqvC\n" +
            "TYLb3DBEZVb2mvlGxgBpcSgViDZGqvK3TRyaVSHiGcE1coB76gXse7uR2ibbNSIS\n" +
            "1FIpSi6tqeQqdogiNywkjjf5AxHgGwGcqhNFTRSURoQI+P8HASBFSzfu+hx4Od83\n" +
            "PVuworkkw7nZT9pDUc6S7tjSVLHgxJc3aKk=\n" +
            "-----END CERTIFICATE-----";

    @Test
    public void testIt() throws IOException {
        CryptoUtil.load_jks(ca);
        CryptoUtil.load_jks(caNoHeaders);
        CryptoUtil.load_jks(caJustBegin);
        CryptoUtil.load_jks(caJustEnd);
        CryptoUtil.load_jks(ca.replace("\n", ""));
        CryptoUtil.load_jks(caNoHeaders.replace("\n", ""));
        CryptoUtil.load_jks(caJustBegin.replace("\n", ""));
        CryptoUtil.load_jks(caJustEnd.replace("\n", ""));
    }

}
