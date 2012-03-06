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
import org.springframework.web.bind.annotation.ResponseBody;

import com.stan.wen9000.domain.Cbat;
import com.stan.wen9000.domain.Cnu;
import com.stan.wen9000.service.CbatService;

import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/nav/**")
@Controller
public class NavigateController {
   
    @RequestMapping(value="search")
    public String ToSearch() {
        return "discovery/search";
    }
    
   
}
