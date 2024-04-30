package com.example.rendertestbot.notes.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UpdateNote extends BaseWsRequest implements Serializable {
    private Long noteId;
    private String noteText;
}
