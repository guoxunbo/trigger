package com.newbiest.common.trigger.service.impl;

import com.newbiest.base.constant.EnvConstant;
import com.newbiest.base.exception.ClientException;
import com.newbiest.base.exception.ClientParameterException;
import com.newbiest.base.exception.ExceptionManager;
import com.newbiest.base.model.NBParameter;
import com.newbiest.base.service.BaseService;
import com.newbiest.base.service.FrameworkCacheService;
import com.newbiest.base.threadlocal.ThreadLocalContext;
import com.newbiest.base.utils.CollectionUtils;
import com.newbiest.base.utils.DateUtils;
import com.newbiest.common.trigger.Exceptions;
import com.newbiest.common.trigger.NewbiestScheduleConfig;
import com.newbiest.common.trigger.TriggerConfiguration;
import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.common.trigger.model.TriggerInstanceHistory;
import com.newbiest.common.trigger.model.TriggerLock;
import com.newbiest.common.trigger.repository.TriggerInstanceHistoryRepository;
import com.newbiest.common.trigger.repository.TriggerInstanceRepository;
import com.newbiest.common.trigger.repository.TriggerLockRepository;
import com.newbiest.common.trigger.service.TriggerService;
import com.newbiest.security.model.NBOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Created by guoxunbo on 2019-11-11 15:46
 */
@Component
@Transactional
@Slf4j
public class TriggerServiceImpl implements TriggerService {

    @Autowired
    TriggerInstanceRepository triggerInstanceRepository;

    @Autowired
    TriggerInstanceHistoryRepository triggerInstanceHistoryRepository;

    @Autowired
    TriggerLockRepository triggerLockRepository;

    @Autowired
    FrameworkCacheService cacheService;

    @Autowired
    BaseService baseService;

    @Autowired
    TriggerConfiguration configuration;

    @Autowired
    NewbiestScheduleConfig newbiestScheduleConfig;

    public void saveTriggerHistory(TriggerInstanceHistory triggerInstanceHistory) throws ClientException {
        triggerInstanceHistoryRepository.save(triggerInstanceHistory);
    }

    /**
     * 任务执行之后更新时间
     * @param triggerInstance
     * @return
     * @throws ClientException
     */
    public TriggerInstance scheduleTriggerInstance(TriggerInstance triggerInstance, Date lastExecutionTime, Date nextExecutionTime) throws ClientException {
        triggerInstance.setLastExecuteTime(lastExecutionTime);
        triggerInstance.setNextExecuteTime(nextExecutionTime);

        return triggerInstanceRepository.saveAndFlush(triggerInstance);
    }

    public TriggerInstance stopTrigger(TriggerInstance triggerInstance) throws ClientException{
        triggerInstance.setState(TriggerInstance.STATE_STOP);

        return (TriggerInstance) baseService.saveEntity(triggerInstance);
    }

    public TriggerInstance getTriggerInstanceByName(String name) throws ClientException {
        try {
            List<TriggerInstance> triggerInstanceList = triggerInstanceRepository.findByNameAndOrgRrn(name, ThreadLocalContext.getOrgRrn());
            if (CollectionUtils.isEmpty(triggerInstanceList)) {
                throw new ClientParameterException(Exceptions.TRIGGER_INST_IS_NOT_FOUND, name);
            }
            return triggerInstanceList.get(0);
        } catch (Exception e){
            throw ExceptionManager.handleException(e, log);
        }

    }

    public List<TriggerInstance> getTriggerByState(String state) throws ClientException {
        return triggerInstanceRepository.getByState(state);
        //return triggerInstanceRepository.findAll(NBOrg.GLOBAL_ORG_RRN, "state = '" + state + "'", "");
    }

    public List<TriggerInstance> getTriggerInstance() throws ClientException {
        return triggerInstanceRepository.findAll(EnvConstant.GLOBAL_ORG_RRN);
    }

    public List<TriggerLock> getAllTriggerLock() throws ClientException {
        return triggerLockRepository.findAll(ThreadLocalContext.getOrgRrn());
    }

