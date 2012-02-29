package com.stan.wen9000.web;

import com.stan.wen9000.domain.Cnu;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/cnus")
@Controller
@RooWebScaffold(path = "cnus", formBackingObject = Cnu.class)
public class CnuController {
}
