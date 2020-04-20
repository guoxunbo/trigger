package com.newbiest.common.trigger.model;

import com.newbiest.base.model.NBHis;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 *
 * Created by guoxunbo on 2019-11-11 11:17
 */
@Data
@Entity
@Table(name="COM_TRIGGER_HIS")
public class TriggerInstanceHistory extends NBHis {

    public static final String TRANS_TYPE_STOP = "Stop";
    public static final String TRANS_TYPE_EXECUTE = "Execute";

    @Column(name="NAME")
    private String name;

    @Column(name="DESCRIPTION")
    private String description;

    @Column(name="CRON_EXPRESSION")
    private String cronExpression;

    /**
     * 花费时间 单位毫秒
     */
    @Column(name="ELAPSED_TIME")
    private Long elapsedTime;

    @Column(name="RESULT_CODE")
    private String resultCode = TriggerResult.RESULT_CODE_SUCCESS;

    @Column(name="RESULT_TEXT")
    private String resultText;

    @Column(name="EXECUTE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date executeTime;

}
