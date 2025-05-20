package template.cqrs.domain.model.alert.value_objects;

import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Value
public class AlertDetails {
    Map<String, Object> properties;

    public AlertDetails(Map<String, Object> properties) {
        // Use a new HashMap to ensure it's a type XStream handles well by default
        // and to make a defensive copy.
        this.properties = properties != null ? new HashMap<>(properties) : Collections.emptyMap();
    }
}