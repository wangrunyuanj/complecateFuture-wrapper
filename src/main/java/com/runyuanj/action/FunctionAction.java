package com.runyuanj.action;

import com.runyuanj.util.TransformUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

import static com.runyuanj.util.ActionUtil.GET_ACTION_FUTURE;

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
        this.props = props;
    }

    private void setName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new RuntimeException("Name can not be empty");
        }
        if (name.startsWith(GET_ACTION_FUTURE)) {
            throw new RuntimeException("Name can not start with " + GET_ACTION_FUTURE);
        }
        this.name = name.trim();
    }

    public <U> U getProp(int index, Class<U> type) {
        if (props != null && props.length > index) {
            return TransformUtil.convert(props[index], type);
        } else {
            throw new IndexOutOfBoundsException("Action " + name + " param index out of range! index: " + index);
        }
    }
}
