package io.innospots.script.base.jit;

import io.innospots.base.exception.ScriptException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Smars
 * @date 2021/5/24
 */
public class JavaSourceFileCompilerTest {

    @Test
    public void compile() throws IOException, ScriptException {
        File file = new File("target/clz2");
        System.out.println(file.getAbsolutePath());
        String src = "src/test/java/live/innospot/base/utils/Test_1.java";
        String src2 = "src/test/java/live/innospot/base/utils/Test_2.java";
//        String src2 = "target/clz2/Test_2.java";
        JavaSourceFileCompiler compiler = new JavaSourceFileCompiler(Paths.get("target/clz2"));
        compiler.addSourceFile(new File(src));
        compiler.addSourceFile(new File(src2));
        compiler.compile();
    }

    @Test
    public void test() {
        System.out.println(Paths.get("").toAbsolutePath());

        System.out.println(Paths.get("/temp", "dd", "cc", "ss", "java.jar"));

        String clazz = this.getClass().getName();
        System.out.println(Paths.get("/sss", clazz.split("\\.")));
    }
}