package io.innospots.base.script.jit;

import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

/**
 * @author Smars
 * @date 2021/5/25
 */
public class JavaSourceFileStaticBuilderTest {

    @Test
    public void toSource() {
        String src = null;
        JavaSourceFileStaticBuilder builder = JavaSourceFileStaticBuilder.newBuilder("Ts1", "abc", Paths.get(""));
        ParamField pf = new ParamField();
        pf.setCode("p1");
        pf.setValueType(FieldValueType.INTEGER);
        pf.setName("pf");
//        builder.addMethod(Boolean.class, "boolean b = true;return b;", "callB", pf);
        src = builder.toSource();

        System.out.println(src);
    }
}