package com.newbiest.common.trigger;

import com.newbiest.base.service.BaseService;
import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.common.trigger.service.TriggerService;
import com.newbiest.security.service.SecurityService;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by guoxunbo on 2019-11-11 14:54
 */
@Data
public class TriggerContext implements Serializable {

    private BaseService baseService;

    private SecurityService securityService;

    private TriggerService triggerService;

    private TriggerInstance triggerInstance;

}
