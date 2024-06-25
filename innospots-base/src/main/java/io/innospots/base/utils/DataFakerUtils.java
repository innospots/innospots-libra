package io.innospots.base.utils;

import cn.hutool.core.util.RandomUtil;
import io.innospots.base.utils.time.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import net.datafaker.providers.base.AbstractProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/22
 */
@Slf4j
public class DataFakerUtils {

    private Faker faker;


    private DataFakerUtils(Locale locale) {
        this.faker = new Faker(locale);
    }

    public static DataFakerUtils build(Locale locale) {
        return new DataFakerUtils(locale);
    }

    public static DataFakerUtils build() {
        return new DataFakerUtils(Locale.getDefault());
    }

    public <E> E gen(E... items) {
        return faker.options().option(items);
    }

    public String gen(String... items) {
        return faker.options().option(items);
    }

    public <E> E gen(List<E> items) {
        return items.get(faker.random().nextInt(items.size()));
    }

    public String genFuture(String format, TimeUnit timeUnit, int atMost) {
        return faker.date().future(atMost, timeUnit, format);
    }

    public String genPast(String format, TimeUnit timeUnit, int atMost) {
        return faker.date().past(atMost, timeUnit, format);
    }

    public String birthday(int minAge, int maxAge, String format) {
        return faker.date().birthday(minAge, maxAge, format);
    }

    public String genHex(int length) {
        return faker.random().hex(length);
    }

    public String genRegex(String regex) {
        return faker.regexify(regex);
    }

    /**
     * test????test
     *
     * @param letter
     * @return
     */
    public String genLetter(String letter) {
        return faker.letterify(letter);
    }

    /**
     * #test#
     *
     * @param number
     * @return
     */
    public String genNumerify(String number) {
        return faker.numerify(number);
    }

    public String genNumbers(int length) {
        return RandomUtil.randomNumbers(length);
    }

    public String genString(int length) {
        return RandomUtil.randomString(length);
    }

    public Map<String,String> gen(){
        return genObject(faker.name());
    }

    public Map<String,Object> sample(){
        Map<String,Object> sample = new LinkedHashMap<>();
        sample.put("hex",faker.random().hex(16));
        sample.put("double_value",faker.random().nextDouble(10,500));
        sample.put("int_value",faker.random().nextInt(10,200));
        sample.put("option",this.gen("11","22","33","ab","cd"));
        sample.put("birthday",faker.date().birthday(18,80));
        sample.put("future_time",this.faker.date().future(90,TimeUnit.MINUTES,new Date(),DateTimeUtils.DEFAULT_TIME_PATTERN));
        sample.put("future_date",this.faker.date().future(50,TimeUnit.DAYS,new Date()));
        sample.put("past_date",this.faker.date().past(10,TimeUnit.DAYS,new Date(), DateTimeUtils.DEFAULT_DATE_PATTERN));
        sample.put("address",this.faker.address().fullAddress());
        sample.put("country",this.faker.address().country());
        sample.put("city",this.faker.address().city());
        sample.put("full_name",this.faker.name().fullName());
        sample.put("user_name",this.faker.name().username());
        sample.put("watch",this.faker.brand().watch());
        sample.put("pesel_number",this.faker.idNumber().peselNumber());
        sample.put("en_ssn",this.faker.idNumber().validEnZaSsn());
        sample.put("cn_ssn",this.faker.idNumber().validZhCNSsn());
        sample.put("program_lang",this.faker.programmingLanguage().name());
        return sample;
    }

    public Faker faker(){
        return this.faker;
    }

    public Map<String, String> genAddress() {
        return genObject(faker.address());
    }

    public String gen(String expression){
        return faker.expression(expression);
    }

    public Map<String, String> genObject(AbstractProvider provider) {
        Map<String, String> map = new HashMap<>();
        Set<String> methods = Arrays.stream(Object.class.getMethods())
                .map(Method::getName).collect(Collectors.toSet());
        for (Method method : provider.getClass().getMethods()) {
            if(methods.contains(method.getName())){
                continue;
            }
            if (method.getName().startsWith("get") || method.getParameterCount() >= 1) {
                continue;
            }
            try {
                method.setAccessible(true);
                Class<?> returnClass = method.getReturnType();
                if (String.class.equals(returnClass)) {
                    map.put(method.getName(), String.valueOf(method.invoke(provider)));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage());
            }
        }//end for
        return map;
    }

}
