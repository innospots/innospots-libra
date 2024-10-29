package io.innospots.base.function;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Smars
 * @date 2023/9/2
 */
class StatisticFunctionTypeTest {

    @Test
    void values() {
        ArrayBlockingQueue<Double> queue = new ArrayBlockingQueue<>(100);
        System.out.println(queue.size());
        for (int i = 0; i < 20; i++) {
            queue.offer(Double.valueOf(i));
        }
        System.out.println(queue.remainingCapacity());
        System.out.println(queue.size());
        System.out.println(queue.stream().mapToDouble(Double::doubleValue).toArray().length);
        queue.poll();
        System.out.println(queue.size());
    }

    @Test
    void t(){
        List<Object> ll = new ArrayList<>(7);

        ll.add(2,332.2d);

        ll.add(6, 3d);
        System.out.println(ll);
    }
}