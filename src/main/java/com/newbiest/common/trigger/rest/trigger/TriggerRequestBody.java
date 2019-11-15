package com.newbiest.common.trigger.rest.trigger;

import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.msg.RequestBody;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by guoxunbo on 2017/9/29.
 */
@Data
@ApiModel("具体请求操作信息")
public class TriggerRequestBody extends RequestBody {
	
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "操作类型", example = "Update/Stop等")
	private String actionType;

	@ApiModelProperty(value = "操作的物料对象")
	private TriggerInstance triggerInstance;

}
