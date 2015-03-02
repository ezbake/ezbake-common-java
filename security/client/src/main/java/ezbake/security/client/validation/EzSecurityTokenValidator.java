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

import ezbake.base.thrift.EzSecurityToken;
import ezbake.base.thrift.EzSecurityTokenException;
import ezbake.security.common.core.EzSecurityTokenUtils;
import ezbake.security.common.core.TokenExpiredException;

import javax.inject.Inject;
import java.util.Properties;

/**
 * Class that provides validation for EzSecurityTokens
 */
public class EzSecurityTokenValidator extends SignatureValidator implements TokenValidator<EzSecurityToken> {

    @Inject
    public EzSecurityTokenValidator(final Properties configuration) {
        super(configuration);
    }

    @Override
    public void validateToken(EzSecurityToken token) throws EzSecurityTokenException, TokenExpiredException {
        if(securityConfigurationHelper.useMock()) {
            return;
        }
        EzSecurityTokenUtils.verifyReceivedToken(crypto.get(), token, applicationConfigurationHelper.getSecurityID());
    }

    /**
     * Validates a security token using a new token validator. This differs from the instance validateToken in that it
     * only throws an EzSecurityTokenException, catching the expired exception and rethrowing
     *
     * @param token a token to verify
     * @param configuration the configuration properties for the app
     * @throws EzSecurityTokenException
     */
    public static void validateToken(EzSecurityToken token, Properties configuration)
            throws EzSecurityTokenException {
        try {
            new EzSecurityTokenValidator(configuration).validateToken(token);
        } catch (TokenExpiredException e) {
            throw new EzSecurityTokenException(e.getMessage());
        }
    }
}
