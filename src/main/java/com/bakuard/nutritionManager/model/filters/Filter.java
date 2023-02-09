package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Filter {

    public static enum Type {
        OR,
        AND,
        MIN_TAGS,
        CATEGORY,
        SHOPS,
        GRADES,
        MANUFACTURER,
        INGREDIENTS,
        USER,
        MIN_QUANTITY,
        DISHES,
        MENUS
    }

    /**
     * Создает и возвращает новый объект ограничения AndFilter.
     * @param a первый обязательный операнд ограничения AndFilter.
     * @param b второй обязательный операнд ограничения AndFilter.
     * @param other другие не обязательные операнды ограничения AndFilter.
     * @return новый объект ограничения AndFilter.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если кол-во ограничений в списке filters меньше двух.<br/>
     *          2. если хотя бы один из операндов имеет значение null.<br/>
     *          3. если передаваемый список операндов имеет значение null.
     */
    public static AndFilter and(Filter a, Filter b, Filter... other) {
        return new AndFilter(toList(a, b, other));
    }

    /**
     * Создает и возвращает новый объект ограничения AndFilter.
     * @param filters список ограничений выступающих как операнды данного ограничения.
     * @return новый объект ограничения AndFilter.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если хотя бы один из операндов имеет значение null.<br/>
     *          2. если передаваемый массив операндов имеет значение null.
     */
    public static AndFilter and(List<Filter> filters) {
        return new AndFilter(filters);
    }

    /**
     * Создает и возвращает новый объект {@link OrFilter} для указываемых ограничений.
     * @param a первый обязательный операнд ограничения.
     * @param b второй обязательный операнд ограничения.
     * @param other необязательные операнды ограничения.
     * @return возвращает новый объект {@link OrFilter} для указываемых ограничений.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если хотя бы один из операндов имеет значение null.<br/>
     *          2. если передаваемый массив операндов имеет значение null.
     */
    public static OrFilter or(Filter a, Filter b, Filter... other) {
        return new OrFilter(toList(a, b, other));
    }

    /**
     * Создает и возвращает новый объект {@link OrFilter} для указываемых ограничений.
     * @param filters список ограничений выступающих как операнды данного ограничения.
     * @return возвращает новый объект {@link OrFilter} для указываемых ограничений.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если кол-во ограничений в списке filters меньше двух.<br/>
     *          2. если хотя бы один из операндов имеет значение null.<br/>
     *          3. если передаваемый список операндов имеет значение null.
     */
    public static OrFilter or(List<Filter> filters) {
        return new OrFilter(filters);
    }

    public static AnyFilter anyCategory(String a, String... other) {
        return new AnyFilter(toList(a, other), 1, Filter.Type.CATEGORY);
    }

    public static AnyFilter anyCategory(List<String> values) {
        return new AnyFilter(values, 1, Type.CATEGORY);
    }

    public static AnyFilter anyManufacturer(String a, String... other) {
        return new AnyFilter(toList(a, other), 1, Type.MANUFACTURER);
    }

    public static AnyFilter anyManufacturer(List<String> values) {
        return new AnyFilter(values, 1, Type.MANUFACTURER);
    }

    public static AnyFilter anyShop(String a, String... other) {
        return new AnyFilter(toList(a, other), 1, Type.SHOPS);
    }

    public static AnyFilter anyShop(List<String> values) {
        return new AnyFilter(values, 1, Type.SHOPS);
    }

    public static AnyFilter anyGrade(String a, String... other) {
        return new AnyFilter(toList(a, other), 1, Type.GRADES);
    }

    public static AnyFilter anyGrade(List<String> values) {
        return new AnyFilter(values, 1, Type.GRADES);
    }

    public static AnyFilter anyIngredient(String productCategory, String... other) {
        return new AnyFilter(toList(productCategory, other), 1, Type.INGREDIENTS);
    }

    public static AnyFilter anyIngredient(List<String> productCategories) {
        return new AnyFilter(productCategories, 1, Type.INGREDIENTS);
    }

    public static AnyFilter anyDish(String dishName, String... other) {
        return new AnyFilter(toList(dishName, other), 1, Type.DISHES);
    }

    public static AnyFilter anyDish(List<String> dishNames) {
        return new AnyFilter(dishNames, 1, Type.DISHES);
    }

    public static AnyFilter anyMenu(String menuName, String... other) {
        return new AnyFilter(toList(menuName, other), 1, Type.MENUS);
    }

    public static AnyFilter anyMenu(List<String> menuNames) {
        return new AnyFilter(menuNames, 1, Type.MENUS);
    }

    public static MinTagsFilter minTags(Tag a, Tag... other) {
        return new MinTagsFilter(toList(a, other));
    }

    /**
     * Создает и возвращает новый объект MinTagsFilter содержащий указанные теги.
     * @param values теги для которых определяется создаваемое ограничение MinTagsFilter.
     * @return новый объект MinTagsFilter.
     * @throws ValidateException если выполняется одно из следующих условий:<br/>
     *          1. если хотя бы один из элементов имеет значение null.<br/>
     *          2. если передаваемый список имеет значение null.<br/>
     *          3. если передаваемый список пустой.
     */
    public static MinTagsFilter minTags(List<Tag> values) {
        return new MinTagsFilter(values);
    }

    public static UserFilter user(UUID userId) {
        return new UserFilter(userId);
    }

    public static QuantityFilter less(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.LESS);
    }

    public static QuantityFilter lessOrEqual(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.LESS_OR_EQUAL);
    }

    public static QuantityFilter greater(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.GREATER);
    }

    public static QuantityFilter greaterOrEqual(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.GREATER_OR_EQUAL);
    }

    public static QuantityFilter equal(BigDecimal quantity) {
        return new QuantityFilter(quantity, QuantityFilter.Relative.EQUAL);
    }


    private static List<String> toList(String a, String... other) {
        ArrayList<String> result = new ArrayList<>();
        result.add(a);
        result.addAll(Arrays.asList(other));
        return result;
    }

    private static List<Tag> toList(Tag a, Tag... other) {
        ArrayList<Tag> result = new ArrayList<>();
        result.add(a);
        result.addAll(Arrays.asList(other));
        return result;
    }

    private static List<Filter> toList(Filter a, Filter b, Filter... other) {
        ArrayList<Filter> result = new ArrayList<>();
        result.add(a);
        result.add(b);
        result.addAll(Arrays.asList(other));
        return result;
    }


    public Type getType();

    public ImmutableList<Filter> getOperands();

    /**
     * Возвращает непосредственного родителя для данного фильтра. Если фильтр является корнем дерева
     * фильтров - возвращает null.
     * @return ближайшего родителя для данного фильтра или null.
     */
    public Filter getParent();

    public default boolean typeIs(Type type) {
        return getType() == type;
    }

    public default boolean typeIsOneOf(Type... types) {
        boolean result = false;
        for(int i = 0; i < types.length && !result; ++i) {
            result = getType() == types[i];
        }
        return result;
    }

    public boolean containsMax(Type... types);

    public boolean containsMax(int maxMatch, Type... types);

    public boolean containsMax(int maxMatch, int maxDepth, Type... types);

    public boolean containsExactly(Type... types);

    public boolean containsExactly(int matchNumber, Type... types);

    public boolean containsExactly(int matchNumber, int maxDepth, Type... types);

    public boolean containsMin(Type... types);

    public boolean containsMin(int minMatch, Type... types);

    public boolean containsMin(int minMatch, int maxDepth, Type... types);

    /**
     * Возвращает Stream из отдельных фильтров в порядке соотвествующим обходу дерева фильтров в ширину.
     */
    public default Stream<IterableFilter> bfs() {
        final IterableFilter firstItem = new IterableFilter(this, 0);

        UnaryOperator<IterableFilter> filterIterator = new UnaryOperator<>() {
            private final ArrayDeque<IterableFilter> queue = new ArrayDeque<>();

            @Override
            public IterableFilter apply(IterableFilter iterableFilter) {
                IterableFilter result = null;
                List<Filter> operands = iterableFilter.filter().getOperands();
                for(int i = 0; i < operands.size(); i++) {
                    IterableFilter addedFilter = new IterableFilter(
                            operands.get(i), iterableFilter.depth() + 1);
                    queue.addFirst(addedFilter);
                }

                if(!queue.isEmpty()) {
                    result = queue.removeLast();
                }
                return result;
            }
        };

        return Stream.iterate(firstItem, Objects::nonNull, filterIterator);
    }

    /**
     * Возвращает Stream из отдельных фильтров в порядке соотвествующим обходу дерева фильтров в глубину.
     */
    public default Stream<IterableFilter> dfs() {
        final IterableFilter firstItem = new IterableFilter(this, 0);

        UnaryOperator<IterableFilter> filterIterator = new UnaryOperator<>() {
            private final ArrayDeque<IterableFilter> stack = new ArrayDeque<>();

            @Override
            public IterableFilter apply(IterableFilter iterableFilter) {
                IterableFilter result = null;
                List<Filter> operands = iterableFilter.filter().getOperands();
                for(int i = operands.size() - 1; i >= 0; i--) {
                    IterableFilter addedFilter = new IterableFilter(
                            operands.get(i), iterableFilter.depth() + 1);
                    stack.addFirst(addedFilter);
                }

                if(!stack.isEmpty()) {
                    result = stack.removeFirst();
                }
                return result;
            }
        };

        return Stream.iterate(firstItem, Objects::nonNull, filterIterator);
    }

    public int getDepth();

    public default <T extends Filter> Optional<T> findAny(Type type) {
        Filter result = type == getType() ? this : null;

        for(int i = 0; i < getOperands().size() && result == null; i++) {
            result = getOperands().get(i).findAny(type).orElse(null);
        }

        return Optional.ofNullable((T)result);
    }

    public default <T extends Filter> Optional<T> findFirstDirectChild(Type type) {
        return (Optional<T>) getOperands().stream().
                filter(filter -> filter.getType() == type).
                findFirst();
    }

    /**
     * Находит и возвращает ближайшего родителя имеющего указанный тип. Если среди родителей
     * данного фильтра нет родителя с указанным типом - возвращает пустой Optional.
     * @param type тип искомого родителя
     * @return ближайшего родителя имеющего указанный тип.
     */
    public default <T extends Filter> Optional<T> findFirstParent(Type type) {
        Filter filter = getParent();
        while(filter != null && filter.getType() != type) filter = filter.getParent();
        return Optional.ofNullable((T)filter);
    }

    /**
     * Проверяет - находится ли текущий фильтр в дизъюнктивной нормальной форме (ДНФ).
     * @return true - если фильтр находится в ДНФ, false - в противном случае.
     */
    public default boolean isDnf() {
        if(!typeIsOneOf(Type.AND, Type.OR)) {
            return true;
        } else if(typeIs(Type.AND) && getOperands().stream().anyMatch(f -> f.typeIs(Type.OR))) {
            return false;
        } else {
            boolean result = true;
            for(int i = 0; i < getOperands().size(); i++) {
                result &= getOperands().get(i).isDnf();
            }
            return result;
        }
    }

    /**
     * Создает и возвращает новый фильтр представляющий дизъюнктивную нормальную форму текущего
     * фильтра.
     */
    public default Filter toDnf() {
        Filter result = this;

        if(typeIs(Type.OR)) {
            result = or(getOperands().stream().map(Filter::toDnf).toList());
        } else if(typeIs(Type.AND)) {
            ArrayList<Filter> operands = getOperands().stream().
                    map(Filter::toDnf).
                    collect(Collectors.toCollection(ArrayList::new));

            int orFilterIndex = 0;
            while(orFilterIndex < operands.size() && !operands.get(orFilterIndex).typeIs(Type.OR)) ++orFilterIndex;

            if(orFilterIndex < operands.size()) {
                ArrayList<Filter> andFilters = new ArrayList<>();
                Filter orFilter = operands.remove(orFilterIndex);
                for(int i = 0; i < orFilter.getOperands().size(); i++) {
                    ArrayList<Filter> andFilterOperands = new ArrayList<>(operands);
                    andFilterOperands.add(orFilter.getOperands().get(i));
                    andFilters.add(and(andFilterOperands).toDnf());
                }
                result = or(andFilters);
            } else {
                result = and(operands);
            }
        }

        return result;
    }

    /**
     * Возвращает строковое представление данного фильтра в плоском виде: каждый фильтр
     * указан на отдельной строке, порядок фильтров в строке соответствует порядоку обхода дерева
     * фильтров в глубину.
     * @return строковое представление данного фильтра.
     */
    public default String toPrettyString() {
        return dfs().map(
                iterableFilter -> "-".repeat(iterableFilter.depth()) +
                        (iterableFilter.filter().typeIsOneOf(Type.AND, Type.OR) ?
                                iterableFilter.filter().getType() :
                                iterableFilter.filter()) +
                System.lineSeparator()).
                reduce(String::concat).
                orElseThrow();
    }

}
