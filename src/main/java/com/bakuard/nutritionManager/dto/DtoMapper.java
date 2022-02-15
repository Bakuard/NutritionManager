package com.bakuard.nutritionManager.dto;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.dal.criteria.products.ProductCategoryCriteria;
import com.bakuard.nutritionManager.dal.criteria.products.ProductCriteria;
import com.bakuard.nutritionManager.dal.criteria.products.ProductFieldCriteria;
import com.bakuard.nutritionManager.dto.auth.JwsResponse;
import com.bakuard.nutritionManager.dto.exceptions.*;
import com.bakuard.nutritionManager.dto.products.*;
import com.bakuard.nutritionManager.dto.users.UserResponse;
import com.bakuard.nutritionManager.model.*;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;
import com.bakuard.nutritionManager.services.AuthService;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;

import java.util.*;

public class DtoMapper {

    private AppConfigData appConfiguration;
    private UserRepository userRepository;
    private MessageSource messageSource;

    public DtoMapper(UserRepository userRepository,
                     MessageSource messageSource,
                     AppConfigData appConfiguration) {
        this.userRepository = userRepository;
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
        response.setImagePath(product.getImagePath());
        response.setTags(toTagsResponse(product.getContext().getTags()));
        return response;
    }

    public Product toProductForUpdate(ProductRequest dto) {
        Product.Builder builder = new Product.Builder().
                setAppConfiguration(appConfiguration).
                setId(dto.getId()).
                setUser(userRepository.getById(dto.getUserId())).
                setCategory(dto.getCategory()).
                setShop(dto.getShop()).
                setVariety(dto.getGrade()).
                setManufacturer(dto.getManufacturer()).
                setPrice(dto.getPrice()).
                setPackingSize(dto.getPackingSize()).
                setUnit(dto.getUnit()).
                setQuantity(dto.getQuantity()).
                setDescription(dto.getDescription()).
                setImagePath(dto.getImagePath());

        dto.getTags().forEach(builder::addTag);

        return builder.tryBuild();
    }

    public Product toProductForAdd(ProductRequest dto) {
        Product.Builder builder = new Product.Builder().
                setAppConfiguration(appConfiguration).
                generateId().
                setUser(userRepository.getById(dto.getUserId())).
                setCategory(dto.getCategory()).
                setShop(dto.getShop()).
                setVariety(dto.getGrade()).
                setManufacturer(dto.getManufacturer()).
                setPrice(dto.getPrice()).
                setPackingSize(dto.getPackingSize()).
                setUnit(dto.getUnit()).
                setQuantity(dto.getQuantity()).
                setDescription(dto.getDescription()).
                setImagePath(dto.getImagePath());

        dto.getTags().forEach(builder::addTag);

        return builder.tryBuild();
    }

    public Page<ProductResponse> toProductsResponse(Page<Product> products) {
        return products.map(this::toProductResponse);
    }


    public ProductCriteria toProductCriteria(int page,
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
        if(category != null) filters.add(CategoryFilter.of(category));
        if(shops != null) filters.add(ShopsFilter.of(shops));
        if(varieties != null) filters.add(VarietiesFilter.of(varieties));
        if(manufacturers != null) filters.add(ManufacturerFilter.of(manufacturers));
        if(tags != null) filters.add(MinTagsFilter.of(toTags(tags)));

        Filter filter = null;
        if(filters.size() == 1) filter = filters.get(0);
        else if(filters.size() > 1) filter = AndFilter.of(filters);

        return ProductCriteria.of(
                Pageable.of(page, size),
                user).
                setOnlyFridge(onlyFridge).
                setProductSort(toProductSort(sortRule)).
                setFilter(filter);
    }

    public ProductFieldCriteria toProductFieldCriteria(int page, int size, UUID userId, String productCategory) {
        User user = userRepository.getById(userId);
        return ProductFieldCriteria.of(Pageable.of(page, size), user).
                setProductCategory(productCategory);
    }

    public ProductCategoryCriteria toProductCategoryCriteria(int page, int size, UUID userId) {
        User user = userRepository.getById(userId);
        return ProductCategoryCriteria.of(Pageable.of(page, size), user);
    }


    public Page<String> toTagsResponse(Page<Tag> tags) {
        return tags.map(Tag::getValue);
    }

    public Page<ShopResponse> toShopsResponse(Page<String> shops) {
        return shops.map(shop -> {
            ShopResponse response = new ShopResponse();
            response.setName(shop);
            response.setCode(shop);
            return response;
        });
    }

    public Page<VarietyResponse> toVarietiesResponse(Page<String> varieties) {
        return varieties.map(shop -> {
            VarietyResponse response = new VarietyResponse();
            response.setName(shop);
            response.setCode(shop);
            return response;
        });
    }

    public Page<ManufacturerResponse> toManufacturerResponse(Page<String> manufacturers) {
        return manufacturers.map(shop -> {
            ManufacturerResponse response = new ManufacturerResponse();
            response.setName(shop);
            response.setCode(shop);
            return response;
        });
    }

    public Page<CategoryResponse> toCategoryResponse(Page<String> categories) {
        return categories.map(shop -> {
            CategoryResponse response = new CategoryResponse();
            response.setName(shop);
            response.setCode(shop);
            return response;
        });
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

    public ExceptionResponse toExceptionResponse(ServiceException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        if(e.getCheckedType() == AuthService.class) httpStatus = HttpStatus.FORBIDDEN;
        else if(e.containsConstraint(ConstraintType.UNKNOWN_ENTITY)) httpStatus = HttpStatus.NOT_FOUND;

        ExceptionResponse response = new ExceptionResponse(
                httpStatus,
                getMessage(e.getMessageKey(), e.getMessage()),
                getMessage("errorTitle", "Error")
        );
        e.forEach(constraint -> response.addReason(toConstraintResponse(constraint)));
        return response;
    }

    public ExceptionResponse toExceptionResponse(ValidateException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        if(e.getCheckedType() == AuthService.class) httpStatus = HttpStatus.FORBIDDEN;
        else if(e.containsConstraint(ConstraintType.UNKNOWN_ENTITY)) httpStatus = HttpStatus.NOT_FOUND;

        ExceptionResponse response = new ExceptionResponse(
                httpStatus,
                getMessage(e.getMessageKey(), e.getMessage()),
                getMessage("errorTitle", "Error")
        );
        e.forEach(constraint -> response.addReason(toConstraintResponse(constraint)));
        return response;
    }


    private ConstraintResponse toConstraintResponse(Constraint constraint) {
        ConstraintResponse dto = new ConstraintResponse();
        dto.setField(constraint.getFieldName());
        dto.setTitle(getMessage("constraintTitle", "Reason"));
        dto.setMessage(getMessage(constraint.getMessageKey(), constraint.getDetail()));
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

    private ProductSort toProductSort(String sortRuleAsString) {
        if(sortRuleAsString == null) return null;
        String[] parameters = sortRuleAsString.split("_");
        return new ProductSort(parameters[0], parameters[1]);
    }

    private List<Tag> toTags(List<String> tags) {
        return tags.stream().map(Tag::new).toList();
    }

}
