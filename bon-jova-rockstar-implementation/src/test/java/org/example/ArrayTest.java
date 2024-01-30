package org.example;

import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.gizmo.TestClassLoader;
import org.example.util.ParseHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import rock.Rockstar;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayTest {

    @BeforeEach
    public void clearState() {
        Variable.clearState();
    }

    @Test
    public void shouldParseVariableNameWithSimpleVariableArray() {
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray("Rock thing");
        Array a = new Array(ctx);
        // Variable names should be normalised to lower case
        String name = "thing";
        assertEquals(name, a.getVariableName());
    }

    @Test
    public void shouldParseVariableNameWithCommonVariableArray() {
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray("Rock my thing");
        Array a = new Array(ctx);
        // Variable names should be normalised to lower case
        String name = "my__thing";
        assertEquals(name, a.getVariableName());
    }

    @Test
    public void shouldParseVariableNameWithProperVariableArray() {
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray("Rock Doctor Feelgood");
        Array a = new Array(ctx);
        // Variable names should be normalised to lower case
        assertEquals("doctor__feelgood", a.getVariableName());
    }

    @Test
    public void shouldCreateAnArrayOnInitialisationWithRock() {
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray("Rock the thing");
        assertEquals(new ArrayList<Object>(), execute(new Array(ctx)));
    }

    @Test
    public void shouldCreateAnArrayOnInitialisationWithPush() {
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray("Push the thing");
        assertEquals(new ArrayList<Object>(), execute(new Array(ctx)));
    }

    @Test
    public void shouldPopulateArrayOnInitialisationWithSingleElement() {
        String program = """
                Rock 4 into arr
                """;
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray(program);
        Object[] contents = {4d};
        assertEquals(Arrays.asList(contents), execute(new Array(ctx)));

    }

    @Test
    public void shouldPopulateArrayOnInitialisationWithList() {
        String program = """
                Rock 1, 2, 3, 4, 8 into arr
                """;
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray(program);
        Object[] contents = {1d, 2d, 3d, 4d, 8d};
        assertEquals(Arrays.asList(contents), execute(new Array(ctx)));
    }

    @Test
    public void shouldPopulateArrayOnInitialisationWithWith() {
        String program = """
                Rock arr with 5
                """;
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray(program);
        Object[] contents = {5d};
        assertEquals(Arrays.asList(contents), execute(new Array(ctx)));
    }

    @Test
    public void shouldPopulateArrayOnInitialisationWithWithList() {
        String program = """
                Rock arr with 1, 2, 3, 4, 8
                """;
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray(program);
        Object[] contents = {1d, 2d, 3d, 4d, 8d};
        assertEquals(Arrays.asList(contents), execute(new Array(ctx)));
    }

    @Test
    public void shouldPopulateArrayOnInitialisationWithSingleExpression() {
        String program = """
                Rock 4 + 5 into me
                """;
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray(program);
        Object[] contents = {9d};
        assertEquals(Arrays.asList(contents), execute(new Array(ctx)));
    }

    @Test
    public void shouldPopulateArrayOnInitialisationAtAIndexNextToTheEnd() {
        String program = """
                Let arr at 0 be 2
                """;
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray(program);
        Object[] contents = {2d};
        assertEquals(Arrays.asList(contents), execute(new Array(ctx)));
    }

    @Test
    public void shouldPopulateArrayOnInitialisationAtALargeIndex() {
        String program = """
                Let arr at 5 be 2
                """;
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray(program);
        Object[] contents = {null, null, null, null, null, 2d};
        assertEquals(Arrays.asList(contents), execute(new Array(ctx)));
    }

    @Test
    public void shouldAcceptNonNumericKeys() {
        String program = """
                Let arr at "hello" be 2
                """;
        Rockstar.ArrayStmtContext ctx = new ParseHelper().getArray(program);
        Map execute = (Map) execute(new Array(ctx));
        assertEquals(2d, execute.get("hello"));
    }

    // We can't test reading arrays because they're multi-line executions

    private Object execute(Array a) {
        TestClassLoader cl = new TestClassLoader(this.getClass().getClassLoader().getParent());

        // The auto-close on this triggers the write
        String className = "com.ArrayTest";
        String methodName = "method";
        try (ClassCreator creator = ClassCreator.builder()
                .classOutput(cl)
                .className(className)
                .build()) {

            MethodCreator method = creator.getMethodCreator(methodName, Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle rh = a.toCode(method, creator);
            method.returnValue(rh);
        }

        try {
            Class<?> clazz = cl.loadClass(className);
            return clazz.getMethod(methodName)
                    .invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Test error: " + e);
        }
    }

}
