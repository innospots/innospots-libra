/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.workflow.node.app.logic;


import io.innospots.base.condition.EmbedCondition;
import io.innospots.base.condition.Mode;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.re.aviator.AviatorExpression;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;
import io.innospots.workflow.node.app.utils.NodeInstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * condition expression execute return false or true
 *
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
public class ConditionNode extends BaseAppNode {


    private static final Logger logger = LoggerFactory.getLogger(ConditionNode.class);

    private List<String> trueNextNodeKeys;
    private List<String> falseNextNodeKeys;
    private String conditionExpression;

    public static final String FIELD_TRUE_ANCHOR_KEY = "true-out";
    public static final String FIELD_FALSE_ANCHOR_KEY = "false-out";
    public static final String FIELD_CONDITIONS = "conditions";

    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        this.trueNextNodeKeys = nodeInstance.findNodeKeysByAnchor(FIELD_TRUE_ANCHOR_KEY);
        this.falseNextNodeKeys = nodeInstance.findNodeKeysByAnchor(FIELD_FALSE_ANCHOR_KEY);
        EmbedCondition embedCondition = NodeInstanceUtils.buildCondition(nodeInstance,FIELD_CONDITIONS,this);
        if(embedCondition==null){
            throw ConfigException.buildMissingException(this.getClass(), "nodeKey:" + this.nodeKey() + ", field:" + FIELD_CONDITIONS);
        }
        this.conditionExpression =embedCondition.getStatement();
        this.expression = new AviatorExpression(this.conditionExpression, null);

    }

    @Override
    public void invoke(NodeExecution nodeExecution) {

        if (expression != null) {
            NodeOutput trueOut = new NodeOutput("true");
            if (this.trueNextNodeKeys != null) {
                trueOut.addNextKey(this.trueNextNodeKeys);
            }
            NodeOutput falseOut = new NodeOutput("false");
            if (this.falseNextNodeKeys != null) {
                falseOut.addNextKey(this.falseNextNodeKeys);
            }

            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                for (Map<String, Object> data : executionInput.getData()) {
                    Object result = expression.execute(data);
                    logger.debug("ifNode,condition: {}, result:{}, execute:{}", conditionExpression, result, data);
                    boolean v = booleanValue(result);
                    if (v) {
                        trueOut.addResult(data);
                    } else {
                        falseOut.addResult(data);
                    }

                }//end for
            }//end execution input
            nodeExecution.addOutput(trueOut);
            nodeExecution.addOutput(falseOut);
        }//end if
    }

    private boolean booleanValue(Object v) {
        boolean o = false;
        if (v instanceof String) {
            o = Boolean.parseBoolean((String) v);
        }

        if (v instanceof Boolean) {
            o = (Boolean) v;
        }//end if
        return o;
    }

    /*
    @Override
    public MethodBody expSource(NodeInstance nodeInstance) {
        MethodBody methodBody = new MethodBody(ScriptType.JAVA);
        String src = null;
        try {
            Map<String,Object> dataModel = new HashMap<>();
            EmbedCondition condition = buildCondition(nodeInstance);
            dataModel.put("condition",condition.getStatement());
            src = SourceTemplateUtils.output(this.getClass().getSimpleName()+".ftl",dataModel);
            methodBody.setSrcBody(src);
            //methodBody.setParams(nodeInstance.getInputFields());
            methodBody.setReturnType(Boolean.class);
            methodBody.setMethodName(this.expName());
        } catch (IOException | TemplateException e) {
            logger.error(e.getMessage(),e);
                throw ConfigException.buildTypeException(this.getClass(),"if template invalid format, nodeKey:"+nodeKey + ", error: "+e.getMessage());
        }
        if(logger.isDebugEnabled()){
            logger.debug("if node nodeKey:{}, source: {}",this.nodeKey,src);
        }

        return methodBody;
    }

     */

}
