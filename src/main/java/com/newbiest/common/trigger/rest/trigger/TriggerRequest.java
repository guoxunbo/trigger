package com.newbiest.common.trigger.rest.trigger;

import com.newbiest.msg.Request;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class TriggerRequest extends Request {

	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE_NAME = "TriggerManage";

	public static final String ACTION_STOP = "Stop";

	private TriggerRequestBody body;

}
