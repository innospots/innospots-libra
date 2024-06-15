package io.innospots.workflow.console.starter;

import io.innospots.base.watcher.AbstractWatcher;
import io.innospots.workflow.core.node.definition.meta.NodeMetaInfoLoader;
import org.springframework.stereotype.Component;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/15
 */
@Component
public class NodeMetaWatcher extends AbstractWatcher {


    @Override
    public int execute() {
        NodeMetaInfoLoader.load();
        return 1000*60;
    }
}
