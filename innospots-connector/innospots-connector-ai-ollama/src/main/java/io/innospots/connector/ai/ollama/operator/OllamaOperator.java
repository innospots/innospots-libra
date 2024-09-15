package io.innospots.connector.ai.ollama.operator;

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.request.BaseRequest;
import org.springframework.ai.ollama.api.OllamaApi;
import reactor.core.publisher.Flux;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/15
 */
public class OllamaOperator implements IExecutionOperator {

    private OllamaApi ollamaApi;


    public OllamaOperator(String serviceAddress){
        if(serviceAddress == null){
            this.ollamaApi = new OllamaApi();
            return;
        }
        if(serviceAddress.startsWith("http://")){
            this.ollamaApi = new OllamaApi(serviceAddress);
        }else{
            this.ollamaApi = new OllamaApi("http://"+serviceAddress);
        }
    }

    @Override
    public <D> DataBody<D> execute(BaseRequest<?> itemRequest) {
        //TODO
        return IExecutionOperator.super.execute(itemRequest);
    }

    @Override
    public <D> Flux<D> executeStream(BaseRequest<?> itemRequest) {
        //TODO
        return IExecutionOperator.super.executeStream(itemRequest);
    }
}
