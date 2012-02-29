package com.stan.wen9000.domain;

import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@RooEquals
@RooJson
public class Cbatinfo {

    @Value("161")
    private Integer agentPort;

    private String appVer;

    @Value("1.0.0")
    private String bootVer;

    private Long mvId;

    @Value("false")
    private Boolean mvStatus;

    @NotNull
    @Value("na")
    private String address;

    @NotNull
    @Value("na")
    private String contact;

    @NotNull
    @Value("eoc master")
    private String label;

    @NotNull
    @Value("na")
    private String phone;
}
