package com.bakuard.nutritionManager.dto;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.dal.criteria.ProductCategoryCriteria;
import com.bakuard.nutritionManager.dal.criteria.ProductCriteria;
import com.bakuard.nutritionManager.dal.criteria.ProductFieldCriteria;
import com.bakuard.nutritionManager.dto.auth.JwsResponse;
import com.bakuard.nutritionManager.dto.exceptions.ExceptionResponse;
import com.bakuard.nutritionManager.dto.exceptions.FieldExceptionResponse;
import com.bakuard.nutritionManager.dto.products.*;
import com.bakuard.nutritionManager.dto.tags.TagRequestAndResponse;
import com.bakuard.nutritionManager.dto.users.UserResponse;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.AbstractDomainException;
import com.bakuard.nutritionManager.model.exceptions.ValidateException;
import com.bakuard.nutritionManager.model.filters.*;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

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
        response.setVariety(product.getContext().getVariety());
        response.setManufacturer(product.getContext().getManufacturer());
        response.setPrice(product.getContext().getPrice());
        response.setPackingSize(product.getContext().getPackingSize());
        response.setUnit(product.getContext().getUnit());
        response.setQuantity(product.getQuantity());
        response.setDescription(product.getDescription());
        response.setImagePath(product.getImagePath());
        response.setTags(toTagResponse(product.getContext().getTags()));
        return response;
    }

    public Product toProductForUpdate(ProductRequest dto) {
        Product.Builder builder = new Product.Builder().
                setAppConfiguration(appConfiguration).
                setId(dto.getId()).
                setUser(userRepository.getById(dto.getUserId())).
                setCategory(dto.getCategory()).
                setShop(dto.getShop()).
                setVariety(dto.getVariety()).
                setManufacturer(dto.getManufacturer()).
                setPrice(dto.getPrice()).
                setPackingSize(dto.getPackingSize()).
                setUnit(dto.getUnit()).
                setQuantity(dto.getQuantity()).
                setDescription(dto.getDescription()).
                setImagePath(dto.getImagePath());

        dto.getTags().forEach(t -> builder.addTag(t.getName()));

        return builder.tryBuild();
    }

    public Product toProductForAdd(ProductRequest dto) {
        Product.Builder builder = new Product.Builder().
                setAppConfiguration(appConfiguration).
                generateId().
                setUser(userRepository.getById(dto.getUserId())).
                setCategory(dto.getCategory()).
                setShop(dto.getShop()).
                setVariety(dto.getVariety()).
                setManufacturer(dto.getManufacturer()).
                setPrice(dto.getPrice()).
                setPackingSize(dto.getPackingSize()).
                setUnit(dto.getUnit()).
                setQuantity(dto.getQuantity()).
                setDescription(dto.getDescription()).
                setImagePath(dto.getImagePath());

        dto.getTags().forEach(t -> builder.addTag(t.getName()));

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


    public Page<TagRequestAndResponse> toTagsResponse(Page<Tag> tags) {
        return tags.map(tag -> {
            TagRequestAndResponse response = new TagRequestAndResponse();
            response.setName(tag.getValue());
            response.setCode(tag.getValue());
            return response;
        });
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


    public ExceptionResponse toExceptionResponse(HttpStatus httpStatus) {
        return new ExceptionResponse(httpStatus, "Unexpected exception", "Error");
    }

    public ExceptionResponse toExceptionResponse(AbstractDomainException e, HttpStatus httpStatus) {
        return new ExceptionResponse(httpStatus, getMessage(e), getTitle());
    }

    public ExceptionResponse toExceptionResponse(ValidateException e, HttpStatus httpStatus) {
        ExceptionResponse response = new ExceptionResponse(httpStatus, getMessage(e), getTitle());
        e.forEach(ex -> response.addReason(toFieldExceptionResponse(ex)));
        return response;
    }


    private List<TagRequestAndResponse> toTagResponse(Collection<Tag> tags) {
        return tags.stream().map(tag -> {
            TagRequestAndResponse response = new TagRequestAndResponse();
            response.setName(tag.getValue());
            response.setCode(tag.getValue());
            return response;
        }).toList();
    }

    private FieldExceptionResponse toFieldExceptionResponse(IncorrectFiledValueException e) {
        FieldExceptionResponse dto = new FieldExceptionResponse();
        dto.setField(e.getFieldName());
        dto.setTitle(getTitle());
        dto.setMessage(getMessage(e));
        return dto;
    }

    private String getMessage(AbstractDomainException e) {
        return messageSource.getMessage(
                e.getMessageKey(), null, e.getMessage(), LocaleContextHolder.getLocale()
        );
    }

    private String getTitle() {
        return messageSource.getMessage(
                "title", null, "Error", LocaleContextHolder.getLocale()
        );
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
