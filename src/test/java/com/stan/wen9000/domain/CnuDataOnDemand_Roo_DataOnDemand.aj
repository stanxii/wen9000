// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.stan.wen9000.domain;

import com.stan.wen9000.domain.Cbat;
import com.stan.wen9000.domain.CbatDataOnDemand;
import com.stan.wen9000.domain.Cnu;
import com.stan.wen9000.domain.CnuDataOnDemand;
import com.stan.wen9000.domain.CnuRepository;
import com.stan.wen9000.domain.Profile;
import com.stan.wen9000.domain.ProfileDataOnDemand;
import com.stan.wen9000.service.CnuService;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

privileged aspect CnuDataOnDemand_Roo_DataOnDemand {
    
    declare @type: CnuDataOnDemand: @Component;
    
    private Random CnuDataOnDemand.rnd = new SecureRandom();
    
    private List<Cnu> CnuDataOnDemand.data;
    
    @Autowired
    private CbatDataOnDemand CnuDataOnDemand.cbatDataOnDemand;
    
    @Autowired
    private ProfileDataOnDemand CnuDataOnDemand.profileDataOnDemand;
    
    @Autowired
    CnuService CnuDataOnDemand.cnuService;
    
    @Autowired
    CnuRepository CnuDataOnDemand.cnuRepository;
    
    public Cnu CnuDataOnDemand.getNewTransientCnu(int index) {
        Cnu obj = new Cnu();
        setCbat(obj, index);
        setLabel(obj, index);
        setMac(obj, index);
        setProfile(obj, index);
        return obj;
    }
    
    public void CnuDataOnDemand.setCbat(Cnu obj, int index) {
        Cbat cbat = cbatDataOnDemand.getRandomCbat();
        obj.setCbat(cbat);
    }
    
    public void CnuDataOnDemand.setLabel(Cnu obj, int index) {
        String label = "label_" + index;
        obj.setLabel(label);
    }
    
    public void CnuDataOnDemand.setMac(Cnu obj, int index) {
        String mac = "mac_" + index;
        obj.setMac(mac);
    }
    
    public void CnuDataOnDemand.setProfile(Cnu obj, int index) {
        Profile profile = profileDataOnDemand.getRandomProfile();
        obj.setProfile(profile);
    }
    
    public Cnu CnuDataOnDemand.getSpecificCnu(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        Cnu obj = data.get(index);
        Long id = obj.getId();
        return cnuService.findCnu(id);
    }
    
    public Cnu CnuDataOnDemand.getRandomCnu() {
        init();
        Cnu obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return cnuService.findCnu(id);
    }
    
    public boolean CnuDataOnDemand.modifyCnu(Cnu obj) {
        return false;
    }
    
    public void CnuDataOnDemand.init() {
        int from = 0;
        int to = 10;
        data = cnuService.findCnuEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'Cnu' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<Cnu>();
        for (int i = 0; i < 10; i++) {
            Cnu obj = getNewTransientCnu(i);
            try {
                cnuService.saveCnu(obj);
            } catch (ConstraintViolationException e) {
                StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                    ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getConstraintDescriptor()).append(":").append(cv.getMessage()).append("=").append(cv.getInvalidValue()).append("]");
                }
                throw new RuntimeException(msg.toString(), e);
            }
            cnuRepository.flush();
            data.add(obj);
        }
    }
    
}
