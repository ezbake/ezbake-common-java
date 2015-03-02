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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import ezbake.common.io.ClasspathResources;
import ezbake.common.properties.DuplicatePropertyException;
import ezbake.common.properties.EzProperties;


import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A configuration loader which will attempt to load properties from the class path.
 *
 * This configuration loader is different than other configuration loaders.  In that we are making the assumption that
 * the user wants to load ALL of the classpath resources.  Unlike other configuration loaders, this does not fail
 * silently while its loading.  It will instead throw an IllegalArguemtnException when it tries to see if these objects
 * exist on the classpath.
 *
 * NOTE: This should primarily be used for testing
 */
public class ClasspathConfigurationLoader implements EzConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ClasspathConfigurationLoader.class);

    public static final String CLASSPATH_DEFAULT_RESOURCE = "/ezbake-config.properties";

    protected final List<URL> classpathResourceUrls;

    /**
     * Constructor which loads /ezbake-config.properrties from the classpath.
     *
     * @throws IllegalArgumentException if it can not find /ezbake-config.properties on the classpath
     */
    public ClasspathConfigurationLoader() {
        this(CLASSPATH_DEFAULT_RESOURCE);
    }

    /**
     * Constructor which loads a series of classpathResources from the classpath.
     *
     * @param a variable list of strings indicating classpath resources.
     * @throws IllegalArgumentException if ANY in the series of resources failes to load
     */
    public ClasspathConfigurationLoader(String ... classpathResources) {
        this.classpathResourceUrls = getClasspathResourceUrls(classpathResources);
    }

    /**
     * This method actually loads the properties object.
     *
     * @return a properties object (Implementors should return a blank properties object if no properties are loaded)
     * @throws ezbake.configuration.EzConfigurationLoaderException if there is a problem trying to load properties
     */
    @Override
    public Properties loadConfiguration() throws EzConfigurationLoaderException {
        EzProperties ezProperties = new EzProperties();
        for(URL resourceToLoad : classpathResourceUrls) {
            logger.info("Attempting to load properties from: {}", resourceToLoad);
            Properties loaded = new Properties();
            try {
                loaded.load(resourceToLoad.openStream());
                ezProperties.mergeProperties(loaded, true);
            } catch (IOException e) {
                throw new EzConfigurationLoaderException("We could not load properties from " + resourceToLoad, e);
            } catch (DuplicatePropertyException ignored) {
                // we are merging into an empty EzProperties, this should not happen
            }
        }
        return ezProperties;
    }

    /**
     * This configuration loader is a bit different if we get past the construction phase then we know that this loader
     * should be able to load.  So we are just making this a pass through.
     *
     * @return true since this is a pass through
     */
    @Override
    public boolean isLoadable() {
        return true;
    }

    private List<URL> getClasspathResourceUrls(String [] classpathResources) {
        List <URL> loadedUrls = Lists.newArrayList();
        /* A list to keep track of the resources which don't exist so that we can print them out
           in our log. */
        List <String> nullResources = Lists.newArrayList();
        for(String classpathResource : classpathResources) {
               URL classpathResourceUrl = ClasspathResources.getResource(classpathResource);
               if(classpathResourceUrl == null) {
                   nullResources.add(classpathResource);
                   continue;
               }
               loadedUrls.add(classpathResourceUrl);
        }

        if(!nullResources.isEmpty()) {
            String msg = String.format("Unable to load the following resources %s!",
                Joiner.on(",").skipNulls().join(nullResources));
            throw new IllegalArgumentException(msg);
        }

        return loadedUrls;
    }
}
