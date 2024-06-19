package io.innospots.workflow.runtime.flow.node;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/12/15
 */
public class FakeDataTest {

    @Test
    void test1() {
        Faker faker = new Faker(new Locale("zh-CN"));
        System.out.println(faker.address().fullAddress());
        System.out.println(faker.address().country());
        System.out.println(faker.address().cityName());
        System.out.println(faker.address().state());
        System.out.println(faker.animal().scientificName());
        System.out.println(faker.book().author());
        System.out.println(faker.phoneNumber().cellPhone());
        System.out.println(faker.device().manufacturer());
        System.out.println(faker.gender().binaryTypes());
        System.out.println(faker.gender().shortBinaryTypes());
        System.out.println(faker.camera().brand());
        System.out.println(faker.commerce().productName());
        System.out.println(faker.commerce().vendor());
        System.out.println(faker.commerce().price());
        System.out.println(faker.address().country());
        System.out.println(faker.name().fullName());
        System.out.println(faker.name().lastName());
        System.out.println(faker.regexify("[a-z]{4,10}"));
        System.out.println(faker.expression("#{color.name}"));
        System.out.println(faker.expression("#{company.suffix}"));
        System.out.println(faker.job().title());
        System.out.println(faker.job().field());
        System.out.println(faker.options().option("dd", "bb", "dd", "男", "女"));
        System.out.println(faker.company().name());
        System.out.println(faker.company().industry());
        System.out.println(faker.company().buzzword());
        System.out.println(faker.date().birthday("yyyy-MM-dd"));
        System.out.println(faker.idNumber().validEnZaSsn());
        System.out.println(faker.construction().trades());

        faker.idNumber().peselNumber();
    }

    @Test
    void test2() {
        Faker faker = new Faker(new Locale("zh-CN"));
        System.out.println(faker.expression("#{IdNumber.valid}"));
        System.out.println(faker.expression("#{IdNumber.pesel_number}"));
        System.out.println(faker.expression("#{IdNumber.peselNumber}"));
        System.out.println(faker.expression("#{IdNumber.validZhCNSsn}"));
        System.out.println(faker.expression("#{IdNumber.invalid}"));
        System.out.println(faker.expression("#{IdNumber.ssnValid}"));
        System.out.println(faker.expression("#{IdNumber.validSvSeSsn}"));
        System.out.println(faker.expression("#{IdNumber.validEnZaSsn}"));
        System.out.println(faker.expression("#{IdNumber.singaporeanFin}"));
        System.out.println(faker.expression("#{IdNumber.validEsMXSsn}"));
        System.out.println(faker.expression("#{IdNumber.validPtNif}"));
        System.out.println(faker.expression("#{IdNumber.invalidPtNif}"));
        System.out.println(faker.expression("#{IdNumber.singaporeanUin}"));
        System.out.println(faker.expression("#{Name.name}"));
        System.out.println(faker.expression("#{name.full_name}"));
        System.out.println(faker.expression("#{Name.first_name}"));
        System.out.println(faker.expression("#{Name.last_name}"));
        System.out.println(faker.expression("#{Name.prefix}"));
        System.out.println(faker.expression("#{Name.suffix}"));
        System.out.println(faker.expression("#{Name.username}"));
        System.out.println(faker.expression("#{Name.name_with_middle}"));
        System.out.println(faker.expression("#{Name.title}"));
        System.out.println(faker.expression("#{Name.title.level}"));
        System.out.println(faker.expression("#{Name.title.job}"));

        System.out.println(faker.expression("#{Address.streetName}"));
        System.out.println(faker.expression("#{Address.streetName}"));
    }

