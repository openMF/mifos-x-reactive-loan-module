package org.mifos.loanrisk.common;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum ServiceStatus {

    PENDING(1, "Pending", "The service is pending"), REQUESTED(2, "Requested", "The service has been requested"), COMPLETED(3, "Completed",
            "The service has been completed"), FAILED(4, "Failed",
                    "The service has failed"), CANCELLED(5, "Cancelled", "The loan has been cancelled");

    private final int value;
    private final String label;
    private final String description;

    private static final Map<Integer, ServiceStatus> intToEnumMap = new HashMap<>();

    static {
        for (final ServiceStatus status : ServiceStatus.values()) {
            intToEnumMap.put(status.value, status);
        }
    }

    public static ServiceStatus fromInt(final int value) {
        final ServiceStatus status = intToEnumMap.get(value);
        if (status == null) {
            throw new IllegalArgumentException("No ServiceStatus found for value: " + value);
        }
        return status;
    }

    ServiceStatus(final int value, final String label, final String description) {
        this.value = value;
        this.label = label;
        this.description = description;
    }

    @Override
    public String toString() {
        return name().replaceAll("_", " ");
    }

}
