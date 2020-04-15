package com.newbiest.common.trigger.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.newbiest.base.model.NBBase;
import com.newbiest.base.utils.DateUtils;
import com.newbiest.base.utils.StringUtils;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 *
 * Created by guoxunbo on 2019-11-11 11:17
 */
@Data
@Entity
@Table(name="COM_TRIGGER")
public class TriggerInstance extends NBBase {

    public static final String STATE_SCHEDULING = "Scheduling";
    public static final String STATE_UPDATE = "Update";
    public static final String STATE_STOP = "Stop";

    @Column(name="NAME")
    private String name;

    @Column(name="DESCRIPTION")
    private String description;

    @Column(name="CRON_EXPRESSION")
    private String cronExpression;

    @Column(name="LAST_EXECUTE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = DateUtils.DEFAULT_DATETIME_PATTERN)
    private Date lastExecuteTime;

    @Column(name="NEXT_EXECUTE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = DateUtils.DEFAULT_DATETIME_PATTERN)
    private Date nextExecuteTime;

    /**
     * 状态
     */
    @Column(name="STATE")
    private String state = STATE_SCHEDULING;

    public boolean isScheduling() {
        return StringUtils.isNullOrEmpty(this.state) || STATE_SCHEDULING.equals(this.state);
    }
}
