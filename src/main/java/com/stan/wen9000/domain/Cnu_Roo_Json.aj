// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.stan.wen9000.domain;

import com.stan.wen9000.domain.Cnu;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

privileged aspect Cnu_Roo_Json {
    
    public String Cnu.toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public static Cnu Cnu.fromJsonToCnu(String json) {
        return new JSONDeserializer<Cnu>().use(null, Cnu.class).deserialize(json);
    }
    
    public static String Cnu.toJsonArray(Collection<Cnu> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }
    
    public static Collection<Cnu> Cnu.fromJsonArrayToCnus(String json) {
        return new JSONDeserializer<List<Cnu>>().use(null, ArrayList.class).use("values", Cnu.class).deserialize(json);
    }
    
}
