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

package ezbake.ezpurge;

/**
 * Created by sstapleton on 7/15/14.
 */

import java.io.Closeable;
import java.util.List;

import com.google.common.collect.Multimap;

public interface EzServicePurge extends Closeable {
    public void addPurgeService(final String appName, final String serviceName) throws Exception;
    public Multimap<String, String> getPurgeServices() throws Exception;
    public List<String> getPurgeServicesForApplication(String appName) throws Exception;
    public void removePurgeService(final String appName, final String serviceName) throws Exception;

}
