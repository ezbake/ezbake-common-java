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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

import ezbake.common.properties.DuplicatePropertyException;
import ezbake.common.properties.EzProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will attempt to load properties from a directory.  It looks for this directory either by the user passing
 * it into the consturctor to set the directory programatically.  If not the loader will first attempt to load from a
 * java system property (ezconfiguration.dir), if that fails then it will attempt to read from an environment variable 
 * EZCONFIGURATION_DIR, and if that fails then it will use the default directory /etc/sysconfig/ezbake.
 */
public class DirectoryConfigurationLoader implements EzConfigurationLoader {
    
    public final static String EZCONFIGURATION_PROPERTY = "ezconfiguration.dir";
    public final static String EZCONFIGURATION_ENV_VAR = "EZCONFIGURATION_DIR";
    public final static String EZCONFIGURATION_DEFAULT_DIR = "/etc/sysconfig/ezbake";
    public final static String EZCONFIGURATION_PROPERTIES_FILE_EXTENSION = ".properties";

    private final static Logger logger = LoggerFactory.getLogger(DirectoryConfigurationLoader.class.getName());
    private final static String EZCONFIGURATION_PROPERTIES_GLOB = "*" + EZCONFIGURATION_PROPERTIES_FILE_EXTENSION;

    @VisibleForTesting
    protected final Path dir;
    
    public DirectoryConfigurationLoader() {
        this.dir = getDirectory();
    }
    
    public DirectoryConfigurationLoader(Path dir) {
        this.dir = dir;
    }

    /**
     * This will load the properties from a directory.  It does this by using the
     * {@link ezbake.common.properties.EzProperties#loadFromDirectory(String)} method which will glob for all the files
     * and will override properties based on the order the glob picks up the configuration file.
     *
     * @return a Properties object with the properties loaded
     * @throws EzConfigurationLoaderException if the method can't load from the files on disk
     * @throws IllegalStateException if we somehow have collisions in our propreties even though we passed true to
     * override
     */
    @Override
    public Properties loadConfiguration() throws EzConfigurationLoaderException {
        logger.info("Attempting to load properties from " + dir.toString());
        try {
            EzProperties ezProps = new EzProperties();
            ezProps.loadFromDirectory(dir, EZCONFIGURATION_PROPERTIES_GLOB, true);
            logger.info("Successfully loaded properties from " + dir);
            return ezProps;
        } catch(IOException e) {
            throw new EzConfigurationLoaderException("Could not load from " + dir, e);
        } catch(DuplicatePropertyException e) {
            throw new IllegalStateException("We have collisions between properties even though we should override", e);
        }
    }

    /**
     * This method checks to see if there is anything to load.  It does this by checking to see if the dir variables is
     * non null.  It then checks to see if the directory exists and is indeed a directory.
     *
     * @return true or false if the directory string is non null and is actually a directory
     */
    @Override
    public boolean isLoadable() {
        if(dir == null) {
            return false;
        }
        
       return Files.isDirectory(dir); 
    }

    protected Path getDirectory() {
        String tmp; 
        try {
            tmp = Objects.firstNonNull(System.getProperty(EZCONFIGURATION_PROPERTY), 
                System.getenv(EZCONFIGURATION_ENV_VAR));
        } catch(NullPointerException e) {
            tmp = EZCONFIGURATION_DEFAULT_DIR;
        }
        return Paths.get(tmp);
    }
}
