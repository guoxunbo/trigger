package com.newbiest.common.trigger;

import com.newbiest.base.factory.ModelFactory;
import com.newbiest.common.trigger.model.TriggerInstance;
import com.newbiest.common.trigger.model.TriggerInstanceHistory;
import liquibase.integration.spring.SpringLiquibase;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Created by guoxunbo on 2019-11-11 11:22
 */
@Configuration
@Data
@Slf4j
@ConfigurationProperties(prefix = "trigger")
public class TriggerConfiguration {

    public static final String LOCK_TYPE_DB = "db";
    public static final String LOCK_TYPE_REDIS = "redis";

    public static final String TRIGGER_THREAD_POOL_NAME = "trigger-";
    /**
     * Trigger的Lock类型
     *  DB：基于数据库实现的抢占式锁
     *  Redis: 基于redis实现的分布式缓存锁
     */
    private String lockType = LOCK_TYPE_DB;

    @Bean("triggerLiquibase")
    public SpringLiquibase liquibase(DataSource dataSource) throws Exception{
        if (log.isInfoEnabled()) {
            log.info("Load Trigger Liquibase Configuration.");
        }
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-trigger.yaml");
        liquibase.setShouldRun(true);
        liquibase.setDropFirst(false);
        return liquibase;
    }

}