    @Test
    void test4() {
        Faker faker = new Faker(new Locale("zh-CN"));
        faker.random().hex();
        System.out.println(faker.expression("#{Random.hex}"));
        System.out.println(faker.expression("#{Random.hex '32'}"));
        System.out.println(faker.expression("#{Random.hex '64','false'}"));
        System.out.println(faker.expression("#{Random.nextInt}"));
        System.out.println(faker.expression("#{Random.nextInt '100'}"));
        System.out.println(faker.expression("#{Random.nextInt '100','300'}"));
        System.out.println(faker.expression("#{Random.nextFloat}"));
        System.out.println(faker.expression("#{Random.nextLong}"));
        System.out.println(faker.expression("#{Random.nextLong '50'}"));
        System.out.println(faker.expression("#{Random.nextDouble}"));
        System.out.println(faker.expression("#{Random.nextDouble '10','20'}"));
        System.out.println(faker.expression("#{Random.nextBoolean}"));
    }

    @Test
    void test6() {
        Faker faker = new Faker(new Locale("zh-CN"));
        faker.date().birthday();
        System.out.println(faker.expression("#{Date.birthday}"));
        System.out.println(faker.expression("#{Date.birthday 'yyyy-MM-dd'}"));
        System.out.println(faker.expression("#{Date.birthday 'hh:mm:ss'}"));
        System.out.println(faker.expression("#{Date.birthday '29','50','yyyy-MM-dd'}"));
        System.out.println(faker.expression("#{Date.past '15','SECONDS','dd/MM/yyyy hh:mm:ss'}"));
        System.out.println(faker.expression("#{Date.past '15','MINUTES','dd/MM/yyyy hh:mm:ss'}"));
        System.out.println(faker.expression("#{Date.future '15','MINUTES','dd/MM/yyyy hh:mm:ss'}"));
        System.out.println(faker.expression("#{Date.future '15','HOURS','dd/MM/yyyy hh:mm:ss'}"));
        System.out.println(faker.expression("#{Date.future '15','DAYS','dd/MM/yyyy hh:mm:ss'}"));
//        System.out.println(faker.expression("#{date.duration '15','MINUTE'}"));
//        System.out.println(faker.expression("#{date.duration '5','10','SECONDS'}"));
    }

    @Test
    void test8() {
        Faker faker = new Faker(new Locale("zh-CN"));
        faker.relationships();
        System.out.println(faker.expression("#{Relationship.parent}"));
        System.out.println(faker.expression("#{Relationship.familial.direct}"));
        System.out.println(faker.expression("#{Relationship.familial.extended}"));
        System.out.println(faker.expression("#{Relationship.in_law}"));
        System.out.println(faker.expression("#{Relationship.spouse}"));
        System.out.println(faker.expression("#{Relationship.sibling}"));
    }

