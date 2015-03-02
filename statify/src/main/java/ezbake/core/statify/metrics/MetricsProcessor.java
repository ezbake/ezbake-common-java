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

package ezbake.core.statify.metrics;

import java.util.SortedMap;
import java.util.Properties;
import java.net.ConnectException;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;

/**
 * To process metrics, implement {@link ezbake.core.statify.metrics.MetricsProcessor#process(SortedMap, SortedMap, SortedMap, SortedMap, SortedMap)}
 */
public interface MetricsProcessor {
    
    /**
     * Initialize is called by the reporter on startup
     * @param properties the ezbake configuration properties
     * @throws ConnectException thrown when unable to connect to endpoint
     * @throws Exception
     */
    public void initialize(Properties properties) throws ConnectException, Exception;
    
    /**
     * Provides access to metrics when Reporter is triggered to report.
     * @param gauges {@literal SortedMap<String, Gauges>}
     * @param counters {@literal SortedMap<String, Counter>}
     * @param histograms {@literal SortedMap<String, Histogram>}
     * @param meters {@literal SortedMap<String, Meter>}
     * @param timers {@literal SortedMap<String, Timer>}
     */
    public void process(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers);
   
}
