package com.runyuanj.action;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@Data
@NoArgsConstructor
public class FunctionAction<T, R> implements Action {

    private String name;
    private Function<T, R> action;
    private Object[] props;

    public FunctionAction(String name, Function<T, R> action) {
        this.name = name;
        this.action = action;
    }

    public FunctionAction(String name, Object... props) {
        this.name = name;
        this.props = props;
    }
}
