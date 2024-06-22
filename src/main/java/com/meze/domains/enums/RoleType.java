package com.meze.domains.enums;

public enum RoleType {

    ROLE_CUSTOMER("Customer"),
    ROLE_MANAGER("Manager"),
    ROLE_ADMIN("Administrator");

    private String name;

    //cons'u dışarı açmamak için private yapıyoruz
    private RoleType(String name) {
        this.name=name;
    }

    public String getName() {
        return name;
    }
}
