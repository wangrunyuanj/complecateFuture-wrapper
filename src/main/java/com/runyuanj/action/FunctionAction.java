package com.runyuanj.action;

import com.runyuanj.util.TransformUtil;
import lombok.Data;

import java.util.function.Function;

import static com.runyuanj.util.ActionUtil.validateActionName;

@Data
public class FunctionAction<T, R> implements Action {

    private String name;
    private Function<T, R> action;
    private Object[] props;

    public FunctionAction(String name, Function<T, R> action) {
        setName(name);
        this.action = action;
    }

    public FunctionAction(String name, Object... props) {
        setName(name);
        this.props = deepCopy(props);
    }

    private Object[] deepCopy(Object[] props) {
        return props;
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
