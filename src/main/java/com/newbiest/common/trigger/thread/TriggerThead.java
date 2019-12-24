package com.newbiest.common.trigger.thread;

import com.google.common.base.Stopwatch;
import com.newbiest.base.threadlocal.SessionContext;
import com.newbiest.base.threadlocal.ThreadLocalContext;
import com.newbiest.base.utils.StringUtils;
import com.newbiest.common.trigger.TriggerContext;
import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.common.trigger.model.TriggerInstanceHistory;
import com.newbiest.common.trigger.model.TriggerResult;
import com.newbiest.security.model.NBOrg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 所有需要定时任务的线程父类
 *  由于分布式集群，故在执行的线程的时候会去锁。
 * Created by guoxunbo on 2019-11-11 14:27
 */
@Data
@AllArgsConstructor
@Slf4j
public abstract class TriggerThead implements Runnable {

    protected TriggerContext triggerContext;

    @Override
    public void run() {
        TriggerInstance triggerInstance = triggerContext.getTriggerInstance();
        generatorSessionContext();
        boolean locked = false;
        try {
            locked = triggerContext.getTriggerService().lockTrigger(triggerInstance.getName());
            if (locked) {
                if (log.isDebugEnabled()) {
                    log.debug(" The Task [ " + triggerContext.getTriggerInstance().getName() + " ] ready to start");
                }
                Stopwatch stopwatch = Stopwatch.createStarted();
                TriggerResult result = execute();
                stopwatch.stop();
                Long executeTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                if (result.isRecordHistory()) {
                    TriggerInstanceHistory triggerInstanceHistory = (TriggerInstanceHistory) triggerContext.getBaseService().buildHistoryBean(triggerInstance, TriggerInstanceHistory.TRANS_TYPE_EXECUTE);
                    triggerInstanceHistory.setExecuteTime(ThreadLocalContext.getSessionContext().getTransTime());
                    triggerInstanceHistory.setResultCode(result.getResultCode());
                    triggerInstanceHistory.setResultText(result.getResultText());
                    if (TriggerResult.RESULT_CODE_SUCCESS.equals(result.getResultCode())) {
                        triggerInstanceHistory.setElapsedTime(executeTime);
                    }
                    triggerContext.getTriggerService().saveTriggerHistory(triggerInstanceHistory);
                }
                if (log.isDebugEnabled()) {
                    log.debug(" The Task [ " + triggerContext.getTriggerInstance().getName() + " ] finished");
                }
            }
        } catch (Exception e){
            log.error(e.getMessage(), e);
        } finally {
            if (locked) {
                triggerContext.getTriggerService().unLockTrigger(triggerInstance.getName());
            }
        }

    }

    public abstract TriggerResult execute();

    public String getName() {
        return triggerContext.getTriggerInstance().getName();
    }

    public void generatorSessionContext() {
        SessionContext sc = new SessionContext();
        sc.setOrgRrn(NBOrg.GLOBAL_ORG_RRN);
        sc.setUsername(StringUtils.SYSTEM_USER);
        sc.setTransRrn(UUID.randomUUID().toString());
        sc.setTransTime(new Date());
        ThreadLocalContext.putSessionContext(sc);
    }
}
