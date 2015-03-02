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

package ezbake.common.properties;

import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

import ezbake.common.io.ClasspathResources;
import ezbake.common.security.TextCryptoProvider;
import ezbake.common.openshift.OpenShiftUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * This class extends {@link java.util.Properties} to add convenience methods to get primitives besides strings, deal with
 * encrypted values, and load from various resources including streams
 *
 * If you are going to be dealing with encrypted properties then you are going to have to make sure to the set the
 * TextCryptoProvider eg:
 *
 *      EzProperties ezProperties = new EzProperties();
 *      ezProperties.setTextEncryptor(new SharedSecretTextCryptoProvider(System.getenv("SHARED_SECRET")));
 *
 */
public class EzProperties extends Properties {

    private TextCryptoProvider cryptoProvider = null;

    public EzProperties() {
        this(null, false);
    }

    /**
     * Constructor which takes in properties and will either use the properties objects as "defaults" like the
     * java.util.Properties constructor or if copyProperties is true, then we do a putAll and have all the properties
     * internally.
     *
     * @param props properties which we want to either copy or use as defaults
     * @param copyProperties  is a flag for whether we want to copy the properties into this object or use the
     * properties as a "default" (keep a reference to the object and if we can't find it in our properties object then
     * we go look in the "default" properties object)
     */
    public EzProperties(Properties props, boolean copyProperties) {
        if(!copyProperties) {
            this.defaults = props;
        } else {
            putAll(props);
        }
    }

    /**
     * Set the crypto provider to use when encrypting and decrypting values
     *
     * @param provider the text crypto provider to use {@see ezbake.common.security.TextCryptoProvider}
     */
    public synchronized void setTextCryptoProvider(TextCryptoProvider provider) {
        this.cryptoProvider = provider;
    }

    /**
     * Get the crypt provider that this object is using to encrypt and decrypt values
     *
     * @return {@see ezbake.common.security.TextCryptoProvider} a text crypto provider
     */
    public TextCryptoProvider getTextCryptoProvider() {
        return cryptoProvider;
    }

    /**
     * Obtains the property value for the specified key {@see java.util.Properties.getProperty(java.lang.String)},
     * decrypting it if needed.
     *
     * @param key the property key
     *
     * @throws IllegalStateException if the TextCryptoProvider is null and has not been initialized if trying to read a
     * encrypted property
     * @throws SecurityExcepton if there is a problem decrypting the value
     *
     * @return the decrypted value
     */
    @Override
    public String getProperty(String key) throws SecurityException, IllegalArgumentException {
        return decrypt(super.getProperty(key));
    }

    /**
     * Obtains the property value for the specified key {@see java.util.Properties.getProperty(java.lang.String)},
     * decrypting it if needed.
     *
     * @param key the property key
     * @param defaultValue the default value to return
     *
     * @throws IllegalStateException if the TextCryptoProvider is null and has not been initialized if trying to read a
     * encrypted property
     * @throws SecurityExcepton if there is a problem decrypting the value
     * @return the decrypted value
     */
    @Override
    public String getProperty(String key, String defaultValue) throws SecurityException, IllegalStateException {
        return decrypt(super.getProperty(key, defaultValue));
    }

    /**
     * Set a property {@see java.util.Properties#setProperty(java.lang.String, java.lang.String)}
     *
      * @param key the key to be placed in this properties list
      * @param value the value corresponding to the key
      * @param isEncrypted whether or not we should encrypt the property
      *
      * @throws IllegalStateException if the TextCryptoProvider is null and has not been initialized if trying to read a
      * encrypted property
      * @throws SecurityExceptoin if there is a problem encrypting
      *
      * @return the previous value of the specified key in this property list, or null if it did not have one.
      */
    public void setProperty(String key, String value, boolean isEncrypted) throws SecurityException {
        if(isEncrypted) {
            if(cryptoProvider == null) {
                throw new IllegalStateException("Crypto provider has not been initialized!");
            }
            value = PropertiesEncryptionUtil.encryptPropertyValue(value, cryptoProvider);
        }

        super.setProperty(key, value);
    }

