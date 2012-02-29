package com.stan.wen9000.domain;

import com.stan.wen9000.reference.EocDeviceType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
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
public class Cbat {

    private Boolean active;

    @NotNull
    private String ip;

    @NotNull
    private String mac;

    @NotNull
    @Value("na")
    private String label;

    @NotNull
    @Enumerated
    private EocDeviceType deviceType;

    @OneToOne
    private Cbatinfo cbatinfo;
}
