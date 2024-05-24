package io.innospots.schedule.job.db.sharding;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.operator.jdbc.SelectClause;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/5/18
 */
@Slf4j
public class DbKeyShardingJob extends AbstractShardingJob<List<Object>> {


    public DbKeyShardingJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public void prepare() {
        super.prepare();
        shardingColumn = shardingColumn();
        shardingCount = validParamInteger(PARAM_SHARDING_COUNT);
        shardingCount = shardingList.size() < shardingCount ? shardingList.size() : shardingCount;
        jobExecution.setSubJobCount((long) shardingCount);
    }


    @Override
    protected void fillParams(Map<String, Object> params, List<Object> shardKey) {
        SelectClause selectClause = buildWhereClause(shardKey);
        params.put(PARAM_EXECUTE_READ_CLAUSES, selectClause.buildSql());
    }

    private SelectClause buildWhereClause(List<Object> values) {
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName(table);
        selectClause.addOrderBy(shardingColumn);
        if (values != null) {
            selectClause.addWhereInclude(shardingColumn, values, FieldValueType.convertTypeByValue(values.get(0)));
        }
        if (shardingClause != null) {
            selectClause.setWhereClause(shardingClause);
        }
        return selectClause;
    }

    @Override
    protected List<List<Object>> shardingList() {
        List<Object> shardingValues = shardingValues();
        List<List<Object>> ll = new ArrayList<>();
        int batchSize = shardingValues.size() / shardingCount;
        int pos = 0;
        for (int i = 0; i < shardingCount; i++) {
            List<Object> sv = new ArrayList<>();
            for (int j = 0; j < batchSize; j++) {
                sv.add(shardingValues.get(pos++));
            }
            ll.add(sv);
        }//end for
        if (pos < shardingValues.size()) {
            List<Object> sv = new ArrayList<>();
            for (int i = pos; i < shardingValues.size(); i++) {
                sv.add(shardingValues.get(i));
            }
            ll.add(sv);
        }
        return ll;
    }


    /**
     * select clause
     *
     * @return
     */
    private List<Object> shardingValues() {
        PageBody<Map<String, Object>> shardingCols = dataOperator.selectForList(buildShardingColumn());
        List<Object> values = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(shardingCols.getList())) {
            for (Map<String, Object> itemVal : shardingCols.getList()) {
                Object value = itemVal.get(shardingColumn);
                if (value != null) {
                    values.add(value);
                }
            }//end for
        } else {
            log.warn("sharding value is empty:{}, {}, {}", table, shardingColumn, shardingClause);
        }
        return values;
    }

    private SelectClause buildShardingColumn() {
        SelectClause selectClause = new SelectClause();
        selectClause.setTableName(table);
        selectClause.addOrderBy(shardingColumn);
        selectClause.addColumn("distinct(" + shardingColumn + ")");
        if (shardingClause != null) {
            selectClause.setWhereClause(shardingClause);
        }
        return selectClause;
    }
}