    /**
     * Get a property as a boolean, if the property doesn't exist or is not the string "true" this will return false
     *
     * @param propertyName is the name of the property we are looking for (the key)
     * @param defaultValue is the value to return if the key doesn't exist or the value doesn't equal "true"
     *
     * @return true if the key has a value of "true" it will return "true" if the key has a value of "false" it will
     * return false, else it will return the default value
     */
    public boolean getBoolean(String propertyName, boolean defaultValue) {
        String value = getProperty(propertyName);
        if(value == null) {
            return defaultValue;
        }

        value = value.trim().toLowerCase(); // normalize our string, get rid of whitespace and lower case
        boolean retVal = defaultValue;
        if(value.equals("true")) {
            retVal = true;
        } else if (value.equals("false")) {
            retVal = false;
        }

        return retVal;
    }

    /**
     * Get a property as a double, if the property doesn't exist or can't be converted then we return the default value
     *
     * @param propertyName is the name of the property we are looking for (the key)
     * @param defaultValue is the value to return if the key doesn't exist or the value can't be converted to a double
     *
     * @return either the properly parsed double or the default value if the key doesn't exist or can't be converted
     */
    public double getDouble(String propertyName, double defaultValue) {
        return NumberUtils.toDouble(getProperty(propertyName), defaultValue);
    }

    /**
     * Get a property as a float, if the property doesn't exist or can't be converted then we return the default value
     *
     * @param propertyName is the name of the property we are looking for (the key)
     * @param defaultValue is the value to return if the key doesn't exist or the value can't be converted to a float
     *
     * @return either the properly parsed float or the default value if the key doesn't exist or can't be converted
     */
    public float getFloat(String propertyName, float defaultValue) {
        return NumberUtils.toFloat(getProperty(propertyName), defaultValue);
    }

    /**
     * Get a property as a int, if the property doesn't exist or can't be converted then we return the default value
     *
     * @param propertyName is the name of the property we are looking for (the key)
     * @param defaultValue is the value to return if the key doesn't exist or the value can't be converted to a int
     *
     * @return either the properly parsed int or the default value if the key doesn't exist or can't be converted
     */
    public int getInteger(String propertyName, int defaultValue) {
        return NumberUtils.toInt(getProperty(propertyName), defaultValue);
    }
    /**
     * Get a property as a long, if the property doesn't exist or can't be converted then we return the default value
     *
     * @param propertyName is the name of the property we are looking for (the key)
     * @param defaultValue is the value to return if the key doesn't exist or the value can't be converted to a long
     *
     * @return either the properly parsed long or the default value if the key doesn't exist or can't be converted
     */
    public long getLong(String propertyName, long defaultValue) {
        return NumberUtils.toLong(getProperty(propertyName), defaultValue);
    }

    /**
     * Get a property as a path, if this doesn't exist throw an IOException
     *
     * @param propertyName is the name of the property we are looking for (the key)
     * @param defaultValue the value to return if the key doesn't exist
     *
     * @return the proper path or null if we can't find the directory
     */
    public String getPath(String propertyName, String defaultValue) {
        String path = getProperty(propertyName);
        if (path == null) {
            return defaultValue;
        }

        if (!OpenShiftUtil.inOpenShiftContainer()) {
            return path;
        }

        File file = new File(path);
        if (file.isAbsolute()) {
            return path;
        }

        /*
         * OpenShift cartridges like JBoss don't play nice with relative paths to match thrift runner we assume that we
         * are using relative paths.
         */
        return OpenShiftUtil.getRepoDir() + File.separator + path;

    }


    /**
     * Get a property as a short, if the property doesn't exist or can't be converted then we return the default value
     *
     * @param propertyName is the name of the property we are looking for (the key)
     * @param defaultValue is the value to return if the key doesn't exist or the value can't be converted to a short
     *
     * @return either the properly parsed short or the default value if the key doesn't exist or can't be converted
     */
    public short getShort(String propertyName, short defaultValue) {
        return NumberUtils.toShort(getProperty(propertyName), defaultValue);
    }

    /**
     * Check to see what properties would "collide" (have the same key name)
     *
     * @param toCheck are the properties that we want to compare our properties with
     *
     * @return a set of string which are they keys that overlap
     */
    public Set<String> getCollisions(Properties toCheck) {
        Set<String> commonValues = Sets.intersection(stringPropertyNames(),
                                                     toCheck.stringPropertyNames()).immutableCopy();
        return commonValues;
    }

