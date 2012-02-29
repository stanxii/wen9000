package com.stan.wen9000.web;

import com.stan.wen9000.domain.Cbatinfo;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RooWebJson(jsonObject = Cbatinfo.class)
@Controller
@RequestMapping("/cbatinfoes")
@RooWebScaffold(path = "cbatinfoes", formBackingObject = Cbatinfo.class)
public class CbatinfoController {
}
