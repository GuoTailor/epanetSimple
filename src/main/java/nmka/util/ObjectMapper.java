package nmka.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectMapper {
    private static Gson gson = create();
    private static Gson create() {
        JsonSerializer<Date> ser = (src, typeOfSrc, context) -> {
            return src == null ? null : new JsonPrimitive(src.getTime());
        };
        JsonDeserializer<Date> deSer = (json, typeOfT, context) -> {
            return json == null ? null : new Date(json.getAsLong());
        };
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(BigDecimal.class, new BigDecimalTypeAdapter());
        builder.registerTypeAdapter(Double.class, new DoubleTypeAdapter());
        builder.registerTypeAdapter(Integer.class, new IntegerTypeAdapter());
        builder.registerTypeAdapter(Long.class, new LongTypeAdapter());
        builder.registerTypeAdapter(Date.class, ser);
        builder.registerTypeAdapter(Date.class, deSer);
        return builder.create();
    }

    public static String toJson(Object instance) {
        return gson.toJson(instance);
    }

    public static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> T convert(Object source, Class<T> target) {
        return fromJson(toJson(source), target);
    }

    public static <T> T convert(Object source, Type type) {
        return fromJson(toJson(source), type);
    }

    public static <T> T clone(Object source, Class<T> targetClass) {
        return fromJson(toJson(source), targetClass);
    }

    public static <T> List<T> clone(List<?> source, Class<T> targetClass) {
        return source.stream().map(s -> clone(s, targetClass)).collect(Collectors.toList());
    }

}

