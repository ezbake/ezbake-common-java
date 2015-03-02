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

package ezbake.common.openshift;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;

import java.io.File;

import org.apache.commons.lang3.math.NumberUtils;

public class OpenShiftUtil {
    public static final String OPENSHIFT_REPO_DIR = "OPENSHIFT_REPO_DIR";

    public static boolean inOpenShiftContainer() {
        return System.getenv(OPENSHIFT_REPO_DIR) != null;
    }

    public static String getConfigurationDir() {
        String repoDir = getRepoDir();
        final String ezbakeConfigDir = "config";
        String retVal;
        if(repoDir.endsWith(File.separator)) {
            retVal = repoDir.concat(ezbakeConfigDir);
        } else {
            retVal = Joiner.on(File.separator).join(repoDir, ezbakeConfigDir);
        }

        return retVal;
    }

    public static String getRepoDir() {
        return Preconditions.checkNotNull(System.getenv(OPENSHIFT_REPO_DIR));
    }

    public static HostAndPort getThriftPrivateInfo() {
        String ip = Preconditions.checkNotNull(System.getenv("OPENSHIFT_JAVA_THRIFTRUNNER_IP"));
        String port = Preconditions.checkNotNull(System.getenv("OPENSHIFT_JAVA_THRIFTRUNNER_TCP_PORT"));
        return HostAndPort.fromParts(ip, NumberUtils.toInt(port, -1));
    }

}
