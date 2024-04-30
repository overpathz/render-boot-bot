package com.example.rendertestbot.notes.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        visible = true,
        property = "qualifier"
)
@JsonSubTypes(
        @JsonSubTypes.Type(value = UpdateNote.class, name = "UpdateNote")
)
public abstract class BaseWsRequest {
    protected String wsSessionId;
    protected Long userIdentifier;
    protected String qualifier;
    protected String correlationId;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                '{' +
                "correlationId='" + correlationId + '\'' +
                '}';
    }
}
