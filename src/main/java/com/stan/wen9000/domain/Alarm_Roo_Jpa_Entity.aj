// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.stan.wen9000.domain;

import com.stan.wen9000.domain.Alarm;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

privileged aspect Alarm_Roo_Jpa_Entity {
    
    declare @type: Alarm: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long Alarm.id;
    
    @Version
    @Column(name = "version")
    private Integer Alarm.version;
    
    public Long Alarm.getId() {
        return this.id;
    }
    
    public void Alarm.setId(Long id) {
        this.id = id;
    }
    
    public Integer Alarm.getVersion() {
        return this.version;
    }
    
    public void Alarm.setVersion(Integer version) {
        this.version = version;
    }
    
}
