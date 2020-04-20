package com.newbiest.common.trigger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.newbiest.base.service.BaseService;
import com.newbiest.base.service.FrameworkCacheService;
import com.newbiest.base.utils.StringUtils;
import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.common.trigger.service.TriggerService;
import com.newbiest.common.trigger.thread.DaemonThread;
import com.newbiest.common.trigger.thread.TriggerThead;
import com.newbiest.security.service.SecurityService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by guoxunbo on 2019-11-11 17:42
 */
@Configuration
@EnableScheduling
@Data
@Slf4j
public class NewbiestScheduleConfig implements SchedulingConfigurer {

    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    private List<TriggerInstance> triggerInstanceList;

    private static AtomicBoolean isInit = new AtomicBoolean(false);

    private Map<String, ScheduledFuture> futureMap = Maps.newConcurrentMap();
    private Map<String, TriggerThead> triggerTheadMap = Maps.newConcurrentMap();

    @Autowired
    TriggerService triggerService;

    @Autowired
    BaseService baseService;

    @Autowired
    SecurityService securityService;

    @Autowired
    FrameworkCacheService frameworkCacheService;

    /**
     * 加载所有的TriggerInstance
     */
    @PostConstruct
    public void init() {
        triggerInstanceList = triggerService.getTriggerInstance();
        if (triggerInstanceList == null) {
            triggerInstanceList = Lists.newArrayList();
        }
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(taskExecutor());
        this.scheduledTaskRegistrar = scheduledTaskRegistrar;
        NewbiestScheduleConfig.getIsInit().set(true);
        addDaemonTrigger();
    }

    public static AtomicBoolean getIsInit() {
        return isInit;
    }

    public void addDaemonTrigger() {
        TriggerContext defaultTriggerContext = new TriggerContext();
        TriggerInstance triggerInstance = triggerService.getTriggerInstanceByName(DaemonThread.DAEMON_THREAD_NAME);

        defaultTriggerContext.setTriggerInstance(triggerInstance);
        defaultTriggerContext.setTriggerService(triggerService);
        defaultTriggerContext.setBaseService(baseService);
        defaultTriggerContext.setSecurityService(securityService);
//        addTrigger(new DaemonThread(defaultTriggerContext, this));
    }

    public void addTrigger(TriggerThead triggerThead) {
        TaskScheduler scheduler = this.scheduledTaskRegistrar.getScheduler();
        ScheduledFuture future = scheduler.schedule(triggerThead, new NewbiestTrigger(triggerThead.getTriggerContext()));
        futureMap.put(triggerThead.getName(), future);
        triggerTheadMap.put(triggerThead.getName(), triggerThead);
    }

    /**
     * 因为线程在cancel之后会被interrupt掉，故此处不能restart daemon线程
     * @param triggerInstance
     */
    public void restartTrigger(TriggerInstance triggerInstance) {
        try {
            if (DaemonThread.DAEMON_THREAD_NAME.equals(triggerInstance.getName())) {
                triggerInstance.setState(StringUtils.EMPTY);
                return;
            }
            stopTrigger(triggerInstance);
            TriggerThead triggerThead = triggerTheadMap.get(triggerInstance.getName());

            TriggerContext triggerContext = triggerThead.getTriggerContext();
            triggerInstance.setState(StringUtils.EMPTY);
            triggerContext.setTriggerInstance(triggerInstance);
            triggerThead.setTriggerContext(triggerContext);
            addTrigger(triggerThead);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 因为线程在cancel之后会被interrupt掉，故此处不能stop daemon线程
     * @param triggerInstance
     */
    public void stopTrigger(TriggerInstance triggerInstance) {
        if (DaemonThread.DAEMON_THREAD_NAME.equals(triggerInstance.getName())) {
            return;
        }
        ScheduledFuture scheduledFuture = futureMap.get(triggerInstance.getName());
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            futureMap.remove(scheduledFuture);
        }
    }

    /**
     * 创建trigger的线程池，线程池大小为triggerInst长度+Daemon线程
     * @return
     */
    @Bean(destroyMethod="shutdown")
    public Executor taskExecutor() {
        return new ScheduledThreadPoolExecutor(triggerInstanceList.size() + 1, new NamedThreadFactory(TriggerConfiguration.TRIGGER_THREAD_POOL_NAME));
    }

}
