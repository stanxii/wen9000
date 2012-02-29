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
public class Profile {

    @NotNull
    @Value("enable")
    private Boolean port0enable;

    @NotNull
    @Value("enable")
    private Boolean port1enable;

    @NotNull
    @Value("enable")
    private Boolean port2enable;

    @NotNull
    @Value("enable")
    private Boolean port3enable;

    @NotNull
    private Integer port0rxrate;

    @NotNull
    private Integer port1rxrate;

    @NotNull
    private Integer port2rxrate;

    @NotNull
    private Integer port3rxrate;

    @NotNull
    private Integer port0txrate;

    @NotNull
    private Integer port1txrate;

    @NotNull
    private Integer port2txrate;

    @NotNull
    private Integer port3txrate;

    @NotNull
    private Integer port0vid;

    @NotNull
    private Integer port1vid;

    @NotNull
    private Integer port2vid;

    @NotNull
    private Integer port3vid;

    @NotNull
    @Value("false")
    private Boolean rxlimitsts;

    @NotNull
    @Value("false")
    private Boolean txlimitsts;

    @NotNull
    @Value("false")
    private Boolean vlanenable;

    @NotNull
    @Value("na")
    private String profilename;
}
