package com.nexashop.domain.user.entity;

import com.nexashop.domain.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Permission extends BaseEntity {

    private String code;

    private String domain;

    private String description;
}
