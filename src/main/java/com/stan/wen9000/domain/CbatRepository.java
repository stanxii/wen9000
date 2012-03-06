package com.stan.wen9000.domain;

import org.springframework.roo.addon.layers.repository.jpa.RooJpaRepository;

@RooJpaRepository(domainType = Cbat.class)
public interface CbatRepository {
    Cbat findByMac(String mac);
}
