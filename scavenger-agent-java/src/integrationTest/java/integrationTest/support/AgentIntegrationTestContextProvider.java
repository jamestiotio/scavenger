package integrationTest.support;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public class AgentIntegrationTestContextProvider implements TestTemplateInvocationContextProvider {
    private static final String scavengerAgentPath = System.getProperty("integrationTest.scavengerAgent");
    private static final String classpath = System.getProperty("integrationTest.classpath");
    private static final String javaPaths = System.getProperty("integrationTest.javaPaths");

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        assertNotNull(scavengerAgentPath, "This test must be started from Gradle");
        assertNotNull(classpath, "This test must be started from Gradle");
        assertNotNull(javaPaths, "This test must be started from Gradle");
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return Arrays.stream(javaPaths.split(",")).map(this::invocationContext);
    }

    private TestTemplateInvocationContext invocationContext(String javaPathString) {
        String[] split = javaPathString.split(":");
        String javaVersion = split[0];
        String javaPath = split[1];

        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return "Java " + javaVersion;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return Collections.singletonList(new AgentRunnerParameterResolver(javaPath));
            }
        };
    }

    private static class AgentRunnerParameterResolver implements ParameterResolver {
        private final String javaPath;

        private AgentRunnerParameterResolver(String javaPath) {
            this.javaPath = javaPath;
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameterContext.getParameter().getType()
                .equals(AgentRunner.class);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return new AgentRunner(javaPath, classpath, scavengerAgentPath);
        }
    }
}
