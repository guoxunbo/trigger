package com.newbiest.common.trigger.repository;

import com.newbiest.base.exception.ClientException;
import com.newbiest.base.repository.custom.IRepository;
import com.newbiest.common.trigger.model.TriggerInstance;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriggerInstanceRepository extends IRepository<TriggerInstance, Long> {

    List<TriggerInstance> getByState(String state) throws ClientException;

}
