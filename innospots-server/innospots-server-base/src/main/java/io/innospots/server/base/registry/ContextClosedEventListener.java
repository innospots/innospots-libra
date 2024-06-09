package io.innospots.server.base.registry;

import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.base.utils.ServiceRoleHolder;
import io.innospots.base.utils.time.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/9
 */
@Slf4j
public class ContextClosedEventListener {

    @EventListener(ContextClosedEvent.class)
    public void onContextClosedEvent(ContextClosedEvent contextClosedEvent) {
        ServiceRoleHolder.shutdown();
        long startupDate = BeanContextAwareUtils.beanContextAware().getStartupDate();
        log.info("starting close server, current running time: {}, server info: {}",
                DateTimeUtils.consume(startupDate),
                ServiceRegistryHolder.getCurrentServer().info());
    }
}
