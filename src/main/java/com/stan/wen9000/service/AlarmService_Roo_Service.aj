// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.stan.wen9000.service;

import com.stan.wen9000.domain.Alarm;
import com.stan.wen9000.service.AlarmService;
import java.util.List;

privileged aspect AlarmService_Roo_Service {
    
    public abstract long AlarmService.countAllAlarms();    
    public abstract void AlarmService.deleteAlarm(Alarm alarm);    
    public abstract Alarm AlarmService.findAlarm(Long id);    
    public abstract List<Alarm> AlarmService.findAllAlarms();    
    public abstract List<Alarm> AlarmService.findAlarmEntries(int firstResult, int maxResults);    
    public abstract void AlarmService.saveAlarm(Alarm alarm);    
    public abstract Alarm AlarmService.updateAlarm(Alarm alarm);    
}
