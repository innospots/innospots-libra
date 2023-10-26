package io.innospots.workflow.node.app;

import io.innospots.base.data.enums.DataOperation;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.node.instance.NodeInstance;
import io.innospots.workflow.node.app.data.DataNode;
import io.innospots.workflow.node.app.data.SqlDataNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/5/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SqlDataNodeTest.TestConfiguration.class)
public class SqlDataNodeTest {

    private DataNode dataNode;


    @SpringBootConfiguration
    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfiguration {

        /*
        @Bean
        public DataOperatorManager operatorBuilder(){
            IDataOperator dataOperator = Mockito.mock(IDataOperator.class);

            ISqlOperator sqlOperator = Mockito.mock(ISqlOperator.class);

            Map<String,Object> result = new HashMap<>();
            result.put("res","res123");
            result.put("time",System.currentTimeMillis());
            Mockito.when(dataOperator.queryForObject(
                    Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
                    .thenReturn(ResponseBody.success(new DataBody(result)));

            PageBody pageBody = new PageBody();
            pageBody.setConsume(200);
            pageBody.setPage(1);
            pageBody.setSize(20);
            pageBody.setTotalPage(10);
            pageBody.setStartTime(System.currentTimeMillis());
            List<Map<String, Object>> ff = new ArrayList<>();
            ff.add(result);
            ff.add(result);
            pageBody.setBody(ff);
            Mockito.when(dataOperator.queryForList(Mockito.anyString(),
                    Mockito.anyList(),Mockito.anyInt(),Mockito.anyInt()))
                    .thenReturn(ResponseBody.success(pageBody));

            Mockito.when(sqlOperator.queryForList(Mockito.anyString()))
                    .thenReturn(ResponseBody.success(pageBody));

            Mockito.when(dataOperator.update(Mockito.anyString(),Mockito.any()))
                    .thenReturn(ResponseBody.success(1));


            Mockito.when(dataOperator.insert(Mockito.anyString(),Mockito.any()))
                    .thenReturn(ResponseBody.success(1));


            DataOperatorBuilder dataOperatorBuilder = Mockito.mock(DataOperatorBuilder.class);
            Mockito.when(dataOperatorBuilder.buildDataOperator(Mockito.anyInt(),Mockito.anyString())).thenReturn(dataOperator);
            Mockito.when(dataOperatorBuilder.buildSqlOperator(Mockito.anyInt(),Mockito.anyString())).thenReturn(sqlOperator);
            return dataOperatorBuilder;
        }

         */

        @Bean
        public BeanContextAware applicationContextUtils() {
            return new BeanContextAware();
        }
    }

    @Before
    public void init() {

//        DataOperatorBuilder builder = ApplicationContextUtils.getBean(DataOperatorBuilder.class);

//        System.out.println(builder);
//        Assert.notNull(builder,"not null builder.");
    }

    @Test
    public void test() {

    }

    @Test
    public void build() {
        SqlDataNode dataNode = new SqlDataNode();
        String identifier = "Test_1";
        NodeInstance nodeInstance = nodeInstance();
        dataNode.build(identifier, nodeInstance);
    }

    @Test
    public void select() {
        SqlDataNode dataNode = new SqlDataNode();
        String identifier = "Test_1";
        NodeInstance nodeInstance = nodeInstance();
        nodeInstance.getData().put(SqlDataNode.FIELD_OPERATION, DataOperation.GET_ONE);
        dataNode.build(identifier, nodeInstance);
        dataNode.invoke(nodeExecution());
        //System.out.println(result);
    }

    @Test
    public void selectList() {
        SqlDataNode dataNode = new SqlDataNode();
        String identifier = "Test_1";
        NodeInstance nodeInstance = nodeInstance();
        nodeInstance.getData().put(SqlDataNode.FIELD_OPERATION, DataOperation.GET_LIST);
        dataNode.build(identifier, nodeInstance);
        dataNode.invoke(nodeExecution());
        //System.out.println(result);
    }

