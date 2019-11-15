package com.newbiest.common.trigger.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by guoxunbo on 2019-11-11 15:02
 */
@Data
public class TriggerResult implements Serializable {

    public static final String RESULT_CODE_SUCCESS = "Success";
    public static final String RESULT_CODE_FAIL = "Fail";

    private String resultCode = RESULT_CODE_SUCCESS;

    private String resultText;

    private boolean recordHistory = true;

}
