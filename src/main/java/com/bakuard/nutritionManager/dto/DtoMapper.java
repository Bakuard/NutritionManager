package com.bakuard.nutritionManager.dto;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.ProductRepository;
import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.dto.auth.JwsResponse;
import com.bakuard.nutritionManager.dto.exceptions.*;
import com.bakuard.nutritionManager.dto.products.*;
import com.bakuard.nutritionManager.dto.users.UserResponse;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;
import com.bakuard.nutritionManager.services.AuthService;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.*;

public class DtoMapper {

    private UserRepository userRepository;
    private ProductRepository productRepository;
    private AppConfigData appConfiguration;
    private MessageSource messageSource;

    public DtoMapper(UserRepository userRepository,
                     ProductRepository productRepository,
                     MessageSource messageSource,
                     AppConfigData appConfiguration) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.messageSource = messageSource;
        this.appConfiguration = appConfiguration;
    }

    public ProductResponse toProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setUser(toUserResponse(product.getUser()));
        response.setCategory(product.getContext().getCategory());
        response.setShop(product.getContext().getShop());
        response.setGrade(product.getContext().getVariety());
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

    public Product toProductForUpdate(UUID userId, ProductUpdateRequest dto) {
        Product.Builder builder = new Product.Builder().
                setAppConfiguration(appConfiguration).
                setId(dto.getId()).
                setUser(userRepository.getById(userId)).
                setCategory(dto.getCategory()).
                setShop(dto.getShop()).
                setVariety(dto.getGrade()).
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

    public Product toProductForAdd(UUID userId, ProductAddRequest dto) {
        Product.Builder builder = new Product.Builder().
                setAppConfiguration(appConfiguration).
                generateId().
                setUser(userRepository.getById(userId)).
                setCategory(dto.getCategory()).
                setShop(dto.getShop()).
                setVariety(dto.getGrade()).
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


    public Criteria toProductCriteria(int page,
                                      int size,
                                      UUID userId,
                                      String sortRule,
                                      Boolean onlyFridge,
                                      String category,
                                      List<String> shops,
                                      List<String> varieties,
                                      List<String> manufacturers,
                                      List<String> tags) {
        User user = userRepository.getById(userId);

        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.user(user));
        if(onlyFridge) filters.add(Filter.greater(BigDecimal.ZERO));
        if(category != null) filters.add(Filter.anyCategory(category));
        if(shops != null) filters.add(Filter.anyShop(shops));
        if(varieties != null) filters.add(Filter.anyVariety(varieties));
        if(manufacturers != null) filters.add(Filter.anyManufacturer(manufacturers));
        if(tags != null) filters.add(Filter.minTags(toTags(tags)));

        Filter filter = null;
        if(filters.size() == 1) filter = filters.get(0);
        else if(filters.size() > 1) filter = Filter.and(filters);

        return new Criteria().
                setPageable(Pageable.of(size, page)).
                setSort(toProductSort(sortRule)).
                setFilter(filter);
    }


    public ProductFieldsResponse toProductFieldsResponse(UUID userId) {
        Criteria criteria = new Criteria().
                setPageable(Pageable.of(1000, 0)).
                setFilter(Filter.user(userRepository.getById(userId)));

        Page<String> manufacturers = productRepository.getManufacturers(criteria);
        Page<String> varieties = productRepository.getVarieties(criteria);
        Page<String> shops = productRepository.getShops(criteria);
        Page<String> categories = productRepository.getCategories(criteria);
        Page<Tag> tags = productRepository.getTags(criteria);

        ProductFieldsResponse response = new ProductFieldsResponse();
        response.setTags(tags.getContent().stream().map(t -> new FieldResponse(t.getValue())).toList());
        response.setVarieties(varieties.getContent().stream().map(FieldResponse::new).toList());
        response.setManufacturers(manufacturers.getContent().stream().map(FieldResponse::new).toList());
        response.setShops(shops.getContent().stream().map(FieldResponse::new).toList());
        response.setCategories(categories.getContent().stream().map(FieldResponse::new).toList());

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
        if(e.getCheckedClass() == AuthService.class) httpStatus = HttpStatus.FORBIDDEN;
        else if(e.containsConstraint(Constraint.ENTITY_MUST_EXISTS_IN_DB)) httpStatus = HttpStatus.NOT_FOUND;

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

    private Sort toProductSort(String sortRuleAsString) {
        if(sortRuleAsString == null) return null;
        String[] parameters = sortRuleAsString.split("_");
        return Sort.products().put(parameters[0], parameters[1]);
    }

    private List<Tag> toTags(List<String> tags) {
        return tags.stream().map(Tag::new).toList();
    }

}
