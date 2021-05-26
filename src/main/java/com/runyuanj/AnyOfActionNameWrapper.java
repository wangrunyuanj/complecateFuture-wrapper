package com.runyuanj;

import lombok.Data;

import static com.runyuanj.util.ActionUtil.validateActionName;

@Data
public class AnyOfActionNameWrapper {

    private String name;

    public AnyOfActionNameWrapper(String name) {
        this.name = validateActionName(name);
    }
}
