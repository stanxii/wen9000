package com.stan.wen9000.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/nav/**")
@Controller
public class NavigateController {
	@RequestMapping(value="login")
    public String ToLogin() {
        return "login";
    }
	
    @RequestMapping(value="search")
    public String ToSearch() {
        return "discovery/search";
    }
    
    @RequestMapping(value="profilemanager")
    public String ToProManager() {
        return "profiles/profilemanager";
    }
    
    @RequestMapping(value="acounts")
    public String ToAcounts() {
        return "opt/acounts";
    }
    
    @RequestMapping(value="selprofiles")
    public String ToselProfiles() {
        return "opt/selectprofiles";
    }
    
    @RequestMapping(value="tok")
//    public String ToOK() {
//        return "opt/confirm";
//    }
    public ModelAndView toOk(HttpServletRequest request,HttpServletResponse response) throws Exception {
    		ModelAndView mav = new ModelAndView("opt/confirm");//实例化一个VIew的ModelAndView实例
    		//mav.addObject("proid", "Hello World!");//添加一个带名的model对象
    		return mav;       
    }
    
    @RequestMapping(value="global_opt")
    public String Toglobal_opt() {
        return "opt/global_opt";
    }

    @RequestMapping(value="queue_opt")
    public String Toqueue_opt() {
        return "opt/queue_opt";
    }
    
    @RequestMapping(value="config_results")
    public String Toconfig_results() {
        return "opt/config_results";
    }
}
