package com.stan.wen9000.web;

import com.stan.wen9000.domain.Cbat;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RooWebJson(jsonObject = Cbat.class)
@Controller
@RequestMapping("/cbats")
@RooWebScaffold(path = "cbats", formBackingObject = Cbat.class)
public class CbatController {
}
