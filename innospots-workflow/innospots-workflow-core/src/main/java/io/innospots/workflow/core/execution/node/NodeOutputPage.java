package io.innospots.workflow.core.execution.node;

import io.innospots.base.model.PageBody;
import io.innospots.workflow.core.execution.ExecutionResource;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * @author Smars
 * @date 2023/8/18
 */
@Getter
@Setter
public class NodeOutputPage {

    private PageBody<Map<String, Object>> results = new PageBody<>();

    /**
     * key : item position
     */
    private Map<Integer,List<ExecutionResource>> resources;

    private Set<String> nextNodeKeys = new HashSet<>();

    private String name;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(results.getList());
    }
    public NodeOutputPage(long page,long size) {
        results.setPageSize(size);
        results.setCurrent(page);
    }

    public NodeOutputPage(NodeOutput nodeOutput,long page,long size) {
        results.setPageSize(size);
        results.setCurrent(page);
        results.setTotal(nodeOutput.getTotal());
        //results.setList(nodeOutput.getResults());
        this.resources = nodeOutput.getResources();
        this.nextNodeKeys = nodeOutput.getNextNodeKeys();
        this.name = nodeOutput.getName();
    }
    public void addItem(Map<String,Object> item){
        this.results.add(item);
    }
}
