package com.runyuanj.action;

import com.runyuanj.util.TransformUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

import static com.runyuanj.util.ActionUtil.GET_ACTION_FUTURE;

@Data
public class SupplierAction<T> implements Action {

    private String name;
    private Supplier<T> action;
    private Object[] props;

    public SupplierAction(String name, Object... props) {
        setName(name);
        this.props = props;
    }

    public SupplierAction(String name, Supplier<T> supplier) {
        setName(name);
        this.action = supplier;
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
