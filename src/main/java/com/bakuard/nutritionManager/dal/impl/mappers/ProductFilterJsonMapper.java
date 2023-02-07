package com.bakuard.nutritionManager.dal.impl.mappers;

import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.filters.*;

import com.fasterxml.jackson.core.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductFilterJsonMapper {

    private JsonFactory jsonFactory;

    public ProductFilterJsonMapper() {
        jsonFactory = new JsonFactory();
    }

    public Filter toFilter(String json) {
        try {
            JsonParser parser = jsonFactory.createParser(json);
            Filter result = switchFilter(parser);
            parser.close();

            return result;
        } catch(IOException e) {
            throw new IllegalStateException("Fail to parse json to filter", e);
        }
    }

    public String toJson(Filter filter) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            JsonGenerator writer = jsonFactory.createGenerator(buffer, JsonEncoding.UTF8);

            switchFilter(filter, writer);
            writer.close();

            writer.flush();

            return buffer.toString();
        } catch(IOException e) {
            throw new IllegalStateException("Fail to covert filter to json", e);
        }
    }


    private Filter switchFilter(JsonParser parser) throws IOException {
        Filter result = null;

        Filter.Type type = null;
        while(parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();

            if("type".equals(fieldName)) {
                type = Filter.Type.valueOf(parser.nextTextValue());
            }

            if("values".equals(fieldName)) {
                switch(type) {
                    case OR -> result = toOrElseFilter(parser);
                    case AND -> result = toAndFilter(parser);
                    case MIN_TAGS -> result = toMinTagsFilter(parser);
                    case CATEGORY -> result = toCategoryFilter(parser);
                    case SHOPS -> result = toShopsFilter(parser);
                    case GRADES -> result = toGradesFilter(parser);
                    case MANUFACTURER -> result = toManufacturerFilter(parser);
                    case USER -> result = toUserFilter(parser);
                }
            }
        }

        return result;
    }

    private OrFilter toOrElseFilter(JsonParser parser) throws IOException {
        List<Filter> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(switchFilter(parser));
        }

        return Filter.or(values);
    }

    private AndFilter toAndFilter(JsonParser parser) throws IOException {
        List<Filter> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(switchFilter(parser));
        }

        return Filter.and(values);
    }

    private AnyFilter toCategoryFilter(JsonParser parser) throws IOException {
        List<String> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getValueAsString());
        }

        return Filter.anyCategory(values);
    }

    private AnyFilter toManufacturerFilter(JsonParser parser) throws IOException {
        List<String> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getValueAsString());
        }

        return Filter.anyManufacturer(values);
    }

    private AnyFilter toShopsFilter(JsonParser parser) throws IOException {
        List<String> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getValueAsString());
        }

        return Filter.anyShop(values);
    }

    private AnyFilter toGradesFilter(JsonParser parser) throws IOException {
        List<String> values = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            values.add(parser.getValueAsString());
        }

        return Filter.anyGrade(values);
    }

    private MinTagsFilter toMinTagsFilter(JsonParser parser) throws IOException {
        List<Tag> tags = new ArrayList<>();

        parser.nextToken(); //BEGIN_ARRAY
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            tags.add(new Tag(parser.getValueAsString()));
        }

        return Filter.minTags(tags);
    }

    private UserFilter toUserFilter(JsonParser parser) throws IOException {
        parser.nextToken(); //BEGIN_ARRAY
        UUID userId = UUID.fromString(parser.nextTextValue());
        parser.nextToken(); //END_ARRAY

        return Filter.user(userId);
    }


    private void switchFilter(Filter filter, JsonGenerator writer) throws IOException {
        switch(filter.getType()) {
            case OR -> toJson((OrFilter) filter, writer);
            case AND -> toJson((AndFilter) filter, writer);
            case MIN_TAGS -> toJson((MinTagsFilter) filter, writer);
            case CATEGORY, SHOPS, GRADES, MANUFACTURER -> toJson((AnyFilter) filter, writer);
            case USER -> toJson((UserFilter) filter, writer);
        }
    }

    private void toJson(OrFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        for(Filter f : filter.getOperands()) switchFilter(f, writer);
        writer.writeEndArray();

        writer.writeEndObject();
    }

    private void toJson(AndFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        for(Filter f : filter.getOperands()) switchFilter(f, writer);
        writer.writeEndArray();

        writer.writeEndObject();
    }

    private void toJson(AnyFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        for(String manufacturer : filter.getValues()) writer.writeString(manufacturer);
        writer.writeEndArray();

        writer.writeEndObject();
    }

    private void toJson(MinTagsFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        for(Tag tag : filter.getTags()) writer.writeString(tag.getValue());
        writer.writeEndArray();

        writer.writeEndObject();
    }

    private void toJson(UserFilter filter, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        writer.writeStringField("type", filter.getType().name());

        writer.writeFieldName("values");
        writer.writeStartArray();
        writer.writeString(filter.getUserId().toString());
        writer.writeEndArray();

        writer.writeEndObject();
    }

}
