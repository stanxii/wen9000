// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.stan.wen9000.domain;

import com.stan.wen9000.domain.Hfc;
import com.stan.wen9000.domain.HfcRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

privileged aspect HfcRepository_Roo_Jpa_Repository {
    
    declare parents: HfcRepository extends JpaRepository<Hfc, Long>;
    
    declare parents: HfcRepository extends JpaSpecificationExecutor<Hfc>;
    
    declare @type: HfcRepository: @Repository;
    
}