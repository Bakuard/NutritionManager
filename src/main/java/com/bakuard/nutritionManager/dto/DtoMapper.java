package com.bakuard.nutritionManager.dto;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.*;
import com.bakuard.nutritionManager.dto.auth.JwsResponse;
import com.bakuard.nutritionManager.dto.dishes.*;
import com.bakuard.nutritionManager.dto.exceptions.ConstraintResponse;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.SuccessResponse;
import com.bakuard.nutritionManager.dto.menus.*;
import com.bakuard.nutritionManager.dto.products.ProductAddRequest;
import com.bakuard.nutritionManager.dto.products.ProductFieldsResponse;
import com.bakuard.nutritionManager.dto.products.ProductResponse;
import com.bakuard.nutritionManager.dto.products.ProductUpdateRequest;
import com.bakuard.nutritionManager.dto.users.UserResponse;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.filters.AnyFilter;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.MinTagsFilter;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.PageableByNumber;
import com.bakuard.nutritionManager.validation.Constraint;
import com.bakuard.nutritionManager.validation.RuleException;
import com.bakuard.nutritionManager.validation.ValidateException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

public class DtoMapper {

    private UserRepository userRepository;
    private ProductRepository productRepository;
    private DishRepository dishRepository;
    private MenuRepository menuRepository;
    private AppConfigData appConfiguration;
    private MessageSource messageSource;

