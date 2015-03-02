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

package ezbake.security.client.validation;

import ezbake.base.thrift.EzSecurityTokenException;
import ezbake.base.thrift.ProxyPrincipal;
import ezbake.base.thrift.ProxyUserToken;
import ezbake.security.common.core.EzSecurityTokenUtils;
import ezbake.security.common.core.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/13/14
 * Time: 3:28 PM
 */
public class ProxyTokenValidator extends SignatureValidator implements TokenValidator<ProxyPrincipal> {
    private static final Logger logger = LoggerFactory.getLogger(ProxyTokenValidator.class);

    @Inject
    public ProxyTokenValidator(final Properties configuration) {
        super(configuration);
    }

    @Override
    public void validateToken(ProxyPrincipal token) throws EzSecurityTokenException, TokenExpiredException {
        if (securityConfigurationHelper.useMock()) {
            return;
        }

        if (EzSecurityTokenUtils.verifyProxyUserToken(token.getProxyToken(), token.getSignature(), crypto.get())) {
            ProxyUserToken put = EzSecurityTokenUtils.deserializeProxyUserToken(token.getProxyToken());
            long currentTime = System.currentTimeMillis();
            if (put.getNotAfter() <= currentTime) {
                logger.warn("Verification of User Principal expiration timestamp from headers failed {} < {}",
                        put.getNotAfter(), currentTime);
                throw new TokenExpiredException("Token from HTTP headers was expired");
            }
        } else {
            logger.warn("Proxy header verification failed due to invalid signature");
            throw new EzSecurityTokenException("Unable to verify signature of user info from HTTP headers");
        }


    }
}
