// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.stan.wen9000.domain;

import com.stan.wen9000.domain.HfcDataOnDemand;
import com.stan.wen9000.domain.HfcIntegrationTest;
import com.stan.wen9000.domain.HfcRepository;
import com.stan.wen9000.service.HfcService;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect HfcIntegrationTest_Roo_IntegrationTest {
    
    declare @type: HfcIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: HfcIntegrationTest: @ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml");
    
    declare @type: HfcIntegrationTest: @Transactional;
    
    @Autowired
    private HfcDataOnDemand HfcIntegrationTest.dod;
    
    @Autowired
    HfcService HfcIntegrationTest.hfcService;
    
    @Autowired
    HfcRepository HfcIntegrationTest.hfcRepository;
    
    @Test
    public void HfcIntegrationTest.testCountAllHfcs() {
        Assert.assertNotNull("Data on demand for 'Hfc' failed to initialize correctly", dod.getRandomHfc());
        long count = hfcService.countAllHfcs();
        Assert.assertTrue("Counter for 'Hfc' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void HfcIntegrationTest.testFindHfc() {
        Hfc obj = dod.getRandomHfc();
        Assert.assertNotNull("Data on demand for 'Hfc' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Hfc' failed to provide an identifier", id);
        obj = hfcService.findHfc(id);
        Assert.assertNotNull("Find method for 'Hfc' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'Hfc' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void HfcIntegrationTest.testFindAllHfcs() {
        Assert.assertNotNull("Data on demand for 'Hfc' failed to initialize correctly", dod.getRandomHfc());
        long count = hfcService.countAllHfcs();
        Assert.assertTrue("Too expensive to perform a find all test for 'Hfc', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<Hfc> result = hfcService.findAllHfcs();
        Assert.assertNotNull("Find all method for 'Hfc' illegally returned null", result);
        Assert.assertTrue("Find all method for 'Hfc' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void HfcIntegrationTest.testFindHfcEntries() {
        Assert.assertNotNull("Data on demand for 'Hfc' failed to initialize correctly", dod.getRandomHfc());
        long count = hfcService.countAllHfcs();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<Hfc> result = hfcService.findHfcEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'Hfc' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'Hfc' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void HfcIntegrationTest.testFlush() {
        Hfc obj = dod.getRandomHfc();
        Assert.assertNotNull("Data on demand for 'Hfc' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Hfc' failed to provide an identifier", id);
        obj = hfcService.findHfc(id);
        Assert.assertNotNull("Find method for 'Hfc' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyHfc(obj);
        Integer currentVersion = obj.getVersion();
        hfcRepository.flush();
        Assert.assertTrue("Version for 'Hfc' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void HfcIntegrationTest.testUpdateHfcUpdate() {
        Hfc obj = dod.getRandomHfc();
        Assert.assertNotNull("Data on demand for 'Hfc' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Hfc' failed to provide an identifier", id);
        obj = hfcService.findHfc(id);
        boolean modified =  dod.modifyHfc(obj);
        Integer currentVersion = obj.getVersion();
        Hfc merged = hfcService.updateHfc(obj);
        hfcRepository.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'Hfc' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void HfcIntegrationTest.testSaveHfc() {
        Assert.assertNotNull("Data on demand for 'Hfc' failed to initialize correctly", dod.getRandomHfc());
        Hfc obj = dod.getNewTransientHfc(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'Hfc' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'Hfc' identifier to be null", obj.getId());
        hfcService.saveHfc(obj);
        hfcRepository.flush();
        Assert.assertNotNull("Expected 'Hfc' identifier to no longer be null", obj.getId());
    }
    
    @Test
    public void HfcIntegrationTest.testDeleteHfc() {
        Hfc obj = dod.getRandomHfc();
        Assert.assertNotNull("Data on demand for 'Hfc' failed to initialize correctly", obj);
        Long id = obj.getId();
        Assert.assertNotNull("Data on demand for 'Hfc' failed to provide an identifier", id);
        obj = hfcService.findHfc(id);
        hfcService.deleteHfc(obj);
        hfcRepository.flush();
        Assert.assertNull("Failed to remove 'Hfc' with identifier '" + id + "'", hfcService.findHfc(id));
    }
    
}