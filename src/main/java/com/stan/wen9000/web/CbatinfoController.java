package com.stan.wen9000.web;

import com.stan.wen9000.domain.Cbatinfo;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/cbatinfoes")
@Controller
@RooWebScaffold(path = "cbatinfoes", formBackingObject = Cbatinfo.class)
public class CbatinfoController {
}
