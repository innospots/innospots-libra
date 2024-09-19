package io.innospots.connector.ai.ollama;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import reactor.core.publisher.Flux;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/15
 */
public class OllamaApiTest {

    @Test
    void test() {
        var ollamaApi = new OllamaApi();

        OllamaChatModel chatModel = new OllamaChatModel(ollamaApi,
                OllamaOptions.create()
                        .withModel("qwen2:7b")
                        .withFormat("json")
                        .withTemperature(0.9d));


        ChatResponse response = chatModel.call(
                new Prompt("人工智能的简介作文，200字"));
        System.out.println(response.getResult().getOutput());
//        System.out.println(response.getResult().getOutput().getContent());

// Or with streaming responses
//        Flux<ChatResponse> response = chatModel.stream(
//                new Prompt("Generate the names of 5 famous pirates."));

    }

    @Test
    void test2() {
        var ollamaApi = new OllamaApi();

        var chatModel = new OllamaChatModel(ollamaApi,
                OllamaOptions.create()
                        .withModel("qwen2:7b")
                        .withTemperature(0.9d));

        Flux<ChatResponse> response = chatModel.stream(
                new Prompt("人工智能"));

        response.doOnNext(chatResponse ->
                        {
                            System.out.println(chatResponse.getResult().getOutput());
                        }
                )
                .doFinally(signal -> System.out.println("Finished"))
                .blockLast();

    }

    @Test
    void test3() {
        var ollamaApi = new OllamaApi();

        var chatModel = new OllamaChatModel(ollamaApi,
                OllamaOptions.create()
                        .withModel("qwen2:7b")
                        .withTemperature(0.9d));

        String s = chatModel.call("介绍自己的能力");
        System.out.println(s);


    }
}
