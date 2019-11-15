package com.newbiest.common.trigger.thread;

import com.newbiest.base.utils.CollectionUtils;
import com.newbiest.base.utils.DateUtils;
import com.newbiest.common.trigger.NewbiestScheduleConfig;
import com.newbiest.common.trigger.TriggerContext;
import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.common.trigger.model.TriggerLock;
import com.newbiest.common.trigger.model.TriggerResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 守护线程 守护线程。每触发10次，记录一次历史。主要做如下事情。
 *  1.检查当前的triggerLock。当达到了最长执行时间就执行unlock
 *  2.检查TriggerInstance是否做了改动，如果有改动则需要重新处理
 * Created by guoxunbo on 2019-11-11 17:19
 */
@Slf4j
public class DaemonThread extends TriggerThead {

    public static final String DAEMON_THREAD_NAME = "Daemon";

    private NewbiestScheduleConfig newbiestScheduleConfig;

    private static final int TRIGGER_RECORD_HISTORY_COUNT = 10;

    private static AtomicInteger triggerCount = new AtomicInteger(TRIGGER_RECORD_HISTORY_COUNT);

    public DaemonThread(TriggerContext triggerContext, NewbiestScheduleConfig newbiestScheduleConfig) {
        super(triggerContext);
        this.newbiestScheduleConfig = newbiestScheduleConfig;
    }

    @Override
    public TriggerResult execute() {
        TriggerResult triggerResult = new TriggerResult();
        try {
            monitorTriggerLock();
            monitorTriggerState();
            triggerCount.getAndIncrement();
            if (triggerCount.get() < TRIGGER_RECORD_HISTORY_COUNT) {
                triggerResult.setRecordHistory(false);
            } else {
                triggerCount.set(triggerCount.get() - TRIGGER_RECORD_HISTORY_COUNT);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            triggerResult.setResultCode(TriggerResult.RESULT_CODE_FAIL);
            triggerResult.setResultText(e.getMessage());
        }
        return triggerResult;
    }

    /**
     * 监控triggerInstance状态。及时停止定时器或者更新定时器的执行频率
     */
    public void monitorTriggerState() {
        List<TriggerInstance> triggerInstanceList = getTriggerContext().getTriggerService().getTriggerByState(TriggerInstance.STATE_UPDATE);
        if (CollectionUtils.isNotEmpty(triggerInstanceList)) {
            for (TriggerInstance triggerInstance : triggerInstanceList) {
                this.newbiestScheduleConfig.restartTrigger(triggerInstance);
            }
        }

        triggerInstanceList = getTriggerContext().getTriggerService().getTriggerByState(TriggerInstance.STATE_STOP);
        if (CollectionUtils.isNotEmpty(triggerInstanceList)) {
            for (TriggerInstance triggerInstance : triggerInstanceList) {
                this.newbiestScheduleConfig.stopTrigger(triggerInstance);
            }
        }
    }

    /**
     * 监控trigger的锁，当达到了这个锁的最长时间的时候做unlock
     *
     */
    private void monitorTriggerLock() {
        List<TriggerLock> triggerLockList = getTriggerContext().getTriggerService().getAllTriggerLock();
        if (CollectionUtils.isNotEmpty(triggerLockList)) {
            for (TriggerLock triggerLock : triggerLockList) {
                if (triggerLock.getEffectiveTime().after(DateUtils.now())) {
                    getTriggerContext().getTriggerService().unLockTrigger(triggerLock.getTriggerName());
                }
            }
        }
    }


}
