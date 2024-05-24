package io.innospots.base.execution;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * manage all executor that can be defined in the multi module
 * @author Smars
 * @date 2024/5/23
 */
@Slf4j
public class ExecutorManager {

    private static Map<String,IExecutor> executorCache = new HashMap<>();

    public static IExecutor findExecutor(String identifier){
        return executorCache.get(identifier);
    }

    public static void register(IExecutor executor){
        if(executorCache.containsKey(executor.identifier())){
            log.warn("executor has registered, which will overwrite, {}",executor.info());
        }
        executorCache.put(executor.identifier(),executor);
    }

    public static void unregister(String identifier){
        executorCache.remove(identifier);
    }

    public static void clear(){
        executorCache.clear();
    }
}
