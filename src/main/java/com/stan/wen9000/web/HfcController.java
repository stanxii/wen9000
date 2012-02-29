package com.stan.wen9000.web;

import com.stan.wen9000.domain.Hfc;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RooWebJson(jsonObject = Hfc.class)
@Controller
@RequestMapping("/hfcs")
@RooWebScaffold(path = "hfcs", formBackingObject = Hfc.class)
public class HfcController {
}
