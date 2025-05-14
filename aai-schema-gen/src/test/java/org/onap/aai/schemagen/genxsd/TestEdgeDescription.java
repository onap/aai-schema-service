package org.onap.aai.schemagen.genxsd;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.edges.EdgeRule;
import org.onap.aai.edges.enums.AAIDirection;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestEdgeDescription {

    private EdgeRule mockEdgeRule;
    private EdgeDescription edgeDescription;

    @BeforeEach
    public void setUp() {
        mockEdgeRule = mock(EdgeRule.class);

        when(mockEdgeRule.getFrom()).thenReturn("fromNode");
        when(mockEdgeRule.getTo()).thenReturn("toNode");
        when(mockEdgeRule.getDirection()).thenReturn(Direction.valueOf("OUT"));
        when(mockEdgeRule.getContains()).thenReturn("OUT");
        when(mockEdgeRule.getDeleteOtherV()).thenReturn("IN");
        when(mockEdgeRule.getPreventDelete()).thenReturn("false");
        when(mockEdgeRule.getDescription()).thenReturn("Test description");
        when(mockEdgeRule.getLabel()).thenReturn("label");

        // Initialize EdgeDescription with mocked EdgeRule
        edgeDescription = new EdgeDescription(mockEdgeRule);
    }

    @Test
    public void testHasDelTarget() {
        // Test that hasDelTarget returns true when DeleteOtherV is not "NONE"
        assertTrue(edgeDescription.hasDelTarget());

        // Test with different value for DeleteOtherV to test the false case
        when(mockEdgeRule.getDeleteOtherV()).thenReturn("NONE");
        assertFalse(edgeDescription.hasDelTarget());
    }

    @Test
    public void testGetType() {
        // Test the method for expected LineageType
        assertEquals(EdgeDescription.LineageType.PARENT, edgeDescription.getType());
    }

    @Test
    public void testGetDescription() {
        assertEquals("Test description", edgeDescription.getDescription());
    }

    @Test
    public void testGetDirection() {
        assertEquals(AAIDirection.OUT, edgeDescription.getDirection());
    }

    @Test
    public void testGetTo() {
        assertEquals("toNode", edgeDescription.getTo());
    }

    @Test
    public void testGetFrom() {
        assertEquals("fromNode", edgeDescription.getFrom());
    }

    @Test
    public void testGetDeleteOtherV() {
        assertEquals("IN", edgeDescription.getDeleteOtherV());
    }

    @Test
    public void testGetPreventDelete() {
        assertEquals("false", edgeDescription.getPreventDelete());
    }
}
