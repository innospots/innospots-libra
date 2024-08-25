package io.innospots.base.model.field;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * @author Smars
 * @date 2023/9/22
 */
class FieldValueTypeTest {


    @Test
    void testDate() {
        String[] times = new String[]{"2023-09-11", "2013.02.21", "2059/01/18",
                "23:15:18", "09:10", "09:10:22.176",
                "2020-05-12 10:07:18", "2015.05.01T07:15", "2022/03/22 01:30:15.654"};
        for (String time : times) {
            FieldValueType valueType = FieldValueTypeConverter.convertJavaTypeByValue(time);
            System.out.println(time + " : " + valueType);
            Object v = valueType.normalize(time);
            System.out.println(v.getClass() + " : " + v);
        }
    }

    @Test
    void testAdd() {
        String[] times = new String[]{"2023-09-11", "2013.02.21", "2059/01/18"};
        for (String time : times) {
            FieldValueType valueType = FieldValueTypeConverter.convertJavaTypeByValue(time);
            System.out.println(time + " : " + valueType);
            Object v = valueType.normalize(time);
            LocalDate ld = valueType.plusTime(time, 2, ChronoUnit.MONTHS, LocalDate.class);
            System.out.println("add: " + ld);
        }
    }

    @Test
    void testAdd2() {
        String[] times = new String[]{"23:15:18", "09:10", "09:10:22.176"};
        for (String time : times) {
            FieldValueType valueType = FieldValueTypeConverter.convertJavaTypeByValue(time);
            System.out.println(time + " : " + valueType);
            Object v = valueType.normalize(time);
            LocalTime ld = valueType.plusTime(time, -40, ChronoUnit.MINUTES, LocalTime.class);
            System.out.println("add time: " + ld);
        }
    }

    @Test
    void testFormat() {
        String[] times = new String[]{"2020-05-12 10:07:18", "2015.05.01T07:15", "2022/03/22 01:30:15.654"};
        for (String time : times) {
            FieldValueType valueType = FieldValueTypeConverter.convertJavaTypeByValue(time);
            System.out.println(time + " : " + valueType);
            Object v = valueType.normalize(time, "mm/dd/yyyy");
            System.out.println(v.getClass() + " : " + v);

        }
    }

    @Test
    void test2(){
        String[] vals = new String[]{"9922.1122","223.121d","21321.111223456"};
        for (String val : vals) {
            FieldValueType valueType = FieldValueTypeConverter.convertJavaTypeByValue(val);
            System.out.println(val + " : " + valueType);
            Object v = FieldValueType.DOUBLE.normalize(val);
            System.out.println(v + ": " + v.getClass());
        }
    }

}