package com.dobbinsoft.gus.remotesales.client.gus.location.model;

public enum LocationType {
    STORE,
    WAREHOUSE,
    /**
     * The definition of virtual location:
     * Virtual location is a combine of real location (store & warehouse). The product stocks in virtual location equals the sum of union locations
     */
    VIRTUAL,
}
