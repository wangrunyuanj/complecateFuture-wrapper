package com.runyuanj.action;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@Data
@NoArgsConstructor
public class SupplierAction<T> implements Action {

    private String name;
    private Supplier<T> action;
    private Object[] props;

    public SupplierAction(String name) {
        this.name = name;
    }

    public SupplierAction(String name, Supplier<T> supplier) {
        this.name = name;
        this.action = supplier;
    }

    public SupplierAction(String name, Object... props) {
        this.name = name;
        this.props = props;
    }

    public Object[] getProps() {
        return props;
    }

    @Override
    public Supplier<T> getAction() {
        return this.action;
    }

}
