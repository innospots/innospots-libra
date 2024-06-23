package io.innospots.workflow.node.app;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/12/20
 */
public class TablesawTest {

    private static Table table;

    @BeforeAll
    static void setup() {
        table = Table.read().csv("/Users/yxy/quantificat/indicator_158/stock_file/sh600536.csv");
        System.out.println(table.columnNames());
        System.out.println(table.shape());
        System.out.println(table.structure().printAll());
    }

    @Test
    void test1() {

        //System.out.println(table);

        System.out.println(table.first(3));

        System.out.println(table.first(10).summary().printAll());
        Table result = table.where(table.doubleColumn("开盘价").isGreaterThan(60));
        result = result.selectColumns("开盘价", "交易日期");
        //result.rollingStream(5).forEach(c-> Arrays.stream(c).mapToDouble(r->r.getDouble(0)).sum());
//        System.out.println(result);

        result = result.first(10);
        System.out.println(result.printAll());
        System.out.println("=========");
        Column column = result.column("开盘价").rolling(3).calc(AggregateFunctions.mean);
        column.setName("Mean-3-开盘价");
        result = result.addColumns(column);
        result = result.addColumns(result.column("开盘价").rolling(3).calc(AggregateFunctions.max));
        System.out.println(result);
    }

    @Test
    void kdj() {
        DoubleColumn low40Column = table.doubleColumn("最低价_复权").rolling(40).min();
        low40Column.setName("Low40");
        DoubleColumn high40Column = table.doubleColumn("最高价_复权").rolling(40).max();
        high40Column.setName("High40");
        DoubleColumn rsvCol = table.doubleColumn("收盘价_复权").subtract(low40Column)
                .divide(high40Column.subtract(low40Column)).multiply(100);
        rsvCol.rolling(1).calc(AggregateFunctions.skewness);
    }


}
