package com.stan.wen9000.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stan.wen9000.web.CbatController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.stan.wen9000.domain.Cbat;
import com.stan.wen9000.domain.Cnu;
import com.stan.wen9000.service.CbatService;

import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/discovery/**")
@Controller
public class DiscoveryController {

    //@Autowired
     //CbatController cbatctl;
	

	@Autowired
    CnuController cnuctl;
    
    @RequestMapping(method = RequestMethod.POST, value = "{id}")
    public void post(@PathVariable Long id, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    }

    @RequestMapping
    public String index() {
        return "discovery/index";
    }
    
    @RequestMapping(value = "searchresult",  headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> searchListAll() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
//        List<Cbat> result = cbatctl.cbatService.findAllCbats();
        List<Cnu>  result = cnuctl.cnuService.findAllCnus();
        return new ResponseEntity<String>(Cnu.toJsonArray(result), headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "search", method = RequestMethod.POST)      
    public String discoveryProductor(@RequestParam(value = "startip", required = false) String startip, @RequestParam(value = "startip", required = false) String stopip ) {
               
    	System.out.println("startip=" + startip +" stop=" + stopip);
    	
    	return "discovery/index";
    }
    
}
