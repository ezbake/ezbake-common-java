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

package ezbake.base.thrift;

import com.codahale.metrics.*;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import ezbake.base.thrift.metrics.*;

import java.util.*;

/**
 * A Simple builder that can aid in the building of a MetricRegistryThrift
 * object.  Also has a nice helper {@link #buildFrom(com.codahale.metrics.MetricRegistry)} method
 * to convert from a codahale drop-wizard metrics object into the MetricRegistryThrift.
 */
public class EzBakeMetricRegistryBuilder {
    private MetricRegistryThrift result = new MetricRegistryThrift();
    private Gson gson = new Gson();

    public EzBakeMetricRegistryBuilder() {
        result.setCounters(new HashMap<String, CounterThrift>());
        result.setGauges(new HashMap<String, GaugeThrift>());
        result.setMeters(new HashMap<String, MeteredThrift>());
        result.setHistograms(new HashMap<String, HistogramThrift>());
        result.setTimers(new HashMap<String, TimerThrift>());

    }

    public EzBakeMetricRegistryBuilder addCount(String name, long count) {
        result.getCounters().put(name, new CounterThrift(count));
        return this;
    }

    public EzBakeMetricRegistryBuilder addGauge(String name, GaugeThrift gauge) {
        result.getGauges().put(name, gauge);
        return this;
    }

    public EzBakeMetricRegistryBuilder addHistogram(String name, HistogramThrift histogram) {
        result.getHistograms().put(name, histogram);
        return this;
    }

    public EzBakeMetricRegistryBuilder addMeter(String name, MeteredThrift meter) {
        result.getMeters().put(name, meter);
        return this;
    }

    public EzBakeMetricRegistryBuilder addTimer(String name, TimerThrift timer) {
        result.getTimers().put(name, timer);
        return this;
    }

    public static MetricRegistryThrift buildFrom(MetricRegistry registry) {
        EzBakeMetricRegistryBuilder builder = new EzBakeMetricRegistryBuilder();

        builder.addAllCounters(registry.getCounters())
                .addAllGauges(registry.getGauges())
                .addAllMeters(registry.getMeters())
                .addAllHistograms(registry.getHistograms())
                .addAllTimers(registry.getTimers());

        return builder.result;
    }


    public EzBakeMetricRegistryBuilder addAllCounters(Map<String, Counter> counters) {
        for (Map.Entry<String, Counter> entry : counters.entrySet() ) {
            addCount(entry.getKey(), entry.getValue().getCount());
        }
        return this;
    }

    public EzBakeMetricRegistryBuilder addAllGauges(Map<String, Gauge> gauges) {
        for (Map.Entry<String, Gauge> entry : gauges.entrySet() ) {
            GaugeThrift g = new GaugeThrift();
            try {
                //Due to the object can be anything, we need some sort
                //of serialization scheme lets just use gson because it
                //it will mostly be Integers most likely and that will just
                // expand to the String version of the Integer
                String value = gson.toJson(entry.getValue().getValue());
                g.setValue(value);
            } catch (Exception e) {
                g.setError(e.getMessage());
            }
            addGauge(entry.getKey(), g);
        }
        return this;
    }

    public EzBakeMetricRegistryBuilder addAllMeters(Map<String, Meter> meters) {
        for (Map.Entry<String, Meter> entry : meters.entrySet() ) {
            addMeter(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public EzBakeMetricRegistryBuilder addAllHistograms(Map<String, Histogram> histrograms) {
        for (Map.Entry<String, Histogram> entry : histrograms.entrySet() ) {
            addHistogram(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public EzBakeMetricRegistryBuilder addAllTimers(Map<String, Timer> timers) {
        for (Map.Entry<String, Timer> entry : timers.entrySet() ) {
            addTimer(entry.getKey(), entry.getValue());
        }
        return this;
    }

    private void addHistogram(String name, Histogram histogram) {
        List<Long> values = Lists.newArrayListWithCapacity(histogram.getSnapshot().getValues().length);
        for(Long l : histogram.getSnapshot().getValues() )
            values.add(l);

        addHistogram(name, new HistogramThrift()
                        .setCount(histogram.getCount())
                        .setSnapshot(toThrift(histogram.getSnapshot())));
    }

    private void addTimer(String name, Timer timer) {
        List<Double> values = Lists.newArrayListWithCapacity(timer.getSnapshot().getValues().length);
        for(Long d : timer.getSnapshot().getValues() )
            values.add(Double.valueOf(d));

        addTimer(name, new TimerThrift()
                        .setCount(timer.getCount())
                        .setSnapshot(toThrift(timer.getSnapshot()))
                        .setMeter(toThrift(timer)));
    }

    private void addMeter(String name, Meter meter) {
        addMeter(name, toThrift(meter));
    }

    private SnapShotThrift toThrift(Snapshot snapshot) {
        List<Long> values = Lists.newArrayListWithCapacity(snapshot.getValues().length);
        for(Long l : snapshot.getValues() )
            values.add(l);

        return new SnapShotThrift().setMax(snapshot.getMax())
                .setMean(snapshot.getMean())
                .setMin(snapshot.getMin())
                .setMedian(snapshot.getMedian())
                .setP75(snapshot.get75thPercentile())
                .setP95(snapshot.get95thPercentile())
                .setP98(snapshot.get98thPercentile())
                .setP99(snapshot.get99thPercentile())
                .setP999(snapshot.get999thPercentile())
                .setValues(values)
                .setStdDev(snapshot.getStdDev());
    }

    private MeteredThrift toThrift(Metered meter) {
        return new MeteredThrift()
                .setCount(meter.getCount())
                .setM15Rate(meter.getFifteenMinuteRate())
                .setM1Rate(meter.getOneMinuteRate())
                .setM5Rate(meter.getFifteenMinuteRate())
                .setMeanRate(meter.getMeanRate());
    }
}
