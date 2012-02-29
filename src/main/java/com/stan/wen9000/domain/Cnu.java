package com.stan.wen9000.domain;

import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@RooEquals
public class Cnu {

    @NotNull
    private String mac;

    @NotNull
    @Value("na")
    private String label;

    @ManyToOne
    private Cbat cbat;

    @ManyToOne
    private Profile profile;
}
