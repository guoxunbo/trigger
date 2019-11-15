package com.newbiest.common.trigger.repository;

import com.newbiest.base.exception.ClientException;
import com.newbiest.base.repository.custom.IRepository;
import com.newbiest.common.trigger.model.TriggerInstanceHistory;
import com.newbiest.common.trigger.model.TriggerLock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TriggerLockRepository extends IRepository<TriggerLock, Long> {

    TriggerLock getByTriggerName(String triggerName) throws ClientException;

    @Modifying
    @Query("DELETE FROM TriggerLock TriggerLock WHERE TriggerLock.triggerName = :triggerName")
    void deleteByTriggerName(String triggerName) throws ClientException;

}
