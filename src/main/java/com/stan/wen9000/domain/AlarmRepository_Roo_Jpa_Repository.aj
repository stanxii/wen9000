// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.stan.wen9000.domain;

import com.stan.wen9000.domain.Alarm;
import com.stan.wen9000.domain.AlarmRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

privileged aspect AlarmRepository_Roo_Jpa_Repository {
    
    declare parents: AlarmRepository extends JpaRepository<Alarm, Long>;
    
    declare parents: AlarmRepository extends JpaSpecificationExecutor<Alarm>;
    
    declare @type: AlarmRepository: @Repository;
    
}
