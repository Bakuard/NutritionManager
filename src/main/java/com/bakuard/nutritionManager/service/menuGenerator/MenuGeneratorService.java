package com.bakuard.nutritionManager.service.menuGenerator;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.MenuItem;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;
import it.ssc.pl.milp.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

import static com.bakuard.nutritionManager.validation.Rule.failure;

public class MenuGeneratorService {

    private AppConfigData appConfigData;

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
        List<Input.DishMinPrice> dishMinPrices = input.getAllDishMinPrices();
        List<Input.DishTagConstraint> dishTagConstraints = input.getConstraintsByAllDishTags();
        List<Input.ProductConstraint> productConstraints = input.getConstraintsByAllProducts();

        double[] goal = dishMinPrices.stream().
                mapToDouble(dmp -> dmp.minPrice().doubleValue()).
                toArray();
        double[] constraintConst = DoubleStream.concat(
                dishTagConstraints.stream().mapToDouble(tc -> tc.quantity().doubleValue()),
                productConstraints.stream().mapToDouble(pc -> pc.quantity().doubleValue())
        ).toArray();
        double[][] constraintVectors = new double[constraintConst.length][goal.length];
        for(int i = 0; i < constraintVectors.length; i++) {
            for(int j = 0; j < constraintVectors[i].length; j++) {
                if(i < dishTagConstraints.size()) {
                    constraintVectors[i][j] =
                            input.hasTag(dishMinPrices.get(j).dish(), dishTagConstraints.get(i).dishTag()) ? 1.0 : 0.0;
                } else {
                    int l = i - dishTagConstraints.size();
                    constraintVectors[l][j] = input.getQuantity(
                            dishMinPrices.get(j).dish(), productConstraints.get(l).productCategory()
                    ).doubleValue();
                }
            }
        }

        Variable[] result = new Variable[0];
        try {
            LinearObjectiveFunction function = new LinearObjectiveFunction(goal, GoalType.MIN);
            ArrayList<Constraint> constraints = new ArrayList<>();
            for(int i = 0; i < constraintVectors.length; i++) {
                Constraint c = new Constraint(constraintVectors[i], ConsType.GE, constraintConst[i]);
                constraints.add(c);
            }
            double[] intParams = new double[goal.length];
            Arrays.fill(intParams, 1.0);
            constraints.add(new Constraint(intParams, ConsType.INT, Double.NaN));

            MILP milp = new MILP(function, constraints);
            milp.resolve();
            Solution solution = milp.getSolution();
            result = solution.getVariables();
        } catch(Exception e) {
            Validator.check("MenuGeneratorService.generate",
                    failure(com.bakuard.nutritionManager.validation.Constraint.SOLUTION_EXISTS));
        }

        Menu.Builder menuBuilder = new Menu.Builder().
                generateId().
                setUser(input.getUser()).
                setName(input.getGeneratedMenuName()).
                setConfig(appConfigData);
        for(int i = 0; i < result.length; i++) {
            menuBuilder.addItem(
                    new MenuItem.LoadBuilder().
                            generateId().
                            setDish(dishMinPrices.get(i).dish()).
                            setConfig(appConfigData).
                            setQuantity(BigDecimal.valueOf(result[i].getValue()))
            );
        }

        return menuBuilder.tryBuild();
    }

}
