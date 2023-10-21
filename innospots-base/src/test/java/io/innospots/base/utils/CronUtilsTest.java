package io.innospots.base.utils;

import cn.hutool.core.net.url.UrlBuilder;
import io.innospots.base.quartz.TimePeriod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.CronExpression;

import java.nio.charset.Charset;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CronUtilsTest
 *
 * @author zwg
 * @date 2022/4/3-16:46
 */
@Slf4j
public class CronUtilsTest {
    @Test
    public void createCronExpressionTest() {
        TimePeriod timePeriod = TimePeriod.MONTH;
        List<String> periodTimes = new ArrayList<>();
        periodTimes.add("2");
        periodTimes.add("4");
        LocalTime localTime = LocalTime.parse("12:23", DateTimeFormatter.ofPattern("HH:mm"));
        String expression = CronUtils.createCronExpression(timePeriod, periodTimes, localTime);
        System.out.println("expression:" + expression);
        Assert.assertTrue("expression error:" + expression, CronExpression.isValidExpression(expression));
    }

    @Test
    public void test2(){
        String authorityUrl = "http://127.0.0.1/abd";
        String clientId = "dfsfsd";
        String callBackUrl = "http://1.2.3.4:9922";
        UrlBuilder rb = UrlBuilder.of(authorityUrl).addQuery("CLIENT_ID",clientId)
                .addQuery("response_type","code")
                .addQuery("redirect_uri",callBackUrl)
                .addQuery("state", RandomStringUtils.randomAlphabetic(6).toLowerCase());
        System.out.println(rb.build());
        System.out.println(rb.getQueryStr());
    }

    @Test
    public void test3(){
        long current = System.currentTimeMillis();
        int expiresIn = 659000;
        long expireTs = current + expiresIn * 1000;
        System.out.println(expireTs + ":" + current);
    }
}
