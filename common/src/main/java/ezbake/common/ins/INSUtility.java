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

package ezbake.common.ins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that is used by INS to determine prefixes but may be needed by external applications when
 * communicating with INS
 */
public class INSUtility {
    private static final Pattern UriPattern = Pattern.compile("(\\w+://[_A-Za-z0-9.-]+)(/|$)");

    /**
     * Builds the URI prefix given a category and a key (typically the feed name)
     *
     * @param category    Category of data
     * @param categoryKey The key, which is typically the name of the feed
     * @return The prefix
     */
    public static String buildUriPrefix(String category, String categoryKey) {
        return category + "://" + categoryKey + "/";
    }

    /**
     * Given a document URI, will return the prefix
     *
     * @param uri The URI of the document
     * @return The prefix of the URI
     * @throws IllegalArgumentException thrown when the uri passed in is not a valid URI
     */
    public static String getUriPrefix(String uri) throws IllegalArgumentException {
        Matcher matcher = UriPattern.matcher(uri);
        if (matcher.find()) {
            String match = matcher.group(1);
            if (!match.endsWith("/")) {
                match = match + "/";
            }
            return match;
        } else {
            throw new IllegalArgumentException("Invalid uri");
        }
    }
}
