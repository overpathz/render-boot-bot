package com.example.rendertestbot.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UpdateNoteWsRequest extends BaseWsRequest implements Serializable {
    private Long noteId;
    private String noteText;
}
