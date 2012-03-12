package com.stan.wen9000.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/nav/**")
@Controller
public class NavigateController {
   
    @RequestMapping(value="search")
    public String ToSearch() {
        return "discovery/search";
    }
    
    @RequestMapping(value="profilemanager")
    public String ToProManager() {
        return "profiles/profilemanager";
    }
}
