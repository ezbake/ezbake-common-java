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

package ezbake.data.iterator;

import static ezbake.security.serialize.VisibilitySerialization.deserializeVisibilityWrappedValue;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.Filter;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.thrift.TException;

import com.google.common.base.Joiner;

import ezbake.base.thrift.Authorizations;
import ezbake.base.thrift.Permission;
import ezbake.base.thrift.Visibility;
import ezbake.security.permissions.PermissionUtils;
import ezbake.thrift.ThriftUtils;

public class EzBakeVisibilityFilter extends Filter {
    private static final String USER_AUTHORIZATION_BASE64 = "userAuthorizationBase64";
    private static final String REQUIRED_PERMISSIONS = "requiredPermissions";

    private Authorizations userAuthorization = null;
    private final EnumSet<Permission> requiredPermissions = EnumSet.noneOf(Permission.class);

    @Override
    public boolean accept(Key k, Value v) {
        Visibility visibility = null;
        try {
            visibility = deserializeVisibilityWrappedValue(v).getVisibilityMarkings();
        } catch (final IOException | TException e) {
            throw new RuntimeException(e.getMessage());
        }

        return PermissionUtils.getPermissions(userAuthorization, visibility, false, requiredPermissions).containsAll(
                requiredPermissions);
    }

    @Override
    public void init(SortedKeyValueIterator<Key, Value> source, Map<String, String> options, IteratorEnvironment env)
            throws IOException {
        super.init(source, options, env);

        if (options.containsKey(USER_AUTHORIZATION_BASE64)) {
            try {
                userAuthorization =
                        ThriftUtils.deserializeFromBase64(Authorizations.class,
                                options.get(USER_AUTHORIZATION_BASE64));
            } catch (final TException e) {
                throw new IOException(e);
            }
        } else {
            throw new IllegalArgumentException("Missing required key: " + USER_AUTHORIZATION_BASE64);
        }

        if (options.containsKey(REQUIRED_PERMISSIONS)) {
            for (final String perm : options.get(REQUIRED_PERMISSIONS).split(",")) {
                requiredPermissions.add(Permission.valueOf(perm));
            }
        } else {
            throw new IllegalArgumentException("Missing required key: " + REQUIRED_PERMISSIONS);
        }
    }

    @Override
    public IteratorOptions describeOptions() {
        final IteratorOptions io = super.describeOptions();
        io.setName("EzBake Visibility Iterator");

        io.setDescription("Extracts visibility from value field using BVSerializer, filters entries that don't "
                + "have proper permissions set");

        io.addNamedOption(USER_AUTHORIZATION_BASE64, "Base64 Encoded User Authorization");
        io.addNamedOption(REQUIRED_PERMISSIONS, "Comma delimited string of required permissions");
        return io;
    }

    public static void setOptions(IteratorSetting iteratorSetting, Authorizations userAuths,
            Set<Permission> requiredPermissions) throws TException {
        iteratorSetting.addOption(USER_AUTHORIZATION_BASE64, ThriftUtils.serializeToBase64(userAuths));
        iteratorSetting.addOption(REQUIRED_PERMISSIONS, Joiner.on(',').join(requiredPermissions));
    }
}
