package com.stan.wen9000.domain;

import java.util.Date;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
@RooEquals
public class Alarm {

    private Integer alarmcode;

    private Integer alarmlevel;

    private Integer alarmtype;

    private Integer alarmvalue;

    private Integer cltindex;

    private Integer cnuindex;

    private String cnumac;

    private String cbatmac;

    private String cbatip;

    private String oid;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date realtime;

    private String trapinfo;

    private String timeticks;

    private Integer itemnumber;
}
