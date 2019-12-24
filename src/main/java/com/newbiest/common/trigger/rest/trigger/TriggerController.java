package com.newbiest.common.trigger.rest.trigger;

import com.newbiest.base.exception.ClientException;
import com.newbiest.base.msg.Request;
import com.newbiest.base.rest.AbstractRestController;
import com.newbiest.base.service.BaseService;
import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.common.trigger.service.TriggerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by guoxunbo on 2018/7/12.
 */
@RestController
@RequestMapping("/trigger")
@Slf4j
@Api(value="/trigger", tags="Trigger", description = "定时器相关")
public class TriggerController extends AbstractRestController {

    @Autowired
    TriggerService triggerService;

    @Autowired
    BaseService baseService;

    @ApiOperation(value = "对Trigger做操作", notes = "更新以及停止定时器，暂不支持新增定时器")
    @ApiImplicitParam(name="request", value="request", required = true, dataType = "TriggerRequest")
    @RequestMapping(value = "/triggerManager", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public TriggerResponse execute(@RequestBody TriggerRequest request) throws Exception {
        TriggerResponse response = new TriggerResponse();
        response.getHeader().setTransactionId(request.getHeader().getTransactionId());
        TriggerResponseBody responseBody = new TriggerResponseBody();

        TriggerRequestBody requestBody = request.getBody();
        String actionType = requestBody.getActionType();
        TriggerInstance triggerInstance = requestBody.getTriggerInstance();

        if (TriggerRequest.ACTION_UPDATE.equals(actionType)) {
            triggerInstance.setState(TriggerInstance.STATE_UPDATE);
            triggerInstance = (TriggerInstance) baseService.saveEntity(triggerInstance);
        } else if (TriggerRequest.ACTION_STOP.equals(actionType)) {
            triggerInstance = triggerService.stopTrigger(triggerInstance);
        } else {
            throw new ClientException(Request.NON_SUPPORT_ACTION_TYPE + requestBody.getActionType());
        }
        responseBody.setTriggerInstance(triggerInstance);
        response.setBody(responseBody);
        return response;
    }

}