    @Test
    public void update() {
        SqlDataNode dataNode = new SqlDataNode();
        String identifier = "Test_1";
        NodeInstance nodeInstance = nodeInstance();
        nodeInstance.getData().put(SqlDataNode.FIELD_OPERATION, DataOperation.UPDATE);
        dataNode.build(identifier, nodeInstance);
        dataNode.invoke(nodeExecution());
        //System.out.println(result);
    }

    @Test
    public void insert() {
        SqlDataNode dataNode = new SqlDataNode();
        String identifier = "Test_1";
        NodeInstance nodeInstance = nodeInstance();
        nodeInstance.getData().put(SqlDataNode.FIELD_OPERATION, DataOperation.INSERT);
        dataNode.build(identifier, nodeInstance);
        dataNode.invoke(nodeExecution());
        //System.out.println(result);
    }


    private NodeExecution nodeExecution() {
        NodeExecution execution = NodeExecution.buildNewNodeExecution("abc", 22L, 1, "213", true);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("uid", "u123456789");
        inputs.put("user_name_001", "jay yuan");
        inputs.put("user_age", 25);
        inputs.put("user_address", "address 0101");
        inputs.put("uid_001", "uuid_0000111");
//        execution.setInput(inputs);
        return execution;
    }

    public static NodeInstance nodeInstance() {

        NodeInstance instance = new NodeInstance();
        instance.setHeight(100);
        instance.setWidth(200);
        instance.setNodeDefinitionId(1);
        instance.setPauseFlag(false);
        instance.setNodeInstanceId(2L);
        instance.setX(10);
        instance.setY(10);
        instance.setCode("MYSQL");
        instance.setDisplayName("mysql");
        instance.setNodeKey("abcd123456");
        instance.setNodeType("");
        List<String> nks = new ArrayList<>();
        nks.add("n123");
        nks.add("m123");
        instance.setNextNodeKeys(nks);
        instance.setNodeType("io.innospots.workflow.strategy.event.flow.node.data.SqlDataNode");
        List<ParamField> inputFields = new ArrayList<>();
        ParamField pf = new ParamField("user_name", "user_name", FieldValueType.FIELD_CODE);
        inputFields.add(pf);
        pf = new ParamField("uid", "uid", FieldValueType.FIELD_CODE);
        inputFields.add(pf);
//        instance.setInputFields(inputFields);
        Map<String, Object> data = new HashMap<>();

        data.put(SqlDataNode.FIELD_CREDENTIAL_ID, 12);
        data.put(SqlDataNode.FIELD_SQL_CLAUSE, "select user_name from user");
        data.put(SqlDataNode.FIELD_DB_NAME, "user_db");
        data.put(SqlDataNode.FIELD_OPERATION, DataOperation.GET_ONE);
        data.put(SqlDataNode.FIELD_TABLE_NAME, "user_table");
        List<Map<String, Object>> cl = new ArrayList<>();
        Map<String, Object> condition = new HashMap<>();
        condition.put("name", "userName");
        condition.put("opt", "EQUAL");
        condition.put("code", "user_name");
        condition.put("value", "${user_name_001}");
        condition.put("valueType", "FIELD_CODE");

        cl.add(condition);
        condition = new HashMap<>();
        condition.put("name", "uid");
        condition.put("code", "uid");
        condition.put("opt", "EQUAL");
        condition.put("value", "${uid_001}");
        condition.put("valueType", "FIELD_CODE");
        cl.add(condition);
        data.put(SqlDataNode.FIELD_QUERY_CONDITION, cl);

        List<Map<String, Object>> cc = new ArrayList<>();
        Map<String, Object> columns = new HashMap<>();
        columns.put("name", "user_age");
        columns.put("code", "user_age");
        cc.add(columns);
        columns = new HashMap<>();
        columns.put("name", "user_address");
        columns.put("code", "user_address");
        cc.add(columns);
        data.put(SqlDataNode.FIELD_COLUMN_MAPPING, cc);


        instance.setData(data);
        return instance;
    }
}