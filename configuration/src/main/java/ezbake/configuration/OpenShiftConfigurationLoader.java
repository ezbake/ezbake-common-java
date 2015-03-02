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

package ezbake.configuration;

import ezbake.common.openshift.OpenShiftUtil;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will attempt to load properties from a OpenShift Gears configuration directory.  Which is a directory
 * inside of OpenShift's git repository.  That directory can be found on the OpenShift gear by look at
 * OPENSHIFT_REPO_DIR environement variable from inside of the gear.  The configuration directory is the "config"
 * directory under the OPENSHIFT_REPO_DIR.
 *
 * For more information on OpenShift Origin @see <a href="http://openshift.github.io/documentation">OpenShift Docs</a>
 */
public class OpenShiftConfigurationLoader extends DirectoryConfigurationLoader {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftConfigurationLoader.class.getName());

    /**
     * Lets figure out if we are in an openshift conainter or not.  If we are in an openshift container return the
     * OpenShift container configuration directory.
     *
     * @return a string with the OpenShift container directory or null if we are not in an openshift container
     */
    @Override
    protected Path getDirectory() {
        if(!OpenShiftUtil.inOpenShiftContainer()) {
            logger.info("We are not in an OpenShift container, so we will NOT attempt to load properties!");
            // ensure directory is null, since we are not in an openshift container
            return null;
        }
        return Paths.get(OpenShiftUtil.getConfigurationDir());
    }
}
