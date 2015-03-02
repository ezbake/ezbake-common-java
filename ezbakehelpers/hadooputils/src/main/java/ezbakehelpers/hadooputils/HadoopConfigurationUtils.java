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

package ezbakehelpers.hadooputils;

import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;

/**
 * This is a utility for common configuration operations
 */
public final class HadoopConfigurationUtils {

    /**
     * Creates a {@link org.apache.hadoop.conf.Configuration} from the properties
     *
     * @param props properties object to add to the new created configuration, may be null
     *
     * @return newly created configuration based on the parameters empty configuration if props is null or emtpy
     */
    public static Configuration configurationFromProperties(Properties props) {
        Configuration configuration = new Configuration();
        if(props != null && !props.isEmpty()) {
            for(String key : props.stringPropertyNames()) {
                configuration.set(key, props.getProperty(key));
            }
        }

        return configuration;
    }

    /**
     * Returns a static Properties copy of the given configuration.
     *
     * @param configuration hadoop configuration to convert
     *
     * @return a properties object which has the keys and values from the configuration or an empty properties if
     * configuration is null
     */
    public static Properties propertiesFromConfiguration(Configuration configuration) {
        Properties props = new Properties();
        if(configuration != null) {
            for (Map.Entry<String, String> entry : configuration) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
        }
        return props;
    }

}
