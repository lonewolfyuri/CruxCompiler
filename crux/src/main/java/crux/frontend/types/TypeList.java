package crux.frontend.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class TypeList extends Type implements Iterable<Type> {
    private final List<Type> list;

    public TypeList() {
        list = new ArrayList<>();
    }

    public TypeList(List<Type> types) {
        list = types;
    }

    public static TypeList of(Type... types) {
        var typeList = new TypeList();
        typeList.list.addAll(Arrays.asList(types));
        return typeList;
    }

    public void append(Type type) {
        list.add(type);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean equivalent(Type that) {
        if (that.getClass() != TypeList.class)
            return false;

        var olist = ((TypeList) that).list;

        if (list.size() != olist.size())
            return false;

        for (int i = 0; i < list.size(); ++i) {
            if (!list.get(i).equivalent(olist.get(i)))
                return false;
        }
        return true;
    }

    @Override
    public Iterator<Type> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TypeList(");
        for (int i = 0; i < list.size() - 1; ++i)
            sb.append(list.get(i) + ", ");
        if (list.size() >= 1)
            sb.append(list.get(list.size() - 1));
        sb.append(")");
        return sb.toString();
    }
}
