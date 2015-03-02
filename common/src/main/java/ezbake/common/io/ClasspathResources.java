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

package ezbake.common.io;

import com.google.common.base.Strings;
import com.google.common.io.Resources;
import java.net.URL;


/**
 * This class has utility methods for working with resources on the classpath.
 * Note: even though some methods use {@link java.net.URL} this is class really doesn't do anything with networking
 */
public final class ClasspathResources {
    /*
     * Attempt to loads a resource from the class path.  This uses {@link com.google.common.io.Resources} which will
     * look at the ThreadContext current context class loader or the current class {@link ClasspathResources} class
     * loader if the thread context class loader is null.  If we can't find it there we will use the getClass method on
     * the ClasspathResource class to look at the runtime instance and then we will use the class loader that loded this
     * class will be used.
     *
     * @param resource the name of the resource that we are looking for
     * @return a URL object or null if no resource with the name is found
     */
    public static URL getResource(String resource) {
        // no need to attempt to load
        if(Strings.isNullOrEmpty(resource)) {
            return null;
        }

        // First we will do it from class loaders
        URL retVal = null;
        try {
            retVal = Resources.getResource(resource);
        } catch(IllegalArgumentException ignored) {
            // We are ignoring this so we can try to load from the runtime classes
        }

        retVal = ClasspathResources.class.getClass().getResource(resource);
        if(retVal == null) {
            /* Just in case the java.class and ClasspathResources are loaded from differrent class loader then
            java.lang.class */
            retVal = ClasspathResources.class.getResource(resource);
        }
        return retVal;
    }
}