    @Test
    void test3() throws InvocationTargetException, IllegalAccessException {

        List<String> el2 = new ArrayList<>();

        el2.add("#{Random.hex}");
        el2.add("#{Random.hex '32'}");
        el2.add("#{Random.hex '64','false'}");
        el2.add("#{Random.nextInt}");
        el2.add("#{Random.nextInt '100'}");
        el2.add("#{Random.nextInt '100','300'}");
        el2.add("#{Random.nextFloat}");
        el2.add("#{Random.nextLong}");
        el2.add("#{Random.nextLong '50'}");
        el2.add("#{Random.nextDouble}");
        el2.add("#{Random.nextDouble '10','20'}");
        el2.add("#{Random.nextBoolean}");
        el2.add("#{Date.birthday}");
        el2.add("#{Date.birthday 'yyyy-MM-dd'}");
        el2.add("#{Date.birthday 'hh:mm:ss'}");
        el2.add("#{Date.birthday '29','50','yyyy-MM-dd'}");
        el2.add("#{Date.past '15','SECONDS','dd/MM/yyyy hh:mm:ss'}");
        el2.add("#{Date.past '15','MINUTES','dd/MM/yyyy hh:mm:ss'}");
        el2.add("#{Date.future '15','MINUTES','dd/MM/yyyy hh:mm:ss'}");
        el2.add("#{Date.future '15','HOURS','dd/MM/yyyy hh:mm:ss'}");
        el2.add("#{Date.future '15','DAYS','dd/MM/yyyy hh:mm:ss'}");
        el2.add("#{Relationship.parent}");
        el2.add("#{Relationship.familial.direct}");
        el2.add("#{Relationship.familial.extended}");
        el2.add("#{Relationship.in_law}");
        el2.add("#{Relationship.spouse}");
        el2.add("#{Relationship.sibling}");


        Faker faker = new Faker(new Locale("zh-CN"));
        Set<String> skips = new HashSet<>();
        skips.add("wait");
        skips.add("csv");
        skips.add("toString");
        skips.add("hashCode");
        skips.add("getClass");
        skips.add("notify");
        skips.add("notifyAll");
        skips.add("getFaker");
        skips.add("getContext");
        skips.add("stream");
        skips.add("collection");
        skips.add("instance");
        Set<String> skipClass = new HashSet<>();
        skipClass.add("RandomService");
        skipClass.add("DateAndTime");
        skipClass.add("Relationship");
        skipClass.add("Locality");
        skipClass.add("bodyBytes");
        skipClass.add("bodyString");
        Method[] methods = Faker.class.getMethods();
        List<String> exps = new ArrayList<>();
        for (Method method : methods) {
            String f = method.getName();
            f += ":";
            f += method.getParameterCount();
            if (method.getParameterCount() == 0 && !skips.contains(method.getName())) {
                Object v = method.invoke(faker);
                f += ": " + v.getClass().getName();
                System.out.println(f);
                if (skipClass.contains(v.getClass().getSimpleName())) {
                    System.out.println("---------");
                    continue;
                }

                Method[] vMethods = v.getClass().getMethods();
                for (Method vMethod : vMethods) {
                    if (vMethod.getParameterCount() == 0 && !skips.contains(vMethod.getName())) {
                        String exp = "#{" + v.getClass().getSimpleName();
                        exp += ".";
                        exp += vMethod.getName();
                        exp += "}";
                        try {
                            //Object out = vMethod.invoke(v);
                            //System.out.println(vMethod.getName() + "=" + out);
                            String vv = faker.expression(exp);
//                            System.out.print(exp);
//                            System.out.println(" : "+vv);
                            exps.add(v.getClass().getSimpleName() + "|" + exp + "|" + vv);
                        } catch (Exception e) {
                            System.err.println(e.getMessage() + ", method: " + vMethod.getName());
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("---------");
            }
        }
        for (String s : el2) {
            String out = "";
            if (s.contains("Random")) {
                out = "Random";
            } else if (s.contains("Date")) {
                out = "Date";
            } else if (s.contains("Relationship")) {
                out = "Relationship";
            }
            out += "|" + s + "|" + faker.expression(s);
            exps.add(out);
        }

        System.out.println("==========");

        List<String> ss = new ArrayList<>();
        ss.add("#{letterify 'test????test'}");
        ss.add("#{letterify 'test????test','true'}");
        ss.add("#{numerify '#test#'}");
        ss.add("#{numerify '####'}");
        ss.add("#{bothify '?#?#?#?#'}");
        ss.add("#{bothify '?#?#?#?#', true}");
        ss.add("#{templatify 'test','t','q','@'}");
        ss.add("#{templatify 'test','t','q','@','$','*'}");
        ss.add("#{examplify 'ABC'}");
        ss.add("#{examplify 'test'}");
        ss.add("#{regexify '(a|b){2,3}'}");
        ss.add("#{options.option 'ABC','2','5','$'}");
        ss.add("#{options.option '23','2','5','$','%','*'}");
        for (String s : ss) {
            String ff = "";
            if (s.contains("letterify")) {
                ff = "Letterify";
            } else if (s.contains("numerify")) {
                ff = "Numerify";
            } else if (s.contains("bothify")) {
                ff = "Bothify";
            } else if (s.contains("templatify")) {
                ff = "Templatify";
            } else if (s.contains("examplify")) {
                ff = "Examplify";
            } else if (s.contains("regexify")) {
                ff = "Regexify";
            } else if (s.contains("options")) {
                ff = "Options";
            }
            System.out.println(ff + "|" + s + "|" + faker.expression(s));
        }

        System.out.println("****************");

        exps.sort(Comparator.naturalOrder());
        for (String exp : exps) {
            System.out.println(exp);
        }
    }
}
