package io.innospots.workflow.core.logger;

import cn.hutool.core.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Smars
 * @date 2024/6/26
 */
@Slf4j
public class FlowLoggerFactory {

    private static final String LOGGER_PKG = "io.innospots.workflow";

    private static CompositeFlowLogger flowLogger;

    public static IFlowLogger getLogger() {
        if (flowLogger != null) {
            return flowLogger;
        }
        Set<Class<?>> flowClassSet = ClassUtil.scanPackageBySuper(LOGGER_PKG, IFlowLogger.class);
        List<IFlowLogger> flowLoggers = new ArrayList<>();
        for (Class<?> flowClass : flowClassSet) {
            try {
                if (CompositeFlowLogger.class.equals(flowClass)) {
                    continue;
                }
                IFlowLogger flowLogger = (IFlowLogger) flowClass.getConstructor().newInstance();
                flowLoggers.add(flowLogger);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error(e.getMessage(), e);
            }
        }
        flowLogger = new CompositeFlowLogger(flowLoggers);
        return flowLogger;
    }

}
