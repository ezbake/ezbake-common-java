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
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;

/**
 *
 */
public class MetricsReporter extends ScheduledReporter {

    protected MetricsProcessor processor;
    private static Logger log = LoggerFactory.getLogger(MetricsReporter.class);
    
    /**
     * @param properties properties with Flume client connection information, application name, etc.
     * @param processor of type {@link ezbake.core.statify.metrics.MetricsProcessor#Metricsprocessor}
     * @param registry
     * @param name
     * @param filter
     * @param rateUnit
     * @param durationUnit
     */
    protected MetricsReporter(MetricsProcessor processor, MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) throws ConnectException, Exception {
        super(registry, name, filter, rateUnit, durationUnit);
        this.processor = processor;
        log.info("MetricsReporter initialized");
    }
    
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        log.debug("Report called on processor");
        processor.process(gauges, counters, histograms, meters, timers);
    }
    
    /**
     * Returns a new {@link Builder} for {@link MetricsReporter}.
     *
     * @param registry the registry to report to
     * @return a {@link Builder} instance for a {@link MetricsReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }
    
    /**
     * A builder for {@link MetricsReporter} instances. Defaults to converting
     * rates to events/second, converting durations to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private String name;
        private Clock clock;
        private TimeUnit rateUnit;
        private MetricFilter filter;
        private TimeUnit durationUnit;
        private Properties properties;
        private MetricsProcessor processor;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.filter = MetricFilter.ALL;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS; 
        }
        
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        
        /**
         * A processor of type MetricsProcessor. <br />
         * This processor will be called to process metrics when the Reporter reports.
         *
         * @param processor {@link ezbake.core.statify.metrics.MetricsProcessor#Metricsprocessor}
         * @return {@code this}
         */
        public Builder withProcessor(MetricsProcessor processor) {
            this.processor = processor;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Use the given {@link Clock} instance for the time.
         *
         * @param clock a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link MetricsReporter} with the given properties, writing stats to EzMetrics.
         *
         * @return a {@link MetricsReporter}
         * @throws ConnectException on Flume connection failure
         */
        public MetricsReporter build() throws ConnectException, Exception {
            return new MetricsReporter(processor, registry, name, filter, rateUnit, durationUnit);
        }
    }

}
