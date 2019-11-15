package com.newbiest.common.trigger.rest.trigger;

import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.msg.ResponseBody;
import lombok.Data;

@Data
public class TriggerResponseBody extends ResponseBody {

	private static final long serialVersionUID = 1L;

	private TriggerInstance triggerInstance;

}
