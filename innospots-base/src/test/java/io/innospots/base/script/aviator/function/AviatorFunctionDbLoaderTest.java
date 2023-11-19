package io.innospots.base.script.aviator.function;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import io.innospots.base.function.definition.model.FunctionDefinition;
import io.innospots.base.function.definition.manager.FunctionDefinitionManager;
import io.innospots.base.utils.time.DateTimeUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * @author Smars
 * @date 2021/9/5
 */
public class AviatorFunctionDbLoaderTest {

    @BeforeAll
    public void setup() {
        AviatorFunctionLoader.load();
    }

    @Test
    public void testFunc() {
        Collection<FunctionDefinition> fds = FunctionDefinitionManager.functions("aviator");
        for (FunctionDefinition fd : fds) {
            String exp = fd.getExpression().replaceAll("\\$\\{", "").replaceAll("\\}", "");
            System.out.println(exp);
            AviatorEvaluator.compile(exp);
        }
    }

    @Test
    public void load() {
        AviatorFunctionLoader.load();
        AviatorFunction function = AviatorEvaluator.getFunction("time.plusDay");
        System.out.println(function);
        assertNotNull(function);
        String exp = "time.plusDay(num,date)";
        Expression expression = AviatorEvaluator.compile(exp, true);
        Map<String, Object> env = new HashMap<>();
        env.put("num", 5);
        env.put("date", DateTimeUtils.formatDate(new Date(), DateTimeUtils.DEFAULT_DATE_PATTERN));
        System.out.println(env);
        Object v = expression.execute(env);
        System.out.println(v);
    }

    @Test
    public void tsExtract() {
        Map<String, Object> env = new HashMap<>();
        env.put("unit", ChronoUnit.DAYS.name());
        env.put("timestamp", String.valueOf(System.currentTimeMillis()));
        String exp = "ts.extract(unit,timestamp)";
        Expression expression = AviatorEvaluator.compile(exp, true);
        Object v = expression.execute(env);
        System.out.println(v);

        env.put("unit", ChronoUnit.MINUTES.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.HOURS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.MONTHS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.WEEKS.name());
        System.out.println(expression.execute(env));
    }

    @Test
    public void testDcal() {
        Map<String, Object> env = new HashMap<>();
        env.put("numberOfDay", 5);
        env.put("date", "2021-09-26");
        String exp = "time.plusDay(numberOfDay,date)";
        Expression expression = AviatorEvaluator.compile(exp, true);
        System.out.println(expression.execute(env));
        env.put("numberOfDay", -10);
        System.out.println(expression.execute(env));
    }

    @Test
    public void testDtcal() {
        String exp = "time.plusDateTime(unit,range,dt)";
        Expression expression = AviatorEvaluator.compile(exp, true);

        Map<String, Object> env = new HashMap<>();

        env.put("range", -10);
        env.put("dt", "2021-09-26 10:30:29");

        env.put("unit", ChronoUnit.DAYS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.HOURS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.MINUTES.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.SECONDS.name());
        System.out.println(expression.execute(env));

        env.put("unit", ChronoUnit.MONTHS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.WEEKS.name());
        System.out.println(expression.execute(env));
    }

    @Test
    public void testDtDiff() {
        AviatorFunction function = AviatorEvaluator.getFunction("time.diffDt");
        System.out.println(function);
        String exp = "time.diffDt(unit,startDt,endDt)";
        Expression expression = AviatorEvaluator.compile(exp, true);

        Map<String, Object> env = new HashMap<>();


        env.put("startDt", "2021-09-10 10:20:00");
        env.put("endDt", "2021-09-26 10:45:05");

        env.put("unit", ChronoUnit.DAYS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.HOURS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.MINUTES.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.SECONDS.name());
        System.out.println(expression.execute(env));

        env.put("unit", ChronoUnit.MONTHS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.WEEKS.name());
        System.out.println(expression.execute(env));
    }

    @Test
    public void testTcal() {
        AviatorFunction function = AviatorEvaluator.getFunction("time.plusTime");
        System.out.println(function);
        String exp = "time.plusTime(unit,range,dtime)";
        Expression expression = AviatorEvaluator.compile(exp, true);

        Map<String, Object> env = new HashMap<>();

        env.put("range", 10);
        env.put("dtime", "10:20:00");

        env.put("unit", ChronoUnit.HOURS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.MINUTES.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.SECONDS.name());
        System.out.println(expression.execute(env));
    }

    @Test
    public void testTF() {
        AviatorFunction function = AviatorEvaluator.getFunction("time.format");
        System.out.println(function);
        String exp = "time.format(input,sFormat,tFormat)";
        Expression expression = AviatorEvaluator.compile(exp, true);

        Map<String, Object> env = new HashMap<>();

        env.put("input", "2021-09-23 18:20:35");
        env.put("sFormat", "yyyy-MM-dd HH:mm:ss");
        env.put("tFormat", "MM-dd");
        System.out.println(expression.execute(env));

        env.put("tFormat", "HH:mm:ss");
        System.out.println(expression.execute(env));
        env.put("tFormat", "yyyy-MM-dd");
        System.out.println(expression.execute(env));
    }

    @Test
    public void testTs() {
        AviatorFunction function = AviatorEvaluator.getFunction("ts.toTs");
        System.out.println(function);
        String exp = "ts.toTs(input)";
        Expression expression = AviatorEvaluator.compile(exp, true);

        Map<String, Object> env = new HashMap<>();

        env.put("input", System.currentTimeMillis());
        System.out.println(expression.execute(env));

        env.put("input", System.currentTimeMillis() + "");
        System.out.println(expression.execute(env));
    }


    @Test
    public void testTscal() {
        String exp = "ts.plusTs(unit,range,ts)";
        Expression expression = AviatorEvaluator.compile(exp, true);

        Map<String, Object> env = new HashMap<>();

        env.put("ts", System.currentTimeMillis());
        env.put("range", 20);

        env.put("unit", ChronoUnit.DAYS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.HOURS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.MINUTES.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.SECONDS.name());
        System.out.println(expression.execute(env));

        env.put("unit", ChronoUnit.MONTHS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.WEEKS.name());
        System.out.println(expression.execute(env));
    }

    @Test
    public void testTsDiff() {
        String exp = "ts.diffTs(unit,startTs,endTs)";
        Expression expression = AviatorEvaluator.compile(exp, true);

        Map<String, Object> env = new HashMap<>();

        env.put("startTs", System.currentTimeMillis());
        env.put("endTs", System.currentTimeMillis() + 3600 * 24 * 1000 * 10);

        env.put("unit", ChronoUnit.DAYS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.HOURS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.MINUTES.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.SECONDS.name());
        System.out.println(expression.execute(env));

        env.put("unit", ChronoUnit.MONTHS.name());
        System.out.println(expression.execute(env));
        env.put("unit", ChronoUnit.WEEKS.name());
        System.out.println(expression.execute(env));
    }

}