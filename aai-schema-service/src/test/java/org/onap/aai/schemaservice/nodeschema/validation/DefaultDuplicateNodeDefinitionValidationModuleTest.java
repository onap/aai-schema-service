/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.schemaservice.nodeschema.validation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.schemaservice.nodeschema.SchemaVersion;
import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultDuplicateNodeDefinitionValidationModuleTest {

    private DefaultDuplicateNodeDefinitionValidationModule validationModule;

    @Mock
    private SchemaVersion mockSchemaVersion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validationModule = new DefaultDuplicateNodeDefinitionValidationModule();
    }

    @Test
    void testFindDuplicates_ShouldHandleEmptyFileList() throws Exception {
        List<String> files = Collections.emptyList();
        when(mockSchemaVersion.toString()).thenReturn("v1");

        String result = validationModule.findDuplicates(files, mockSchemaVersion);

        assertEquals("", result);
    }

    @Test
    void testBuildErrorMsg() throws Exception {
        Multimap<String, String> types = ArrayListMultimap.create();
        types.put("nodeType1", "file1.xml");
        types.put("nodeType1", "file2.xml");
        types.put("nodeType2", "file3.xml");

        when(mockSchemaVersion.toString()).thenReturn("v1");

        Method buildErrorMsgMethod = DefaultDuplicateNodeDefinitionValidationModule.class
            .getDeclaredMethod("buildErrorMsg", Multimap.class, SchemaVersion.class);
        buildErrorMsgMethod.setAccessible(true);

        String result = (String) buildErrorMsgMethod.invoke(validationModule, types, mockSchemaVersion);

        String expected = "Duplicates found in version v1. nodeType1 has definitions in file1.xml file2.xml ";
        assertEquals(expected, result);
    }

    @Test
    void testFindDuplicatesCatchPart() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("Java");
        files.add("abc");

        when(mockSchemaVersion.toString()).thenReturn("v1");

        InputStream inputStreamThatThrowsIOException = mock(InputStream.class);
        when(inputStreamThatThrowsIOException.read()).thenThrow(new IOException("The system cannot find the file specified"));

        DocumentBuilder mockDocBuilder = mock(DocumentBuilder.class);
        when(mockDocBuilder.parse(inputStreamThatThrowsIOException)).thenThrow(new IOException("The system cannot find the file specified"));

        String result = validationModule.findDuplicates(files, mockSchemaVersion);

        assertEquals("Java (No such file or directory)", result);
    }

}
