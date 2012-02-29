// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.stan.wen9000.domain;

import com.stan.wen9000.domain.Hfc;
import com.stan.wen9000.domain.HfcDataOnDemand;
import com.stan.wen9000.domain.HfcRepository;
import com.stan.wen9000.reference.HfcClass;
import com.stan.wen9000.reference.HfcDeviceType;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

privileged aspect HfcDataOnDemand_Roo_DataOnDemand {
    
    declare @type: HfcDataOnDemand: @Component;
    
    private Random HfcDataOnDemand.rnd = new SecureRandom();
    
    private List<Hfc> HfcDataOnDemand.data;
    
    @Autowired
    HfcRepository HfcDataOnDemand.hfcRepository;
    
    public Hfc HfcDataOnDemand.getNewTransientHfc(int index) {
        Hfc obj = new Hfc();
        setDeviceType(obj, index);
        setHfcType(obj, index);
        setIp(obj, index);
        setLabel(obj, index);
        setMac(obj, index);
        return obj;
    }
    
    public void HfcDataOnDemand.setDeviceType(Hfc obj, int index) {
        HfcDeviceType deviceType = HfcDeviceType.class.getEnumConstants()[0];
        obj.setDeviceType(deviceType);
    }
    
    public void HfcDataOnDemand.setHfcType(Hfc obj, int index) {
        HfcClass hfcType = HfcClass.class.getEnumConstants()[0];
        obj.setHfcType(hfcType);
    }
    
    public void HfcDataOnDemand.setIp(Hfc obj, int index) {
        String ip = "ip_" + index;
        obj.setIp(ip);
    }
    
    public void HfcDataOnDemand.setLabel(Hfc obj, int index) {
        String label = "label_" + index;
        obj.setLabel(label);
    }
    
    public void HfcDataOnDemand.setMac(Hfc obj, int index) {
        String mac = "mac_" + index;
        obj.setMac(mac);
    }
    
    public Hfc HfcDataOnDemand.getSpecificHfc(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        Hfc obj = data.get(index);
        Long id = obj.getId();
        return hfcRepository.findOne(id);
    }
    
    public Hfc HfcDataOnDemand.getRandomHfc() {
        init();
        Hfc obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return hfcRepository.findOne(id);
    }
    
    public boolean HfcDataOnDemand.modifyHfc(Hfc obj) {
        return false;
    }
    
    public void HfcDataOnDemand.init() {
        int from = 0;
        int to = 10;
        data = hfcRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'Hfc' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<Hfc>();
        for (int i = 0; i < 10; i++) {
            Hfc obj = getNewTransientHfc(i);
            try {
                hfcRepository.save(obj);
            } catch (ConstraintViolationException e) {
                StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                    ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getConstraintDescriptor()).append(":").append(cv.getMessage()).append("=").append(cv.getInvalidValue()).append("]");
                }
                throw new RuntimeException(msg.toString(), e);
            }
            hfcRepository.flush();
            data.add(obj);
        }
    }
    
}
