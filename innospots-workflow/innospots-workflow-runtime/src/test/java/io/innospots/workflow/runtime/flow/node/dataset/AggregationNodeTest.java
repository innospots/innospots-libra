package io.innospots.workflow.runtime.flow.node.dataset;

import io.innospots.base.condition.*;
import io.innospots.base.function.StatisticFunctionType;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.utils.BeanUtils;
import io.innospots.base.utils.DataFakerUtils;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.node.app.compute.AggregationComputeField;
import io.innospots.workflow.node.app.dataset.AggregationNode;
import io.innospots.workflow.runtime.flow.node.IDataNodeTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/23
 */
public class AggregationNodeTest implements IDataNodeTest {

    @Test
    void test() {
        this.testExecute(20);
    }

    @Override
    public NodeInstance build() {
        Map<String, Object> m = new HashMap<>();
        m.put(AggregationNode.FIELD_SOURCE_TYPE, "list");
        m.put(AggregationNode.FIELD_DIMENSION_LIST, dimeFields());
        m.put(AggregationNode.FIELD_AGGREGATE, aggregateFields());

        NodeInstance ni = this.build(AggregationNode.class, m);
        return ni;
    }

    private List<Map<String, Object>> dimeFields() {
        List<Map<String, Object>> dfs = new ArrayList<>();
        NodeParamField df = new NodeParamField();
        df.setCode("country");
        df.setName("country");
        df.setValueType(FieldValueType.STRING);
        String s = JSONUtils.toJsonString(df);

        dfs.add(JSONUtils.toMap(s));

        return dfs;
    }

    private List<Map<String, Object>> aggregateFields() {
        List<Map<String, Object>> dfs = new ArrayList<>();
        AggregationComputeField acf = new AggregationComputeField();
        acf.setName("aggrCnt");
        acf.setCode("aggrCnt");
        acf.setComment("aggrCnt");
        acf.setValueType(FieldValueType.INTEGER);
        acf.setFunctionType(StatisticFunctionType.SUM);
        NodeParamField sf = new NodeParamField();
        sf.setCode("cnt1");
        sf.setName("cnt1");
        sf.setValueType(FieldValueType.INTEGER);
        acf.setSummaryField(sf);

        EmbedCondition ecd = new EmbedCondition();
        ecd.setRelation(Relation.AND);
        ecd.setMode(Mode.SCRIPT);
        List<Factor> factors = new ArrayList<>();
        Factor factor = new Factor();
        factor.setCode("country");
        factor.setOpt(Opt.UNEQUAL);
        factor.setValue("abcd");
        factor.setValueType(FieldValueType.STRING);
        factors.add(factor);

        ecd.setFactors(factors);
        acf.setCondition(ecd);
        String s = JSONUtils.toJsonString(acf);

        dfs.add(JSONUtils.toMap(s));
        return dfs;
    }

    @Override
    public Map<String, Object> sampleInput() {
        Map<String, Object> m = new HashMap<>();
        DataFakerUtils df = DataFakerUtils.build();
        m.put("country", df.gen("dist1", "dist2", "dist3"));
        m.put("cnt1", Integer.parseInt(df.genNumbers(3)));
        return m;
    }
}
