package org.apache.bigtop.manager.dao.converter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter
public class JsonToMapConverter implements AttributeConverter<Map<String, String>, String> {
    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        // Convert Map to JSON String
        return new Gson().toJson(attribute);
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        // Convert JSON String to Map
        return new Gson().fromJson(dbData, new TypeToken<Map<String, String>>() {}.getType());
    }
}
