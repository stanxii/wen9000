package com.stan.wen9000.domain;

import com.stan.wen9000.reference.HfcClass;
import com.stan.wen9000.reference.HfcDeviceType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
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
public class Hfc {

    @Enumerated
    private HfcDeviceType deviceType;

    @Enumerated
    private HfcClass hfcType;

    @NotNull
    private String ip;

    @NotNull
    private String mac;

    @NotNull
    private String label;
}
