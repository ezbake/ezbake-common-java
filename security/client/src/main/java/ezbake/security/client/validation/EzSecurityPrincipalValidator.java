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

import ezbake.base.thrift.EzSecurityPrincipal;
import ezbake.base.thrift.EzSecurityTokenException;
import ezbake.security.common.core.EzSecurityTokenUtils;
import ezbake.security.common.core.TokenExpiredException;

import javax.inject.Inject;
import java.util.Properties;

/**
 * User: jhastings
 * Date: 10/13/14
 * Time: 3:34 PM
 */
public class EzSecurityPrincipalValidator extends SignatureValidator implements TokenValidator<EzSecurityPrincipal> {

    @Inject
    public EzSecurityPrincipalValidator(Properties configuration) {
        super(configuration);
    }

    @Override
    public void validateToken(EzSecurityPrincipal token) throws EzSecurityTokenException, TokenExpiredException {
        if (System.currentTimeMillis() >= token.getValidity().getNotAfter()) {
            throw new TokenExpiredException("Principal token was expired. " + System.currentTimeMillis() +
                    " >= " + token.getValidity().getNotAfter());
        }
        EzSecurityTokenUtils.verifyPrincipalSignature(token, crypto.get());
    }
}
