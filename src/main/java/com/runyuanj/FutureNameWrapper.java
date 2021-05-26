package com.runyuanj;

import lombok.Data;

import static com.runyuanj.util.ActionUtil.validateActionName;

@Data
public class FutureNameWrapper {

    private String name;

    public FutureNameWrapper(String name) {
        this.name = validateActionName(name);
    }
}
