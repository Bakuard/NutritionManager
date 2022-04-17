package com.bakuard.nutritionManager.dto;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.dto.auth.JwsResponse;
import com.bakuard.nutritionManager.dto.dishes.*;
import com.bakuard.nutritionManager.dto.exceptions.*;
import com.bakuard.nutritionManager.dto.products.*;
import com.bakuard.nutritionManager.dto.users.UserResponse;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DtoMapper {

    private UserRepository userRepository;
    private ProductRepository productRepository;
    private DishRepository dishRepository;
    private AppConfigData appConfiguration;
    private MessageSource messageSource;

    public DtoMapper(UserRepository userRepository,
                     ProductRepository productRepository,
                     DishRepository dishRepository,
                     MessageSource messageSource,
                     AppConfigData appConfiguration) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.dishRepository = dishRepository;
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
                        mapToObj(i -> toDishIngredientRequestResponse(ingredients.get(i), i)).
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
                setImageUrl(dto.getImageUrl());

        IntStream.range(0, dto.getIngredients().size()).
                forEach(i -> {
                    DishIngredientRequestResponse ingredient = dto.getIngredients().get(i);
                    builder.addIngredient(toDishIngredient(userId, ingredient));
                });

        dto.getTags().forEach(builder::addTag);

        builder.setConfig(appConfiguration).
                setRepository(productRepository);

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
                setImageUrl(dto.getImageUrl());

        IntStream.range(0, dto.getIngredients().size()).
                forEach(i -> {
                    DishIngredientRequestResponse ingredient = dto.getIngredients().get(i);
                    builder.addIngredient(toDishIngredient(userId, ingredient));
                });

        dto.getTags().forEach(builder::addTag);

        builder.setConfig(appConfiguration).
                setRepository(productRepository);

        return builder.tryBuild();
    }

    public Page<DishForListResponse> toDishesResponse(Page<Dish> dishes) {
        return dishes.map(this::toDishForListResponse);
    }

    public DishProductsListResponse toDishProductsListResponse(UUID userId, DishProductsListRequest dto) {
        Dish dish = dishRepository.tryGetById(userId, dto.getDishId());

        DishProductsListResponse response = new DishProductsListResponse();
        response.setDishId(dish.getId());
        response.setServingNumber(dto.getServingNumber());
        response.setTotalPrice(
                dish.getPrice(
                        dto.getServingNumber(),
                        dto.getProducts().stream().
                                collect(Collectors.toMap(
                                        DishIngredientProductRequest::getIngredientIndex,
                                        DishIngredientProductRequest::getProductIndex
                                ))
                ).orElse(null)
        );
        response.setCategories(
                IntStream.range(0, dish.getIngredientNumber()).
                        mapToObj(ingredientIndex -> {
                            DishIngredientProductRequest ingredientRequest =
                                    dto.getProducts().stream().
                                            filter(ingredient -> ingredient.getIngredientIndex() == ingredientIndex).
                                            findFirst().
                                            orElse(null);

                            int productIndex = ingredientRequest != null ? ingredientRequest.getProductIndex() : 0;

                            List<ProductAsDishIngredientResponse> products =
                                    dish.getProducts(ingredientIndex, 0).
                                    map(p -> toProductAsDishIngredientResponse(
                                            dish,
                                            ingredientIndex,
                                            productIndex,
                                            ingredientRequest != null,
                                            dto.getServingNumber()
                                    )).
                                    getContent();

                            DishIngredientForListResponse ingredient = new DishIngredientForListResponse();
                            ingredient.setIngredientIndex(ingredientIndex);
                            ingredient.setProductCategory(
                                    dish.getIngredient(ingredientIndex).
                                            orElseThrow().
                                            getName()
                            );
                            ingredient.setProducts(products);
                            return ingredient;
                        }).
                        toList()
        );

        return response;
    }


    public Criteria toProductCriteria(int page,
                                      int size,
                                      UUID userId,
                                      String sortRule,
                                      boolean onlyFridge,
                                      String category,
                                      List<String> shops,
                                      List<String> grades,
                                      List<String> manufacturers,
                                      List<String> tags) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.user(userId));
        if(onlyFridge) filters.add(Filter.greater(BigDecimal.ZERO));
        if(category != null) filters.add(Filter.anyCategory(category));
        if(shops != null && !shops.isEmpty()) filters.add(Filter.anyShop(shops));
        if(grades != null && !grades.isEmpty()) filters.add(Filter.anyGrade(grades));
        if(manufacturers != null && !manufacturers.isEmpty()) filters.add(Filter.anyManufacturer(manufacturers));
        if(tags != null && !tags.isEmpty()) filters.add(Filter.minTags(toTags(tags)));

        Filter filter = null;
        if(filters.size() == 1) filter = filters.get(0);
        else if(filters.size() > 1) filter = Filter.and(filters);

        return new Criteria().
                setPageable(Pageable.of(size, page)).
                setSort(Sort.products(sortRule != null ? List.of(sortRule) : List.of())).
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
        else if(filters.size() > 1) filter = Filter.and(filters);

        return new Criteria().
                setPageable(Pageable.of(size, page)).
                setSort(Sort.dishes(sortRule != null ? List.of(sortRule) : List.of())).
                setFilter(filter);
    }


    public ProductFieldsResponse toProductFieldsResponse(UUID userId) {
        Criteria criteria = new Criteria().
                setPageable(Pageable.of(1000, 0)).
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
                setPageable(Pageable.of(1000, 0)).
                setFilter(Filter.user(userId));

        Page<Tag> tags = dishRepository.getTags(criteria);
        Page<String> units = dishRepository.getUnits(criteria);
        Page<String> names = dishRepository.getNames(criteria);

        DishFieldsResponse response = new DishFieldsResponse();
        response.setTags(tags.getContent().stream().map(t -> new FieldResponse(t.getValue())).toList());
        response.setUnits(units.getContent().stream().map(FieldResponse::new).toList());
        response.setNames(names.getContent().stream().map(FieldResponse::new).toList());

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
        return new ExceptionResponse(
                httpStatus,
                getMessage(keyMessage, "unexpected error"),
                getMessage("errorTitle", "Error")
        );
    }

    public ExceptionResponse toExceptionResponse(ValidateException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        if(e.getUserMessageKey() != null && e.getUserMessageKey().startsWith("AuthService")) {
            httpStatus = HttpStatus.FORBIDDEN;
        } else if(e.containsConstraint(Constraint.ENTITY_MUST_EXISTS_IN_DB)) {
            httpStatus = HttpStatus.NOT_FOUND;
        }

        ExceptionResponse response = new ExceptionResponse(
                httpStatus,
                getMessage(e.getUserMessageKey(), e.getMessage()),
                getMessage("errorTitle", "Error")
        );
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
                                                                              int ingredientIndex,
                                                                              int productIndex,
                                                                              boolean isChecked,
                                                                              BigDecimal servingNumber) {
        return dish.getProduct(ingredientIndex, productIndex).
                map(product -> {
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
                            dish.tryGetIngredient(ingredientIndex).getNecessaryQuantity(servingNumber)
                    );
                    response.setLackQuantity(
                            dish.getLackQuantity(ingredientIndex, productIndex, servingNumber).orElseThrow()
                    );
                    response.setLackQuantityPrice(
                            dish.getLackQuantityPrice(ingredientIndex, productIndex, servingNumber).orElseThrow()
                    );
                    response.setTags(toTagsResponse(product.getContext().getTags()));
                    response.setChecked(isChecked);
                    return response;
                }).
                orElse(null);
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

    private DishIngredient.Builder toDishIngredient(UUID userId, DishIngredientRequestResponse dto) {
        return new DishIngredient.Builder().
                setConfig(appConfiguration).
                setName(dto.getFilter().getCategory()).
                setQuantity(dto.getQuantity()).
                setFilter(toDishIngredientFilter(userId, dto.getFilter()));
    }

    private Filter toDishIngredientFilter(UUID userId, DishIngredientFilterRequestResponse dto) {
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

    private DishIngredientRequestResponse toDishIngredientRequestResponse(DishIngredient ingredient, int index) {
        DishIngredientRequestResponse response = new DishIngredientRequestResponse();
        response.setIndex(index);
        response.setQuantity(ingredient.getNecessaryQuantity(BigDecimal.ONE));
        response.setFilter(toDishIngredientFilterRequestResponse(ingredient.getFilter()));
        return response;
    }

    private DishIngredientFilterRequestResponse toDishIngredientFilterRequestResponse(Filter filter) {
        DishIngredientFilterRequestResponse response = new DishIngredientFilterRequestResponse();

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

}
