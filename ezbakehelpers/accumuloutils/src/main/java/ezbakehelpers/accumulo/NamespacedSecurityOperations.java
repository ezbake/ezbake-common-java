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

package ezbakehelpers.accumulo;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.admin.SecurityOperations;
import org.apache.accumulo.core.client.security.tokens.AuthenticationToken;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.NamespacePermission;
import org.apache.accumulo.core.security.SystemPermission;
import org.apache.accumulo.core.security.TablePermission;

import java.util.Set;

public class NamespacedSecurityOperations implements SecurityOperations {
    private SecurityOperations operations;
    private String namespace;

    public NamespacedSecurityOperations(SecurityOperations operations, String namespace) {
        this.operations = operations;
        this.namespace = namespace;
    }

    @Override
    public void createUser(String user, byte[] password, Authorizations authorizations) throws AccumuloException, AccumuloSecurityException {
        operations.createUser(user, password, authorizations);
    }

    @Override
    public void createLocalUser(String principal, PasswordToken password) throws AccumuloException, AccumuloSecurityException {
        operations.createLocalUser(principal, password);
    }

    @Override
    public void dropUser(String user) throws AccumuloException, AccumuloSecurityException {
        operations.dropUser(user);
    }

    @Override
    public void dropLocalUser(String principal) throws AccumuloException, AccumuloSecurityException {
        operations.dropLocalUser(principal);
    }

    @Override
    public boolean authenticateUser(String user, byte[] password) throws AccumuloException, AccumuloSecurityException {
        return operations.authenticateUser(user, password);
    }

    @Override
    public boolean authenticateUser(String principal, AuthenticationToken token) throws AccumuloException, AccumuloSecurityException {
        return operations.authenticateUser(principal, token);
    }

    @Override
    public void changeUserPassword(String user, byte[] password) throws AccumuloException, AccumuloSecurityException {
        operations.changeUserPassword(user, password);
    }

    @Override
    public void changeLocalUserPassword(String principal, PasswordToken token) throws AccumuloException, AccumuloSecurityException {
        operations.changeLocalUserPassword(principal, token);
    }

    @Override
    public void changeUserAuthorizations(String principal, Authorizations authorizations) throws AccumuloException, AccumuloSecurityException {
        operations.changeUserAuthorizations(principal, authorizations);
    }

    @Override
    public Authorizations getUserAuthorizations(String principal) throws AccumuloException, AccumuloSecurityException {
        return operations.getUserAuthorizations(principal);
    }

    @Override
    public boolean hasSystemPermission(String principal, SystemPermission perm) throws AccumuloException, AccumuloSecurityException {
        return operations.hasSystemPermission(principal, perm);
    }

    @Override
    public boolean hasTablePermission(String principal, String table, TablePermission perm) throws AccumuloException, AccumuloSecurityException {
        return operations.hasTablePermission(principal, NamespaceUtil.getFullTableName(namespace, table), perm);
    }

    @Override
    public boolean hasNamespacePermission(String principal, String namespace, NamespacePermission perm) throws AccumuloException, AccumuloSecurityException {
        return operations.hasNamespacePermission(principal, namespace, perm);
    }

    @Override
    public void grantSystemPermission(String principal, SystemPermission permission) throws AccumuloException, AccumuloSecurityException {
        operations.grantSystemPermission(principal, permission);
    }

    @Override
    public void grantTablePermission(String principal, String table, TablePermission permission) throws AccumuloException, AccumuloSecurityException {
        operations.grantTablePermission(principal, NamespaceUtil.getFullTableName(namespace, table), permission);
    }

    @Override
    public void grantNamespacePermission(String principal, String namespace, NamespacePermission permission) throws AccumuloException, AccumuloSecurityException {
        operations.grantNamespacePermission(principal, namespace, permission);
    }

    @Override
    public void revokeSystemPermission(String principal, SystemPermission permission) throws AccumuloException, AccumuloSecurityException {
        operations.revokeSystemPermission(principal, permission);
    }

    @Override
    public void revokeTablePermission(String principal, String table, TablePermission permission) throws AccumuloException, AccumuloSecurityException {
        operations.revokeTablePermission(principal, NamespaceUtil.getFullTableName(namespace, table), permission);
    }

    @Override
    public void revokeNamespacePermission(String principal, String namespace, NamespacePermission permission) throws AccumuloException, AccumuloSecurityException {
        operations.revokeNamespacePermission(principal, namespace, permission);
    }

    @Override
    public Set<String> listUsers() throws AccumuloException, AccumuloSecurityException {
        return operations.listUsers();
    }

    @Override
    public Set<String> listLocalUsers() throws AccumuloException, AccumuloSecurityException {
        return operations.listLocalUsers();
    }
}
