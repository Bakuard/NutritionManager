package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.MenuItem;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;
import it.ssc.pl.milp.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.bakuard.nutritionManager.validation.Rule.failure;
import static com.bakuard.nutritionManager.validation.Rule.notNull;

public class MenuGeneratorService {

    private final AppConfigData appConfigData;

    public MenuGeneratorService(AppConfigData appConfigData) {
        this.appConfigData = appConfigData;
    }

    /**
     * Подбирает новое меню. При подборе нового меню используется критерий минимальной суммарной стоимости
     * продуктов необходимых для приготовления всех блюд этого меню. Входными данными для данного метода является
     * набор ограничений передаваемых этому методу в виде объекта input. <br/><br/>
     * <strong>ВАЖНО!</strong> В задачи этого метода НЕ входит сохранение созданного им меню.
     * @param input набор ограничений для подбираемого меню (подробнее см. {@link Input}).
     * @return новое меню.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Если input равен null. <br/>
     *         2. Если невозможно подобрать меню с заданными ограничениями. <br/>
     */
    public Menu generate(Input input) {
        Validator.check("MenuGeneratorService.input", notNull(input));

        try {
            LinearObjectiveFunction goal = goal(input);

            ArrayList<Constraint> allConstraints = new ArrayList<>();
            allConstraints.add(minServingNumberConstraint(input));
            allConstraints.add(intConstraint(input));
            allConstraints.addAll(quantityConstraints(input));

            MILP milp = new MILP(goal, allConstraints);
            milp.resolve();
            Solution solution = milp.getSolution();
            Variable[] result = solution.getVariables();

            return menu(result, input);
        } catch(Exception e) {
            throw new ValidateException("Fail to generate menu").
                    addReason(e).
                    addReason(Rule.of("MenuGeneratorService.generate",
                                    failure(com.bakuard.nutritionManager.validation.Constraint.SOLUTION_EXISTS)));
        }
    }


    private Menu menu(Variable[] result, Input input) {
        Menu.Builder menuBuilder = new Menu.Builder().
                generateId().
                setUser(input.getUser()).
                setName(input.getGeneratedMenuName()).
                setConfig(appConfigData);
        for(int i = 0; i < result.length; i++) {
            if(Math.signum(result[i].getValue()) > 0) {
                menuBuilder.addItem(
                        new MenuItem.LoadBuilder().
                                generateId().
                                setDish(input.getAllDishMinPrices().get(i).dish()).
                                setConfig(appConfigData).
                                setQuantity(BigDecimal.valueOf(result[i].getValue()))
                );
            }
        }

        return menuBuilder.tryBuild();
    }

    private LinearObjectiveFunction goal(Input input) throws LPException {
        double[] goal = input.getAllDishMinPrices().stream().
                mapToDouble(dmp -> dmp.minPrice().doubleValue()).
                toArray();

        return new LinearObjectiveFunction(goal, GoalType.MIN);
    }

    private List<Constraint> quantityConstraints(Input input) throws SimplexException {
        List<Constraint> result = new ArrayList<>();

        List<Input.ProductConstraint> productConstraints = input.getConstraintsByAllProducts();
        List<Input.DishTagConstraint> dishTagConstraints = input.getConstraintsByAllDishTags();
        int quantityConstraintsNumber = productConstraints.size() + dishTagConstraints.size();
        int variablesNumber = input.getAllDishMinPrices().size();

        for(int i = 0; i < quantityConstraintsNumber; i++) {
            double[] vector = new double[variablesNumber];
            double quantity = 0.0;
            ConsType consType = ConsType.GE;

            for(int j = 0; j < variablesNumber; j++) {
                if (i < productConstraints.size()) {
                    Input.ProductConstraint product = productConstraints.get(i);
                    vector[j] = input.getQuantity(
                            input.getAllDishMinPrices().get(j).dish(),
                            product.productCategory()
                    ).doubleValue();

                    quantity = product.quantity().doubleValue();
                    consType = toConsType(product.relation());
                } else {
                    int l = i - productConstraints.size();
                    Input.DishTagConstraint dishTag = dishTagConstraints.get(l);
                    vector[j] = input.hasTag(
                            input.getAllDishMinPrices().get(j).dish(),
                            dishTag.dishTag()
                    ) ? 1.0 : 0.0;

                    quantity = dishTag.quantity().doubleValue();
                    consType = toConsType(dishTag.relation());
                }
            }

            result.add(new Constraint(vector, consType, quantity));
        }

        return result;
    }

    private Constraint intConstraint(Input input) throws SimplexException {
        double[] vector = new double[input.getAllDishMinPrices().size()];
        Arrays.fill(vector, 1.0);

        return new Constraint(vector, ConsType.INT, Double.NaN);
    }

    private Constraint minServingNumberConstraint(Input input) throws SimplexException {
        double[] vector = new double[input.getAllDishMinPrices().size()];
        Arrays.fill(vector, 1.0);

        return new Constraint(vector, ConsType.GE, input.getMinServingNumber().doubleValue());
    }

    private ConsType toConsType(Relationship relation) {
        ConsType result = ConsType.GE;
        switch(relation) {
            case GREATER_OR_EQUAL -> result = ConsType.GE;
            case LESS_OR_EQUAL -> result = ConsType.LE;
        }
        return result;
    }

}
