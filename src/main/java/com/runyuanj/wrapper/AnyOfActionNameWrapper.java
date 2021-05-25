package com.runyuanj.wrapper;

import lombok.Data;

import static com.runyuanj.util.ActionUtil.validateActionName;

@Data
public class AnyOfActionNameWrapper {

    public AnyOfActionNameWrapper(String name) {
        this.name = validateActionName(name);;
    }

    private String name;
}
