package com.newbiest.common.trigger.rest.trigger;

import com.newbiest.common.trigger.model.TriggerInstance;
import lombok.Data;

import java.io.Serializable;

@Data
public class TriggerResponseBody implements Serializable {

	private static final long serialVersionUID = 1L;

	private TriggerInstance triggerInstance;

}
