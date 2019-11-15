package com.newbiest.common.trigger;

import com.newbiest.common.trigger.model.TriggerInstance;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Date;

/**
 * 继承cronTrigger用来更新trigger的执行时间和下次执行时间
 * Created by guoxunbo on 2019-11-12 18:42
 */
public class NewbiestTrigger extends CronTrigger {

    private com.newbiest.common.trigger.TriggerContext context;

    public NewbiestTrigger(com.newbiest.common.trigger.TriggerContext context) {
        super(context.getTriggerInstance().getCronExpression());
        this.context = context;
    }

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        // 重新获取对象，防止直接数据库或页面修改了nextExecutionTime不生效。
        TriggerInstance triggerInstance = context.getTriggerInstance();
        Date nextExecutionTime = super.nextExecutionTime(triggerContext);
        // 如果被改动过
        if (!triggerInstance.isScheduling()) {
            nextExecutionTime = new Date();
        } else {
            triggerInstance.setState(TriggerInstance.STATE_SCHEDULING);
        }
        triggerInstance = context.getTriggerService().scheduleTriggerInstance(triggerInstance, triggerContext.lastActualExecutionTime(), nextExecutionTime);
        context.setTriggerInstance(triggerInstance);
        return nextExecutionTime;
    }
}
