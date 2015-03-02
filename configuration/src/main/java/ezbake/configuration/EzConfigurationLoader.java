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

import java.util.Properties;

/**
 * This interface describes something which will load a properties object. Currently it is on the caller to check to
 * see if this loader should attempt to load the configuration.  The caller can check this using the isLoadable method.
 */
public interface EzConfigurationLoader {
    /**
     * This method actually loads the properties object.
     *
     * @throws EzConfigurationLoaderException if there is a problem trying to load properties 
     * @return a properties object (Implementors should return a blank properties object if no properties are loaded)
     */
    public Properties loadConfiguration() throws EzConfigurationLoaderException;

    /**
     * Checks to see if we should attempt to load the configuration.  <b>It is on the caller of this class to be sure to
     * call this method to avoid attempting to load something that might not exist.</b>
     */
    public boolean isLoadable();
}
