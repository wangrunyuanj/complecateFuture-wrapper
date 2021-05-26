package com.runyuanj.action;

import com.runyuanj.util.TransformUtil;
import lombok.Data;

import java.util.function.Supplier;

import static com.runyuanj.util.ActionUtil.validateActionName;

@Data
public class SupplierAction<T> implements Action {

    private String name;
    private Supplier<T> action;
    private Object[] props;

    public SupplierAction(String name, Object... props) {
        setName(name);
        this.props = deepCopy(props);
    }

    private Object[] deepCopy(Object[] props) {
        return props;
    }

    public SupplierAction(String name, Supplier<T> supplier) {
        setName(name);
        this.action = supplier;
    }

    private void setName(String name) {
        this.name = validateActionName(name);
    }

    public <U> U getProp(int index, Class<U> type) {
        if (props != null && props.length > index) {
            return TransformUtil.convert(props[index], type);
        } else {
            throw new IndexOutOfBoundsException("Action " + name + " param index out of range! index: " + index);
        }
    }

}
