package crux;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

final class CompilerStageTests {
    /**
     * TODO: Change variable TEST_TO_RUN to run tests for other stages.
     * For example, to run tests for all stages:
     * private final String[] TEST_TO_RUN = {"stage1", "stage2", "stage3", "stage4", "stage5"};
     * */
    private final String[] TEST_TO_RUN = {"stage5"};
    private boolean skipStage(String stageName) {
        return List.of(TEST_TO_RUN).stream().noneMatch(s -> s.toLowerCase().equals(stageName));
    }

    @TestFactory
    Stream<DynamicTest> parseTree() throws IOException {
        if(skipStage("stage1")){
            return Stream.empty();
        }
        var tests = getTests("parse-tree");
        return tests.stream().map(test -> dynamicTest(test.in, () -> {
            var loader = getClass().getClassLoader();
            var in = loader.getResourceAsStream(test.in);

            var outStream = new ByteArrayOutputStream();
            var errStream = new ByteArrayOutputStream();
            var driver = new Driver(new PrintStream(outStream), new PrintStream(errStream));

            driver.setInputStream(in);
            driver.enablePrintParseTree();

            var status = driver.run();
            var actualOutput = status == State.Finished ? outStream.toString() : errStream.toString();

            var expectedOutput = readResourceToString(test.out);
            Assertions.assertEquals(expectedOutput, actualOutput, String.format("Parse tree for program %s differs from expected output.", test.in));
        }));
    }

    @TestFactory
    Stream<DynamicTest> ast() throws IOException {
        if(skipStage("stage2")){
            return Stream.empty();
        }
        var tests = getTests("ast");
        return tests.stream().map(test -> dynamicTest(test.in, () -> {
            var loader = getClass().getClassLoader();
            var in = loader.getResourceAsStream(test.in);

            var outStream = new ByteArrayOutputStream();
            var errStream = new ByteArrayOutputStream();
            var driver = new Driver(new PrintStream(outStream), new PrintStream(errStream));

            driver.setInputStream(in);
            driver.enablePrintAst();

            var status = driver.run();
            var actualOutput = status == State.Finished ? outStream.toString() : errStream.toString();

            var expectedOutput = readResourceToString(test.out);
            Assertions.assertEquals(expectedOutput.trim(), actualOutput.trim(), String.format("AST for program %s differs from expected output.", test.in));
        }));
    }

    @TestFactory
    Stream<DynamicTest> typeCheck() throws IOException {
        if(skipStage("stage3")){
            return Stream.empty();
        }
        var tests = getTests("type-check");
        return tests.stream().map(test -> dynamicTest(test.in, () -> {
            var loader = getClass().getClassLoader();
            var in = loader.getResourceAsStream(test.in);

            var outStream = new ByteArrayOutputStream();
            var outPrintStream = new PrintStream(outStream);
            var driver = new Driver(outPrintStream, outPrintStream);

            driver.setInputStream(in);
            driver.enableTypeCheck();
            driver.run();

            var actualOutput = outStream.toString();
            var expectedOutput = readResourceToString(test.out);
            Assertions.assertEquals(expectedOutput.trim(), actualOutput.trim(), String.format("Type check for program %s differs from expected output.", test.in));
        }));
    }

    @TestFactory
    Stream<DynamicTest> emulateIR() throws IOException {
        if(skipStage("stage4")){
            return Stream.empty();
        }
        var tests = getTests("ir");
        return tests.stream().map(test -> dynamicTest(test.in, () -> {
            var loader = getClass().getClassLoader();
            var in = loader.getResourceAsStream(test.in);
            var input = loader.getResourceAsStream(test.input);

            var outStream = new ByteArrayOutputStream();
            var outPrintStream = new PrintStream(outStream);
            var driver = new Driver(outPrintStream, outPrintStream);

            driver.setInputStream(in);
            driver.enableEmulator();
            driver.setEmulatorInput(input);
            driver.run();

            var actualOutput = outStream.toString();
            var expectedOutput = readResourceToString(test.out);
            Assertions.assertEquals(expectedOutput.trim(), actualOutput.trim());
        }));
    }

    @TestFactory
    Stream<DynamicTest> codegen() throws IOException {
        if(skipStage("stage5")){
            return Stream.empty();
        }
        var tests = getTests("codegen");
        Runtime runtime = Runtime.getRuntime();
        
        return tests.stream().map(test -> dynamicTest(test.in, () -> {
            var loader = getClass().getClassLoader();
            var in = loader.getResourceAsStream(test.in);
            var input = loader.getResourceAsStream(test.input);

            var driver = new Driver();
            
            driver.setInputStream(in);
            driver.run();
            Process build = runtime.exec("gcc a.s src/runtime/runtime.c -o autotest.bin");
            try {
                if (build.waitFor() != 0) {
                    throw new Error("Assembling and linking failed");
                }
            } catch (Exception e) {
                throw new Error("Assembling and linking failed");
            }
            Process run = runtime.exec("./autotest.bin");
            OutputStream runinput = run.getOutputStream();

            int val;
            while((val = input.read()) != -1)
                runinput.write(val);
            runinput.close();

            InputStream inputStream = run.getInputStream();
            StringBuffer sb = new StringBuffer();
            while((val = inputStream.read()) != -1)
                sb.append((char)val);

            var actualOutput = sb.toString();
            var expectedOutput = readResourceToString(test.out);
            Assertions.assertEquals(expectedOutput.trim(), actualOutput.trim());
        }));
    }

    private List<InOut> getTests(String stageName) throws IOException {
        var loader = getClass().getClassLoader();
        var folder = String.format("crux/stages/%s", stageName);
        try (var programs = loader.getResourceAsStream(folder);
             BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(programs)))) {

            return br.lines()
                    .filter(resourceName -> resourceName.endsWith(".crx"))
                    .map(resourceName -> {
                        var testName = resourceName.substring(0, resourceName.length() - 4);
                        var input = String.format("%s/%s.in", folder, testName);
                        var out = String.format("%s/%s.out", folder, testName);
                        return new InOut(folder + "/" + resourceName, input, out);
                    }).collect(Collectors.toList());
        }
    }

    private String readResourceToString(String resourceName) throws IOException {
        var loader = getClass().getClassLoader();
        try (var inputStream = Objects.requireNonNull(loader.getResourceAsStream(resourceName))) {
            var result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8);
        }
    }

    private static final class InOut {
        final String in;
        final String input;
        final String out;

        private InOut(String in, String input, String out) {
            this.in = in;
            this.input = input;
            this.out = out;
        }
    }
}
