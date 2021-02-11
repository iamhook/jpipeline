package com.jpipeline.jpipeline.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CJson extends LinkedHashMap<String, Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public CJson() {
        super();
    }

    public CJson(Map map) {
        super(map);
    }

    public String toJson() {
        try {
            final StringWriter stringWriter = new StringWriter();
            MAPPER.writeValue(stringWriter, this);
            return stringWriter.toString();
        } catch(Exception ex) {
            /* ignore this */
            return null;
        }
    }

    public static CJson fromJson(String source) {
        CJson result = null;
        try {
            if (source != null) {
                result = MAPPER.readValue(source, CJson.class);
            }
        } catch(Exception ex) {
            /* ignore this */
            result = new CJson();
        } finally {
            return result;
        }
    }

    public static CJson fromObject(Object source) {
        return MAPPER.convertValue(source, CJson.class);
    }

    public String getString(String key) {
        if(!containsKey(key)) return null;
        Object v = get(key);
        return (v == null ? null : v.toString());
    }

    public Boolean getBoolean(String key) {
        if(!containsKey(key)) return null;
        Object v = get(key);
        return (v == null ? null : (Boolean) v);
    }

    public Integer getInt(String key) {
        return getInteger(key);
    }

    public Integer getInteger(String key) {
        if(!containsKey(key)) return null;
        Object v =  get(key);
        return (v == null ? null : (Integer) v);
    }

    public Long getLong(String key) {
        if(!containsKey(key)) return null;
        Object v =  get(key);
        if (Integer.class.isInstance(v)) return ((Integer)v).longValue();
        return (v == null ? null : (Long) v);
    }

    private final DateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    public Date getDate(String key) {
        if(!containsKey(key)) return null;
        Object v =  get(key);
        if (v == null) return null;
        if (Date.class.isInstance(v)) return (Date) v;
        try {
            return ISO8601.parse(key);
        } catch (ParseException e) {
            return null;
        }
    }

    public CJson getJson(String key) {
        if(!containsKey(key)) return new CJson();
        return new CJson((Map<String, Object>)get(key));
    }

    public <T> List<T> getList(String key) {
        if(!containsKey(key)) return null;
        Object v = get(key);
        return (v == null ? null : (List) v);
    }

}
