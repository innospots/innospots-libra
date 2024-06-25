package io.innospots.workflow.runtime.flow.load;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Smars
 * @date 2024/6/24
 */
public class FLuxTest {

    @Test
    void test(){
        final Random random = new Random();
        Flux.generate(ArrayList::new, (list, sink) -> {
            int value = random.nextInt(100);
            list.add(value);
            sink.next(value);
            if( list.size() ==10 )
                sink.complete();
            return list;
        }).subscribe(System.out::println);
    }
}
