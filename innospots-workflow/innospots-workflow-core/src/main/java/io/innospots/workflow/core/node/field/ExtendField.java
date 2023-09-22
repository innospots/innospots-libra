package io.innospots.workflow.core.node.field;

import cn.hutool.core.util.ReUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.condition.Factor;
import io.innospots.base.utils.DateTimeUtils;
import io.innospots.base.utils.Initializer;
import io.innospots.workflow.core.exception.NodeFieldException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Smars
 * @date 2023/9/20
 */
@Getter
@Setter
@Slf4j
public class ExtendField implements Initializer {


    private ExtendFunction function;

    private String code;

    /**
     * comma to separate multi param
     */
    private String param;

    private Factor field;

    @JsonIgnore
    private Map<String, Object> dict;

    @Override
    public void initialize() {
        if (function == ExtendFunction.dict_map) {
            dict = new HashMap<>();
            if (StringUtils.isNotEmpty(param)) {
                String[] vv = param.split(",");
                for (String v : vv) {
                    String[] dv = v.split(":");
                    if (dv.length >= 2) {
                        dict.put(dv[0], dv[1]);
                    }
                }
            }
        }
    }

    public Object compute(Map<String, Object> item) {
        Object val = field != null ? item.get(field.getCode()) : null;
        try {
            switch (function) {
                case crt_date:
                    String format = param;
                    if (StringUtils.isEmpty(format)) {
                        format = DateTimeUtils.DEFAULT_DATE_PATTERN;
                    }
                    val = DateTimeFormatter.ofPattern(format).format(LocalDate.now());
                    break;
                case crt_date_time:
                    String fmt = param;
                    if (StringUtils.isEmpty(fmt)) {
                        fmt = DateTimeUtils.DEFAULT_DATETIME_PATTERN;
                    }
                    val = DateTimeFormatter.ofPattern(fmt).format(LocalDateTime.now());
                    break;
                case date_format:
                    if (StringUtils.isNotEmpty(param) && field != null && field.getValueType() != null) {
                        val = field.getValueType().normalize(val, param);
                    }
                    break;
                case plus_year:
                    if (StringUtils.isNotEmpty(param) && field != null && field.getValueType() != null && field.getValueType().isTime()) {
                        long addAmt = Long.parseLong(param);
                        val = field.getValueType().plusTime(val, addAmt, ChronoUnit.YEARS);
                    }
                    break;
                case plus_month:
                    if (StringUtils.isNotEmpty(param) && field != null && field.getValueType() != null && field.getValueType().isTime()) {
                        long addAmt = Long.parseLong(param);
                        val = field.getValueType().plusTime(val, addAmt, ChronoUnit.MONTHS);
                    }
                    break;
                case plus_day:
                    if (StringUtils.isNotEmpty(param) && field != null && field.getValueType() != null && field.getValueType().isTime()) {
                        long addAmt = Long.parseLong(param);
                        val = field.getValueType().plusTime(val, addAmt, ChronoUnit.DAYS);
                    }
                    break;
                case plus_hour:
                    if (StringUtils.isNotEmpty(param) && field != null && field.getValueType() != null && field.getValueType().isTime()) {
                        long addAmt = Long.parseLong(param);
                        val = field.getValueType().plusTime(val, addAmt, ChronoUnit.HOURS);
                    }
                    break;
                case regular_exp:
                    if (StringUtils.isNotEmpty(param) && val != null) {
                        List<String> groups = ReUtil.getAllGroups(Pattern.compile(param), val.toString(), false);
                        if (groups != null) {
                            return String.join(",", groups);
                        }
                    }
                    break;
                case dict_map:
                    val = dict.get(String.valueOf(val));
                default:
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return val;
    }


    public enum ExtendFunction {
        //
        crt_date,
        crt_date_time,
        date_format,
        plus_year,
        plus_month,
        plus_day,
        plus_hour,
        regular_exp,
        dict_map;

    }

}