    public TriggerLock getTriggerLockByTriggerName(String triggerName) throws ClientException{
        return triggerLockRepository.getByTriggerName(triggerName);
    }

    /**
     * Trigger解锁。
     *  根据redis和db的不同方式做不同处理
     * @param triggerName
     * @throws ClientException
     */
    public void unLockTrigger(String triggerName) throws ClientException{
        if (TriggerConfiguration.LOCK_TYPE_DB.equals(configuration.getLockType())) {
            triggerLockRepository.deleteByTriggerName(triggerName);
        } else if (TriggerConfiguration.LOCK_TYPE_REDIS.equals(configuration.getLockType())) {
            throw new ClientParameterException(Exceptions.UN_SUPPORT_LOCK_TYPE, configuration.getLockType());
        } else {
            throw new ClientParameterException(Exceptions.UN_SUPPORT_LOCK_TYPE, configuration.getLockType());
        }
    }

    /**
     * 获取triggerLock的失效时间
     * @return
     */
    private Date getTriggerLockEffectiveTime() throws ClientException {
        try {
            NBParameter nbParameter = cacheService.getParameterByName(TriggerLock.PARAMETER_TRIGGER_LOCK_EFFECTIVE_NAME);
            if (nbParameter == null) {
                nbParameter = new NBParameter();
                nbParameter.setName(TriggerLock.PARAMETER_TRIGGER_LOCK_EFFECTIVE_NAME);
                nbParameter.setDescription(TriggerLock.PARAMETER_TRIGGER_LOCK_EFFECTIVE_NAME);
                nbParameter.setCurrentValue(TriggerLock.DEFAULT_TRIGGER_LOCK_EFFECTIVE_VALUE);
                nbParameter = cacheService.saveParameter(nbParameter);
            }
            return DateUtils.plus(DateUtils.now(), Integer.parseInt(nbParameter.getCurrentValue()), ChronoUnit.SECONDS);
        } catch (Exception e) {
            throw ExceptionManager.handleException(e, log);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean lockTrigger(String triggerName) throws ClientException {
        try {
            boolean locked = true;
            TriggerLock triggerLock = new TriggerLock();
            triggerLock.setTriggerName(triggerName);

            InetAddress address = InetAddress.getLocalHost();
            triggerLock.setIpAddress(address.getHostAddress());
            triggerLock.setMachineName(address.getHostName());
            triggerLock.setEffectiveTime(getTriggerLockEffectiveTime());

            if (TriggerConfiguration.LOCK_TYPE_DB.equals(configuration.getLockType())) {
                TriggerLock existTriggerLock = getTriggerLockByTriggerName(triggerName);
                if (existTriggerLock != null) {
                    if (existTriggerLock.getEffectiveTime().after(DateUtils.now())) {
                        log.warn("The Trigger has been locked by [" + existTriggerLock.toString() + "]");
                        locked = false;
                    } else {
                        // 超时直接Unlock
                        unLockTrigger(existTriggerLock.getTriggerName());
                    }
                }
                if (locked) {
                    triggerLockRepository.saveAndFlush(triggerLock);
                }
            } else if (TriggerConfiguration.LOCK_TYPE_REDIS.equals(configuration.getLockType())) {

                throw new ClientParameterException(Exceptions.UN_SUPPORT_LOCK_TYPE, configuration.getLockType());
            } else {
                throw new ClientParameterException(Exceptions.UN_SUPPORT_LOCK_TYPE, configuration.getLockType());
            }
            return locked;
        } catch (Exception e){
            // 当出现同一毫秒同时插入锁的话，第二次插入的自动失败
            if (e instanceof SQLIntegrityConstraintViolationException || e instanceof DataIntegrityViolationException) {
                throw new ClientParameterException("The Trigger_lock has been inserted by other machine", triggerName);
            } else {
                throw ExceptionManager.handleException(e, log);
            }
        }
    }

}
