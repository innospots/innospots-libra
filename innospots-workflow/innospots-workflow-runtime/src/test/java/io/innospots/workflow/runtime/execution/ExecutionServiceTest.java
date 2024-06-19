package io.innospots.workflow.runtime.execution;

import io.innospots.base.utils.time.DateTimeUtils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Raydian
 * @date 2021/1/7
 */
class ExecutionServiceTest {


    @Test
    public void test1() {

        String abc = "1222";
        String bcd = null;
        String dd = "2";
        String s = abc + bcd + dd;
        System.out.println(s);
    }

    @Test
    public void test2() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<String> f1 = buildFuture("abc", 3);
        CompletableFuture<String> f2 = buildFuture("def", 5);
        CompletableFuture<String> f3 = buildFuture("ghi", 6);
        long s = System.currentTimeMillis();
        CompletableFuture all = CompletableFuture.allOf(f1, f2, f3);

        all.get(7, TimeUnit.SECONDS);

        Object ssd = Stream.of(f1, f2, f3).map(CompletableFuture::join).collect(Collectors.joining(","));

        String consume = DateTimeUtils.consume(s);
        System.out.println(consume + " ,return:" + ssd);
    }

    private CompletableFuture buildFuture(Consumer<String> consumer, String input) {
        return CompletableFuture.supplyAsync(() -> {
            consumer.accept(input);
            return input;
        });
    }

    private CompletableFuture buildFuture(String name, int time) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(time);
                System.out.println(name);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return name;
        });
    }

}