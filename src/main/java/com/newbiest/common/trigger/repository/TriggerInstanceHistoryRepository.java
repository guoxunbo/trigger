package com.newbiest.common.trigger.repository;

import com.newbiest.base.repository.custom.IRepository;
import com.newbiest.common.trigger.model.TriggerInstanceHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface TriggerInstanceHistoryRepository extends IRepository<TriggerInstanceHistory, String> {

}
