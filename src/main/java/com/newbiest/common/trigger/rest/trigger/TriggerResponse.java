package com.newbiest.common.trigger.rest.trigger;

import com.newbiest.base.msg.Response;
import lombok.Data;

/**
 * Created by guoxunbo on 2017/9/29.
 */
@Data
public class TriggerResponse extends Response {
	
	private static final long serialVersionUID = 1L;
	
	private TriggerResponseBody body;
	
}
