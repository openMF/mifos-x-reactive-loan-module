package org.mifos.loanrisk.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServiceStatusTest {

    @Test
    @DisplayName("fromInt returns the matching enum constant for every defined value")
    void fromIntReturnsCorrectEnumForAllValues() {
        for (ServiceStatus status : ServiceStatus.values()) {
            assertSame(status, ServiceStatus.fromInt(status.getValue()), () -> "value " + status.getValue() + " should map to " + status);
        }
    }

    @Test
    @DisplayName("fromInt throws IllegalArgumentException for undefined value")
    void fromIntThrowsForUndefinedValue() {
        assertThrows(IllegalArgumentException.class, () -> ServiceStatus.fromInt(-1));
        assertThrows(IllegalArgumentException.class, () -> ServiceStatus.fromInt(0));
        assertThrows(IllegalArgumentException.class, () -> ServiceStatus.fromInt(99));
    }

    @Test
    @DisplayName("label and description are non-blank for all constants")
    void labelAndDescriptionPresent() {
        for (ServiceStatus status : ServiceStatus.values()) {
            assertNotNull(status.getLabel(), status + " label should not be null");
            assertFalse(status.getLabel().isBlank(), status + " label should not be blank");

            assertNotNull(status.getDescription(), status + " description should not be null");
            assertFalse(status.getDescription().isBlank(), status + " description should not be blank");
        }
    }

    @Test
    @DisplayName("toString returns name with underscores replaced by spaces")
    void toStringFormatsNameNicely() {
        assertEquals("PENDING", ServiceStatus.PENDING.toString());
        assertEquals("REQUESTED", ServiceStatus.REQUESTED.toString());
        assertEquals("COMPLETED", ServiceStatus.COMPLETED.toString());
        assertEquals("FAILED", ServiceStatus.FAILED.toString());
        assertEquals("CANCELLED", ServiceStatus.CANCELLED.toString());
    }
}
