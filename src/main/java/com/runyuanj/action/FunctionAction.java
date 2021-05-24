package com.runyuanj.action;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@Data
@NoArgsConstructor
public class FunctionAction<T, R> implements Action {

    public FunctionAction(String name, Function<T, R> action) {
        this.name = name;
        this.action = action;
    }

    public FunctionAction(String name, Object... props) {
        this.name = name;
        this.props = props;
    }

    private String name;

    private Function<T, R> action;

    private Object[] props;

    @Override
    public Function getAction() {
        return this.action;
    }

    public Function getFunction() {
        return this.action;
    }

}
