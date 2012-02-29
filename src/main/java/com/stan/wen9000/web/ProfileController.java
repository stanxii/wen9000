package com.stan.wen9000.web;

import com.stan.wen9000.domain.Profile;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RooWebJson(jsonObject = Profile.class)
@Controller
@RequestMapping("/profiles")
@RooWebScaffold(path = "profiles", formBackingObject = Profile.class)
public class ProfileController {
}
