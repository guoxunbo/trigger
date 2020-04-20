package com.newbiest.common.trigger.model;

import com.newbiest.base.model.NBBase;
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
    private Date lastExecuteTime;

    @Column(name="NEXT_EXECUTE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
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