    /**
     * Merge one set of properties with our properties.  It does this using a putAll so we copy the properties from
     * toMerge into our properties
     *
     * @param toMerge are the properties that we want to merge into our own
     * @param shouldOverride whether or not we should just override, if false then we will check for collisions
     *
     * @throws DuplicatePropertiesException if we are checking for collisions and we actually have collisions this will
     * contain all the "keys" which collide.
     */
    public void mergeProperties(Properties toMerge, boolean shouldOverride) throws DuplicatePropertyException {
        if(!shouldOverride) {
            Set<String> collisions = getCollisions(toMerge);
            if(!collisions.isEmpty()) {
                throw new DuplicatePropertyException(collisions);
            }
        }
        putAll(toMerge);
    }

    /**
     * This will glob properties files form a directory and merge them into our properties files.
     * {@see  mergeProperties(Properties, boolean)}
     *
     * @param directory the path of the directory to read from
     * @param globPattern a pattern of wild card characters for the filenames to match against
     * @param shouldOverride whether or not we should just override, if false then we will check for collisions
     *
     * @throws IOException if there are any problems with talking to the file system or if the directory doesn't exist
     * @throws DuplicatePropertyException if we are checking for collisions and we actually have collisions this will
     * contain all the "keys" which collide.
     */
    public void loadFromDirectory(String directory, String globPattern, boolean shouldOverride)
        throws IOException,DuplicatePropertyException {
        loadFromDirectory(Paths.get(directory), globPattern, shouldOverride);
    }


    /**
     * This will glob properties files form a directory and merge them into our properties files.
     * {@see  mergeProperties(Properties, boolean)}
     *
     * @param directory the path of the directory to read from
     * @param globPattern a pattern of wild card characters for the filenames to match against
     * @param shouldOverride whether or not we should just override, if false then we will check for collisions
     *
     * @throws DuplicatePropertyException if we are checking for collisions and we actually have collisions this will
     * contain all the "keys" which collide.
     * @throws IllegalArgumentException if the directory was null or is not a directory
     * @throws IOException if there are any problems with talking to the file system or if the directory doesn't exist
     */
    public void loadFromDirectory(Path directory, String globPattern, boolean shouldOverride)
        throws IOException,DuplicatePropertyException {
        if(directory == null || !Files.isDirectory(directory)) {
            String dir =  (directory == null) ? "null" : directory.toString();
            throw new IllegalArgumentException("The directory " + dir + " is null or does not exist!");
        }

        DirectoryStream<Path> ds = null;
        try{
            ds = Files.newDirectoryStream(directory, globPattern);
            for(Path p : ds) {
                Properties pro = new Properties();
                pro.load(new FileReader(p.toFile()));
                mergeProperties(pro, shouldOverride);
            }
        } finally {
            if(ds != null) {
                Closeables.close(ds, true);
            }
        }
    }

    /**
     * Loads properties from the class path.
     * @{see mergeProperties(Properties, boolean)
     *
     * @param resourceToLoad the class path resource to load
     * @param shouldOverride whether or not we should just override, if false then we will check for collisions
     *
     * @throws DuplicatePropertyException if we are checking for collisions and we actually have collisions this will
     * contain all the "keys" which collide.
     * @throws IllegalArgumentException if the class path was null or if we couldn't find it on the class path
     * @throws IOException if there are any problems with loading the file from the class path
     */
    public void loadFromClassPath(String resource, boolean shouldOverride)
        throws IOException, DuplicatePropertyException {
        URL resourceUrl = ClasspathResources.getResource(resource);
        if(resourceUrl == null) {
            throw new IllegalArgumentException(resource + " is either or null or does not exist on the class path!");
        }

        Properties toMerge = new Properties();
        toMerge.load(resourceUrl.openStream());
        mergeProperties(toMerge, shouldOverride);
    }

    private String decrypt(String value) {
        if(!PropertiesEncryptionUtil.isEncryptedValue(value)) {
            return value;
        }

        if(cryptoProvider == null) {
            throw new IllegalStateException("Crypto provider has not been initialized!");
        }

        return PropertiesEncryptionUtil.decryptPropertyValue(value, cryptoProvider);
    }
}