    public DtoMapper(UserRepository userRepository,
                     ProductRepository productRepository,
                     DishRepository dishRepository,
                     MenuRepository menuRepository,
                     MessageSource messageSource,
                     AppConfigData appConfiguration) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.dishRepository = dishRepository;
        this.menuRepository = menuRepository;
        this.messageSource = messageSource;
        this.appConfiguration = appConfiguration;
    }

    public ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setUser(toUserResponse(product.getUser()));
        response.setCategory(product.getContext().getCategory());
        response.setShop(product.getContext().getShop());
        response.setGrade(product.getContext().getGrade());
        response.setManufacturer(product.getContext().getManufacturer());
        response.setPrice(product.getContext().getPrice());
        response.setPackingSize(product.getContext().getPackingSize());
        response.setUnit(product.getContext().getUnit());
        response.setQuantity(product.getQuantity());
        response.setDescription(product.getDescription());
        response.setImageUrl(product.getImageUrl());
        response.setTags(toTagsResponse(product.getContext().getTags()));
        return response;
    }

    public Product toProduct(UUID userId, ProductUpdateRequest dto) {
        Product.Builder builder = new Product.Builder().
                setAppConfiguration(appConfiguration).
                setId(dto.getId()).
                setUser(userRepository.tryGetById(userId)).
                setCategory(dto.getCategory()).
                setShop(dto.getShop()).
                setGrade(dto.getGrade()).
                setManufacturer(dto.getManufacturer()).
                setPrice(dto.getPrice()).
                setPackingSize(dto.getPackingSize()).
                setUnit(dto.getUnit()).
                setQuantity(dto.getQuantity()).
                setDescription(dto.getDescription()).
                setImageUrl(dto.getImageUrl());

        dto.getTags().forEach(builder::addTag);

        return builder.tryBuild();
    }

    public Product toProduct(UUID userId, ProductAddRequest dto) {
        Product.Builder builder = new Product.Builder().
                setAppConfiguration(appConfiguration).
                generateId().
                setUser(userRepository.tryGetById(userId)).
                setCategory(dto.getCategory()).
                setShop(dto.getShop()).
                setGrade(dto.getGrade()).
                setManufacturer(dto.getManufacturer()).
                setPrice(dto.getPrice()).
                setPackingSize(dto.getPackingSize()).
                setUnit(dto.getUnit()).
                setQuantity(dto.getQuantity()).
                setDescription(dto.getDescription()).
                setImageUrl(dto.getImageUrl());

        dto.getTags().forEach(builder::addTag);

        return builder.tryBuild();
    }

    public Page<ProductResponse> toProductsResponse(Page<Product> products) {
        return products.map(this::toProductResponse);
    }


    public DishResponse toDishResponse(Dish dish) {
        DishResponse response = new DishResponse();
        response.setId(dish.getId());
        response.setUser(toUserResponse(dish.getUser()));
        response.setName(dish.getName());
        response.setServingSize(dish.getServingSize());
        response.setUnit(dish.getUnit());
        response.setDescription(dish.getDescription());
        response.setImageUrl(dish.getImageUrl());
        response.setTags(toTagsResponse(dish.getTags()));

        List<DishIngredient> ingredients = dish.getIngredients();
        response.setIngredients(
                IntStream.range(0, ingredients.size()).
                        mapToObj(i -> toIngredientResponse(ingredients.get(i), i)).
                        toList()
        );

        return response;
    }

    public Dish toDish(UUID userId, DishAddRequest dto) {
        User user = userRepository.tryGetById(userId);

        Dish.Builder builder = new Dish.Builder().
                generateId().
                setUser(user).
                setName(dto.getName()).
                setServingSize(dto.getServingSize()).
                setUnit(dto.getUnit()).
                setDescription(dto.getDescription()).
                setImageUrl(dto.getImageUrl()).
                setConfig(appConfiguration).
                setRepository(productRepository);

        IntStream.range(0, dto.getIngredients().size()).
                forEach(i -> {
                    IngredientAddRequest ingredient = dto.getIngredients().get(i);
                    builder.addIngredient(toDishIngredient(userId, ingredient, i));
                });

        dto.getTags().forEach(builder::addTag);

        return builder.tryBuild();
    }

    public Dish toDish(UUID userId, DishUpdateRequest dto) {
        User user = userRepository.tryGetById(userId);

        Dish.Builder builder = new Dish.Builder().
                setId(dto.getId()).
                setUser(user).
                setName(dto.getName()).
                setServingSize(dto.getServingSize()).
                setUnit(dto.getUnit()).
                setDescription(dto.getDescription()).
                setImageUrl(dto.getImageUrl()).
                setConfig(appConfiguration).
                setRepository(productRepository);

        IntStream.range(0, dto.getIngredients().size()).
                forEach(i -> {
                    IngredientUpdateRequest ingredient = dto.getIngredients().get(i);
                    builder.addIngredient(toDishIngredient(userId, ingredient, i));
                });

        dto.getTags().forEach(builder::addTag);

        return builder.tryBuild();
    }

    public Page<DishForListResponse> toDishesResponse(Page<Dish> dishes) {
        return dishes.map(this::toDishForListResponse);
    }

    public DishProductsResponse toDishProductsResponse(UUID userId, UUID dishId, BigDecimal servingNumber) {
        Dish dish = dishRepository.tryGetById(userId, dishId);
        return toDishProductsResponse(dish, servingNumber == null ? BigDecimal.ONE : servingNumber);
    }

    public Optional<BigDecimal> toDishPrice(UUID userId, DishPriceRequest dto) {
        Dish dish = dishRepository.tryGetById(userId, dto.getDishId());

        List<Dish.ProductConstraint> indexes = dto.getProducts().stream().
                map(pr -> new Dish.ProductConstraint(pr.getIngredientIndex(), pr.getProductIndex())).
                toList();
        List<Dish.IngredientProduct> ingredients = dish.getProductForEachIngredient(indexes);

        return dish.getLackProductPrice(ingredients, dto.getServingNumber());
    }


    public MenuResponse toMenuResponse(Menu menu) {
        MenuResponse response = new MenuResponse();
        response.setId(menu.getId());
        response.setUser(toUserResponse(menu.getUser()));
        response.setName(menu.getName());
        response.setImageUrl(menu.getImageUrl());
        response.setDescription(menu.getDescription());
        response.setItems(
                IntStream.range(0, menu.getMenuItemNumbers()).
                        mapToObj(i -> toMenuItemResponse(menu.tryGetItem(i), i)).
                        toList()
        );
        response.setTags(toTagsResponse(menu.getTags()));
        return response;
    }

    public Menu toMenu(UUID userId, MenuAddRequest dto) {
        User user = userRepository.tryGetById(userId);

        Menu.Builder builder = new Menu.Builder().
                generateId().
                setUser(user).
                setName(dto.getName()).
                setDescription(dto.getDescription()).
                setImageUrl(dto.getImageUrl()).
                setConfig(appConfiguration);

        dto.getTags().forEach(builder::addTag);

        dto.getItems().forEach(item -> builder.addItem(toMenuItem(userId, item)));

        return builder.tryBuild();
    }

    public Menu toMenu(UUID userId, MenuUpdateRequest dto) {
        User user = userRepository.tryGetById(userId);

        Menu.Builder builder = new Menu.Builder().
                setId(dto.getId()).
                setUser(user).
                setName(dto.getName()).
                setDescription(dto.getDescription()).
                setImageUrl(dto.getImageUrl()).
                setConfig(appConfiguration);

        dto.getTags().forEach(builder::addTag);

        dto.getItems().forEach(item -> builder.addItem(toMenuItem(userId, item)));

        return builder.tryBuild();
    }

    public Page<MenuForListResponse> toMenusResponse(Page<Menu> menus) {
        return menus.map(this::toMenuForListResponse);
    }

    public MenuDishProductsListResponse toMenuDishProductsListResponse(UUID userId,
                                                                       UUID menuId,
                                                                       String dishName,
                                                                       BigDecimal quantity) {
        Menu menu = menuRepository.tryGetById(userId, menuId);
        Optional<BigDecimal> quantityOp = Optional.ofNullable(quantity);

        MenuDishProductsListResponse response = new MenuDishProductsListResponse();
        response.setMenuId(menuId);
        response.setMenuName(menu.getName());
        response.setMenuQuantity(quantityOp.orElse(BigDecimal.ONE));
        response.setDishes(menu.getItems().stream().
                map(this::toDishNameAndIdResponse).
                toList());
        if(dishName != null) {
            MenuItem menuItem = menu.tryGetItem(dishName);
            BigDecimal dishQuantity = menuItem.getNecessaryQuantity(quantityOp.orElse(BigDecimal.ONE));
            response.setDishProducts(toDishProductsResponse(menuItem.getDish(), dishQuantity));
        } else if(menu.getMenuItemNumbers() > 0) {
            MenuItem menuItem = menu.getItems().get(0);
            BigDecimal dishQuantity = menuItem.getNecessaryQuantity(quantityOp.orElse(BigDecimal.ONE));
            response.setDishProducts(toDishProductsResponse(menuItem.getDish(), dishQuantity));
        }

        return response;
    }

    public MenuDishesProductsListResponse toMenuDishesProductsListResponse(UUID userId,
                                                                           UUID menuId,
                                                                           BigDecimal quantity) {
        Menu menu = menuRepository.tryGetById(userId, menuId);
        Optional<BigDecimal> quantityOp = Optional.ofNullable(quantity);

        MenuDishesProductsListResponse response = new MenuDishesProductsListResponse();
        response.setMenuId(menuId);
        response.setMenuName(menu.getName());
        response.setMenuQuantity(quantityOp.orElse(BigDecimal.ONE));
        response.setDishesProducts(
                menu.getItems().stream().
                        map(item -> toDishProductsResponse(
                                item.getDish(), item.getNecessaryQuantity(quantityOp.orElse(BigDecimal.ONE)))
                        ).
                        toList()
        );

        return response;
    }

    public Optional<BigDecimal> toMenuPrice(UUID userId, MenuPriceRequest dto) {
        Menu menu = menuRepository.tryGetById(userId, dto.getMenuId());

        List<Menu.ProductConstraint> constraints = dto.getProducts().stream().
                map(d -> new Menu.ProductConstraint(d.getDishName(), d.getIngredientIndex(), d.getProductIndex())).
                toList();
        List<Menu.MenuItemProduct> items = menu.getMenuItemProducts(constraints);
        return menu.getLackProductsPrice(items, dto.getQuantity());
    }


    public Criteria toProductCriteria(int page,
                                      int size,
                                      UUID userId,
                                      String sortRule,
                                      boolean onlyFridge,
                                      List<String> categories,
                                      List<String> shops,
                                      List<String> grades,
                                      List<String> manufacturers,
                                      List<String> tags) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.user(userId));
        if(onlyFridge) filters.add(Filter.greater(BigDecimal.ZERO));
        if(categories != null && !categories.isEmpty()) filters.add(Filter.anyCategory(categories));
        if(shops != null && !shops.isEmpty()) filters.add(Filter.anyShop(shops));
        if(grades != null && !grades.isEmpty()) filters.add(Filter.anyGrade(grades));
        if(manufacturers != null && !manufacturers.isEmpty()) filters.add(Filter.anyManufacturer(manufacturers));
        if(tags != null && !tags.isEmpty()) filters.add(Filter.minTags(toTags(tags)));

        Filter filter = null;
        if(filters.size() == 1) filter = filters.get(0);
        else filter = Filter.and(filters);

        return new Criteria().
                setPageable(PageableByNumber.of(size, page)).
                setSort(Sort.products(Arrays.asList(sortRule))).
                setFilter(filter);
    }

    public Criteria toDishCriteria(int page,
                                   int size,
                                   UUID userId,
                                   String sortRule,
                                   List<String> productCategories,
                                   List<String> tags) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.user(userId));

        if(productCategories != null && !productCategories.isEmpty()) {
            filters.add(Filter.anyIngredient(productCategories));
        }
        if(tags != null && !tags.isEmpty()) {
            filters.add(Filter.minTags(toTags(tags)));
        }

        Filter filter = null;
        if(filters.size() == 1) filter = filters.get(0);
        else filter = Filter.and(filters);

        return new Criteria().
                setPageable(PageableByNumber.of(size, page)).
                setSort(Sort.dishes(Arrays.asList(sortRule))).
                setFilter(filter);
    }

    public Criteria toMenuCriteria(int page,
                                   int size,
                                   UUID userId,
                                   String sortRule,
                                   List<String> dishNames,
                                   List<String> tags) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.user(userId));

        if(dishNames != null && !dishNames.isEmpty()) {
            filters.add(Filter.anyDish(dishNames));
        }
        if(tags != null && !tags.isEmpty()) {
            filters.add(Filter.minTags(toTags(tags)));
        }

        Filter filter = null;
        if(filters.size() == 1) filter = filters.get(0);
        else filter = Filter.and(filters);

        return new Criteria().
                setPageable(PageableByNumber.of(size, page)).
                setSort(Sort.menus(Arrays.asList(sortRule))).
                setFilter(filter);
    }


    public ProductFieldsResponse toProductFieldsResponse(UUID userId) {
        Criteria criteria = new Criteria().
                setPageable(PageableByNumber.of(1000, 0)).
                setFilter(Filter.user(userId));

        Page<String> manufacturers = productRepository.getManufacturers(criteria);
        Page<String> grades = productRepository.getGrades(criteria);
        Page<String> shops = productRepository.getShops(criteria);
        Page<String> categories = productRepository.getCategories(criteria);
        Page<Tag> tags = productRepository.getTags(criteria);

        ProductFieldsResponse response = new ProductFieldsResponse();
        response.setTags(tags.getContent().stream().map(t -> new FieldResponse(t.getValue())).toList());
        response.setGrades(grades.getContent().stream().map(FieldResponse::new).toList());
        response.setManufacturers(manufacturers.getContent().stream().map(FieldResponse::new).toList());
        response.setShops(shops.getContent().stream().map(FieldResponse::new).toList());
        response.setCategories(categories.getContent().stream().map(FieldResponse::new).toList());

        return response;
    }

    public DishFieldsResponse toDishFieldsResponse(UUID userId) {
        Criteria criteria = new Criteria().
                setPageable(PageableByNumber.of(1000, 0)).
                setFilter(Filter.user(userId));

        Page<Tag> tags = dishRepository.getTags(criteria);
        Page<String> units = dishRepository.getUnits(criteria);
        Page<String> names = dishRepository.getNames(criteria);
        Page<String> categories = productRepository.getCategories(criteria);

        DishFieldsResponse response = new DishFieldsResponse();
        response.setDishTags(tags.getContent().stream().map(t -> new FieldResponse(t.getValue())).toList());
        response.setDishUnits(units.getContent().stream().map(FieldResponse::new).toList());
        response.setDishNames(names.getContent().stream().map(FieldResponse::new).toList());
        response.setProductCategories(categories.getContent().stream().map(FieldResponse::new).toList());

        return response;
    }

    public MenuFieldsResponse toMenuFieldsResponse(UUID userId) {
        Criteria criteria = new Criteria().
                setPageable(PageableByNumber.of(1000, 0)).
                setFilter(Filter.user(userId));

        Page<Tag> menuTags = menuRepository.getTags(criteria);
        Page<String> menuNames = menuRepository.getNames(criteria);
        Page<String> dishNames = dishRepository.getNames(criteria);

        MenuFieldsResponse response = new MenuFieldsResponse();
        response.setMenuTags(menuTags.getContent().stream().map(t -> new FieldResponse(t.getValue())).toList());
        response.setMenuNames(menuNames.getContent().stream().map(FieldResponse::new).toList());
        response.setDishNames(dishNames.getContent().stream().map(FieldResponse::new).toList());

        return response;
    }


    public <T> SuccessResponse<T> toSuccessResponse(String keyMessage, T body) {
        return new SuccessResponse<>(
                getMessage(keyMessage, "Success"),
                getMessage("successTitle", "Success"),
                body
        );
    }

    public JwsResponse toJwsResponse(String jws, User user) {
        JwsResponse dto = new JwsResponse();
        dto.setJws(jws);
        dto.setUser(toUserResponse(user));
        return dto;
    }

    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
    }


    public ExceptionResponse toExceptionResponse(HttpStatus httpStatus, String keyMessage) {
        ExceptionResponse response = new ExceptionResponse(httpStatus);
        ConstraintResponse constraintResponse = new ConstraintResponse();
        constraintResponse.setTitle(getMessage("constraintTitle", "Reason"));
        constraintResponse.setMessage(getMessage(keyMessage, "Unexpected exception"));
        response.addReason(constraintResponse);
        return response;
    }

    public ExceptionResponse toExceptionResponse(ValidateException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        if(e.containsConstraint(Constraint.CORRECT_JWS) || e.containsConstraint(Constraint.CORRECT_CREDENTIALS)) {
            httpStatus = HttpStatus.FORBIDDEN;
        } else if(e.containsConstraint(Constraint.ENTITY_MUST_EXISTS_IN_DB)) {
            httpStatus = HttpStatus.NOT_FOUND;
        }

        ExceptionResponse response = new ExceptionResponse(httpStatus);
        e.forEach(constraint -> response.addReason(toConstraintResponse(constraint)));
        return response;
    }


    private ConstraintResponse toConstraintResponse(RuleException ruleException) {
        ConstraintResponse dto = new ConstraintResponse();
        dto.setTitle(getMessage("constraintTitle", "Reason"));
        dto.setMessage(getMessage(ruleException.getUserMessageKey(), ruleException.getMessage()));
        return dto;
    }

    private String getMessage(String key, String defaultValue) {
        return messageSource.getMessage(
                key, null, defaultValue, LocaleContextHolder.getLocale()
        );
    }


    private List<String> toTagsResponse(Collection<Tag> tags) {
        return tags.stream().map(Tag::getValue).toList();
    }

    private List<Tag> toTags(List<String> tags) {
        return tags.stream().map(Tag::new).toList();
    }

    private ProductAsDishIngredientResponse toProductAsDishIngredientResponse(Dish dish,
                                                                              Dish.IngredientProduct ingredientProduct,
                                                                              boolean isChecked,
                                                                              BigDecimal servingNumber) {
        Product product = ingredientProduct.product().orElseThrow();

        ProductAsDishIngredientResponse response = new ProductAsDishIngredientResponse();
        response.setId(product.getId());
        response.setUser(toUserResponse(product.getUser()));
        response.setImageUrl(product.getImageUrl());
        response.setCategory(product.getContext().getCategory());
        response.setShop(product.getContext().getShop());
        response.setGrade(product.getContext().getGrade());
        response.setManufacturer(product.getContext().getManufacturer());
        response.setPrice(product.getContext().getPrice());
        response.setPackingSize(product.getContext().getPackingSize());
        response.setUnit(product.getContext().getUnit());
        response.setQuantity(product.getQuantity());
        response.setNecessaryQuantity(
                dish.tryGetIngredient(ingredientProduct.ingredientIndex()).getNecessaryQuantity(servingNumber)
        );
        response.setLackQuantity(
                dish.getLackPackageQuantity(ingredientProduct, servingNumber).orElseThrow()
        );
        response.setLackQuantityPrice(
                dish.getLackPackageQuantityPrice(ingredientProduct, servingNumber).orElseThrow()
        );
        response.setTags(toTagsResponse(product.getContext().getTags()));
        response.setChecked(isChecked);
        response.setProductIndex(ingredientProduct.productIndex());
        return response;
    }

    private DishForListResponse toDishForListResponse(Dish dish) {
        DishForListResponse response = new DishForListResponse();
        response.setId(dish.getId());
        response.setImageUrl(dish.getImageUrl());
        response.setName(dish.getName());
        response.setServingSize(dish.getServingSize());
        response.setUnit(dish.getUnit());
        response.setAveragePrice(dish.getAveragePrice().orElse(null));
        response.setTags(toTagsResponse(dish.getTags()));
        return response;
    }

    private DishIngredient.Builder toDishIngredient(UUID userId, IngredientUpdateRequest dto, int index) {
        return new DishIngredient.Builder().
                setOrGenerateId(dto.getId()).
                setConfig(appConfiguration).
                setName("Ингредиент №" + index + " - " + dto.getFilter().getCategory()).
                setQuantity(dto.getQuantity()).
                setFilter(toDishIngredientFilter(userId, dto.getFilter()));
    }

    private DishIngredient.Builder toDishIngredient(UUID userId, IngredientAddRequest dto, int index) {
        return new DishIngredient.Builder().
                generateId().
                setConfig(appConfiguration).
                setName("Ингредиент №" + index + " - " + dto.getFilter().getCategory()).
                setQuantity(dto.getQuantity()).
                setFilter(toDishIngredientFilter(userId, dto.getFilter()));
    }

    private Filter toDishIngredientFilter(UUID userId, IngredientFilterRequestResponse dto) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.user(userId));
        filters.add(Filter.anyCategory(dto.getCategory()));
        if(dto.getGrades() != null && !dto.getGrades().isEmpty()) {
            filters.add(Filter.anyGrade(dto.getGrades()));
        }
        if(dto.getShops() != null && !dto.getShops().isEmpty()) {
            filters.add(Filter.anyShop(dto.getShops()));
        }
        if(dto.getManufacturers() != null && !dto.getManufacturers().isEmpty()) {
            filters.add(Filter.anyManufacturer(dto.getManufacturers()));
        }
        if(dto.getTags() != null && !dto.getTags().isEmpty()) {
            filters.add(Filter.minTags(toTags(dto.getTags())));
        }

        return Filter.and(filters);
    }

    private IngredientResponse toIngredientResponse(DishIngredient ingredient, int index) {
        IngredientResponse response = new IngredientResponse();
        response.setId(ingredient.getId());
        response.setIndex(index);
        response.setQuantity(ingredient.getNecessaryQuantity(BigDecimal.ONE));
        response.setFilter(toDishIngredientFilterRequestResponse(ingredient.getFilter()));
        return response;
    }

    private IngredientFilterRequestResponse toDishIngredientFilterRequestResponse(Filter filter) {
        IngredientFilterRequestResponse response = new IngredientFilterRequestResponse();

        ArrayDeque<Filter> stack = new ArrayDeque<>();
        stack.addLast(filter);
        while(!stack.isEmpty()) {
            Filter temp = stack.removeLast();

            switch(temp.getType()) {
                case MIN_TAGS -> response.setTags(toTagsResponse(((MinTagsFilter)temp).getTags()));
                case CATEGORY -> response.setCategory(((AnyFilter)temp).getValues().get(0));
                case SHOPS -> response.setShops(((AnyFilter)temp).getValues());
                case GRADES -> response.setGrades(((AnyFilter)temp).getValues());
                case MANUFACTURER -> response.setManufacturers(((AnyFilter)temp).getValues());
            }

            temp.getOperands().forEach(stack::addLast);
        }

        return response;
    }

    private DishProductsResponse toDishProductsResponse(Dish dish, BigDecimal servingNumber) {
        DishProductsResponse response = new DishProductsResponse();
        response.setDishId(dish.getId());
        response.setDishName(dish.getName());
        response.setServingNumber(servingNumber);
        response.setCategories(
                IntStream.range(0, dish.getIngredientNumber()).
                        mapToObj(ingredientIndex -> toIngredientProductsResponse(dish, ingredientIndex, servingNumber)).
                        toList()
        );

        return response;
    }

    private IngredientProductsResponse toIngredientProductsResponse(Dish dish,
                                                                    int ingredientIndex,
                                                                    BigDecimal servingNumber) {
        DishIngredient ingredient = dish.getIngredient(ingredientIndex).orElseThrow();

        List<Dish.IngredientProduct> products = dish.getProducts(ingredientIndex, 0).
                orElseThrow().getContent();

        IngredientProductsResponse response = new IngredientProductsResponse();
        response.setIngredientIndex(ingredientIndex);
        response.setProductCategory(ingredient.getName());
        response.setProducts(
                products.stream().
                        map(ingredientProduct ->
                                toProductAsDishIngredientResponse(dish,
                                        ingredientProduct,
                                        ingredientProduct.productIndex() == 0,
                                        servingNumber)
                        ).
                        toList()
        );

        return response;
    }

    private DishNameAndIdResponse toDishNameAndIdResponse(MenuItem item) {
        DishNameAndIdResponse response = new DishNameAndIdResponse();
        response.setDishId(item.getDish().getId());
        response.setDishName(item.getDishName());
        return response;
    }

    private ItemResponse toMenuItemResponse(MenuItem item, int itemIndex) {
        ItemResponse result = new ItemResponse();
        result.setId(item.getId());
        result.setDishName(item.getDishName());
        result.setServingNumber(item.getNecessaryQuantity(BigDecimal.ONE));
        result.setItemIndex(itemIndex);
        return result;
    }

    private MenuItem.Builder toMenuItem(UUID userId, ItemUpdateRequest dto) {
        return new MenuItem.Builder().
                setOrGenerateId(dto.getId()).
                setDishName(dto.getDishName()).
                setQuantity(dto.getServingNumber()).
                setConfig(appConfiguration).
                setRepository(dishRepository).
                setUserId(userId);
    }

    private MenuItem.Builder toMenuItem(UUID userId, ItemAddRequest dto) {
        return new MenuItem.Builder().
                generateId().
                setDishName(dto.getDishName()).
                setQuantity(dto.getServingNumber()).
                setConfig(appConfiguration).
                setRepository(dishRepository).
                setUserId(userId);
    }

    private MenuForListResponse toMenuForListResponse(Menu menu) {
        MenuForListResponse response = new MenuForListResponse();
        response.setId(menu.getId());
        response.setName(menu.getName());
        response.setAveragePrice(menu.getAveragePrice().orElse(null));
        response.setImageUrl(menu.getImageUrl());
        response.setItems(
                IntStream.range(0, menu.getMenuItemNumbers()).
                        mapToObj(i -> toMenuItemResponse(menu.tryGetItem(i), i)).
                        toList()
        );
        response.setTags(toTagsResponse(menu.getTags()));
        return response;
    }

}
