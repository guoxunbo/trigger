package com.newbiest.common.trigger.repository;

import com.newbiest.base.repository.custom.IRepository;
import com.newbiest.base.utils.StringUtils;
import com.newbiest.common.trigger.model.TriggerInstance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriggerInstanceRepository extends IRepository<TriggerInstance, Long> {

}
