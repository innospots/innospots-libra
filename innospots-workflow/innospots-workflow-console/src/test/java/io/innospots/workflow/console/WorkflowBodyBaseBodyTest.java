package io.innospots.workflow.console;

import io.innospots.workflow.core.flow.model.WorkflowBaseBody;
import io.innospots.workflow.core.instance.model.Edge;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
class WorkflowBodyBaseBodyTest {

    private Edge getEdge(String source, String target) {
        Edge edge = new Edge();
        edge.setSource(source);
        edge.setTarget(target);
        return edge;
    }

    @Test
    void checkNodeCircle() {

        WorkflowBaseBody instanceBaseBody = new WorkflowBaseBody();
        List<Edge> edgeList = new ArrayList<>();
        edgeList.add(getEdge("a", "b"));
        edgeList.add(getEdge("b", "c1"));
        edgeList.add(getEdge("b", "c2"));
        edgeList.add(getEdge("c1", "d1"));
        edgeList.add(getEdge("c1", "d2"));
        edgeList.add(getEdge("c2", "d2"));
        edgeList.add(getEdge("d2", "b"));
        instanceBaseBody.setEdges(edgeList);
        if (instanceBaseBody.checkNodeCircle()) {
            log.error("find node circle");
        }
    }
}