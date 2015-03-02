# Permission Utils
A library to handle generating user permissions given a visibility and user authorizations.

The below code shows the main use case of calculating permissions given user authorizations and
data visibility.

## Java API

```java
package example;

import java.util.EnumSet;
import java.util.Set;

import org.apache.thrift.TException;

import ezbake.base.thrift.Authorizations;
import ezbake.base.thrift.EzSecurityToken;
import ezbake.base.thrift.Permission;
import ezbake.base.thrift.Visibility;
import ezbake.security.permissions.PermissionUtils;
import ezbake.thrift.ThriftUtils;

public class Example {
    public static void main(String[] args) {
        // Get actual EzSecurityToken from Thrift call, etc.
        final EzSecurityToken securityToken = new EzSecurityToken();

        // Get actual serialized Visibility struct from backend (could be either Base64-encoded string or raw bytes)
        final String visibilityBase64 = "foobarbaz";

        Visibility visibility = null;
        try {
            // Convert serialized Visibility to actual struct
            visibility = ThriftUtils.deserializeFromBase64(Visibility.class, visibilityBase64);
            // NOTE: or use ThriftUtils.deserialize(Visibility.class, visibilityBytes) if using raw bytes
        } catch (final TException e) {
            // Handle error in some way
        }

        // Get Authorizations instance from EzSecurityToken
        final Authorizations userAuths = securityToken.getAuthorizations();

        // NOTE: There are overloads of gerPermissions to skip various checks like formal authorization.
        final Set<Permission> userPerms = PermissionUtils.getPermissions(userAuths, visibility);
        if (userPerms.contains(Permission.READ) && userPerms.contains(Permission.WRITE)) {
            // Allow data read with update
        } else if (userPerms.containsAll(EnumSet.of(Permission.READ, Permission.DISCOVER))) {
            // Another way to check multiple permissions
        }

        // You can also use the overload of getPermissions that takes a subset of perms you care about so that all do
        // not have to be calculated
        final Set<Permission> userPerms2 =
                PermissionUtils.getPermissions(userAuths, visibility, true, EnumSet.of(Permission.READ));
        if (!userPerms2.isEmpty()) {
            // Allow read operations
        }
    }
}
```
