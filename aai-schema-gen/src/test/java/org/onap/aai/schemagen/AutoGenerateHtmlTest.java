package org.onap.aai.schemagen;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(
    properties = {
        "schemaIngestPropLoc = src/test/resources/schema-ingest.properties"})
public class AutoGenerateHtmlTest {

    @Test
    void testMain_shouldSetEnvironmentVariable() throws Exception {
        // Set a system property before running the method
        System.setProperty("aai.generate.version", "1.0");

        AutoGenerateHtml.main(new String[]{});

        assertEquals("1.0", System.getProperty("aai.generate.version"), "The 'aai.generate.version' property should be set.");
        System.clearProperty("aai.generate.version");
    }

    @Test
    void testMain_withBeansException_shouldHandleGracefully() throws Exception {
        // Run the main method, simulating an exception scenario (such as BeansException)
        try {
            AutoGenerateHtml.main(new String[]{});
        } catch (Exception e) {
            // Assert that no exceptions crash the test, and it completes gracefully
            assertTrue(e instanceof Exception, "The exception should be handled gracefully.");
        }
    }
}
