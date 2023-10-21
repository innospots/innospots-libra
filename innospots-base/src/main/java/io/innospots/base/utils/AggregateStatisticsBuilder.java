package io.innospots.base.utils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

/**
 * @author Smars
 * @date 2023/8/21
 */
public class AggregateStatisticsBuilder {



    public static class AggregateStatistics implements StatisticalSummary {

        private DescriptiveStatistics descriptiveStatistics;

        @Override
        public double getMean() {
            return 0;
        }

        @Override
        public double getVariance() {
            return 0;
        }

        @Override
        public double getStandardDeviation() {
            return 0;
        }

        @Override
        public double getMax() {
            return 0;
        }

        @Override
        public double getMin() {
            return 0;
        }

        @Override
        public long getN() {
            return 0;
        }

        @Override
        public double getSum() {
            return 0;
        }
    }

}
