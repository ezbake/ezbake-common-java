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

package ezbake.security.client.provider;

import java.util.Properties;

import com.google.inject.Inject;
import ezbake.base.thrift.EzSecurityTokenException;
import ezbake.security.thrift.AppNotRegisteredException;
import ezbake.security.thrift.ezsecurityConstants;
import ezbakehelpers.ezconfigurationhelpers.application.EzBakeApplicationConfigurationHelper;
import org.apache.thrift.TException;
import ezbake.thrift.ThriftClientPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

import ezbake.base.thrift.EzSecurityToken;
import ezbake.base.thrift.TokenRequest;
import ezbake.security.thrift.EzSecurity;

public class NonPkiTokenProvider implements TokenProvider {
    private Logger log = LoggerFactory.getLogger(NonPkiTokenProvider.class);

    protected Supplier<ThriftClientPool> pool;
    protected String securityId;

    @Inject
    public NonPkiTokenProvider(Properties properties, Supplier<ThriftClientPool> pool) {
        securityId = new EzBakeApplicationConfigurationHelper(properties).getSecurityID();
        this.pool = pool;
    }

    @Override
    public EzSecurityToken getSecurityToken(TokenRequest tokenRequest) throws EzSecurityTokenException {
        EzSecurityToken token;
        EzSecurity.Client client = null;
        try {
            client = pool.get().getClient(ezsecurityConstants.SERVICE_NAME, EzSecurity.Client.class);
            token = client.requestToken(tokenRequest, "");
        } catch (AppNotRegisteredException e) {
            log.error("Application {} is not registered with EzSecurity", securityId, e);
            throw new EzSecurityTokenException("Application not registered " + e.getMessage());
        } catch (TException e) {
            log.error("Unexpected thrift exception getting security token: {}", e.getMessage());
            throw new EzSecurityTokenException("TException getting security token: "+e.getMessage());
        } finally {
            pool.get().returnToPool(client);
        }

        return token;
    }

    @Override
    public EzSecurityToken refreshSecurityToken(EzSecurityToken token) throws EzSecurityTokenException {
        EzSecurityToken refreshedToken;
        EzSecurity.Client client = null;
        try {
            TokenRequest request = new TokenRequest(securityId,
                    System.currentTimeMillis(), token.getType());
            request.setTokenPrincipal(token);

            client = pool.get().getClient(ezsecurityConstants.SERVICE_NAME, EzSecurity.Client.class);
            refreshedToken = client.refreshToken(request, "");
        } catch (AppNotRegisteredException e) {
            log.error("Application {} is not registered with EzSecurity", securityId, e);
            throw new EzSecurityTokenException("Application not registered " + e.getMessage());
        } catch (TException e) {
            log.error("Unexpected thrift exception getting security token: {}", e.getMessage());
            throw new EzSecurityTokenException("TException getting security token: "+e.getMessage());
        } finally {
            if (client != null) {
                pool.get().returnToPool(client);
            }
        }

        return refreshedToken;
    }

    @Override
    public boolean isValidToken(EzSecurityToken token) {
        return true;
    }

}
