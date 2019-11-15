package com.newbiest.common.trigger.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.newbiest.base.model.NBUpdatable;
import com.newbiest.base.utils.DateUtils;
import com.newbiest.security.model.NBOrg;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * 基于数据库实现的定时任务锁定表。
 *  当分布式的时候，确保任务只执行一次
 * Created by guoxunbo on 2019-11-11 10:52
 */
@Data
@Entity
@Table(name="COM_TRIGGER_LOCK")
public class TriggerLock extends NBUpdatable {

    public static final String PARAMETER_TRIGGER_LOCK_EFFECTIVE_NAME = "trigger_lock_effective";

    public static final String DEFAULT_TRIGGER_LOCK_EFFECTIVE_VALUE = "30";

    public static final String LOCK_STATE_LOCK = "Lock";

    @Column(name = "TRIGGER_NAME")
    private String triggerName;

    @Column(name = "LOCK_STATE")
    private String lockState = LOCK_STATE_LOCK;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Column(name = "MACHINE_NAME")
    private String machineName;

    /**
     * 这个锁的最长有效时间 超过这个时间，就自动释放锁
     */
    @Column(name="EFFECTIVE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(timezone = GMT_PE,pattern = DateUtils.DEFAULT_DATETIME_PATTERN)
    private Date effectiveTime;

    @PrePersist
    protected void prePersist() {
        super.prePersist();
        this.orgRrn = NBOrg.GLOBAL_ORG_RRN;
    }

}
