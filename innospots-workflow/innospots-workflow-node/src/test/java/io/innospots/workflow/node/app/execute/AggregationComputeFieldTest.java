package io.innospots.workflow.node.app.execute;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Smars
 * @date 2023/8/21
 */
class AggregationComputeFieldTest {

    @Test
    void variance() {

        Variance variance = new Variance();
        double[] values = new double[20];
        for (int i = 0; i < 20; i++) {
            variance.increment(i);
            values[i] = i;
        }
        System.out.println(variance.getResult());
        System.out.println(variance.evaluate(values));
    }

    @Test
    void percentile() {
        Percentile percentile = new Percentile();
        double[] values = new double[20];
        for (int i = 0; i < 20; i++) {
            values[i] = i;
        }
        System.out.println(percentile.evaluate(values,100));
        System.out.println(percentile.evaluate(values,75));
        System.out.println(percentile.evaluate(values,50));
        System.out.println(percentile.evaluate(values,25));
    }
}