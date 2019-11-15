package com.newbiest.common.trigger.service;

import com.newbiest.base.exception.ClientException;
import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.common.trigger.model.TriggerInstanceHistory;
import com.newbiest.common.trigger.model.TriggerLock;

import java.util.Date;
import java.util.List;

/**
 * Created by guoxunbo on 2019-11-11 14:56
 */
public interface TriggerService {

    TriggerInstance stopTrigger(TriggerInstance triggerInstance) throws ClientException;
    TriggerInstance scheduleTriggerInstance(TriggerInstance triggerInstance, Date lastExecutionTime, Date nextExecutionTime) throws ClientException;
    List<TriggerInstance> getTriggerByState(String state) throws ClientException;
    List<TriggerInstance> getTriggerInstance() throws ClientException;
    TriggerInstance getTriggerInstanceByName(String name) throws ClientException;

    void saveTriggerHistory(TriggerInstanceHistory triggerInstanceHistory) throws ClientException;

    List<TriggerLock> getAllTriggerLock() throws ClientException;
    boolean lockTrigger(String triggerName) throws ClientException;
    void unLockTrigger(String triggerName) throws ClientException;


}
