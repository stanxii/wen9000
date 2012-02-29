// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.stan.wen9000.domain;

import com.stan.wen9000.domain.Profile;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

privileged aspect Profile_Roo_Json {
    
    public String Profile.toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public static Profile Profile.fromJsonToProfile(String json) {
        return new JSONDeserializer<Profile>().use(null, Profile.class).deserialize(json);
    }
    
    public static String Profile.toJsonArray(Collection<Profile> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }
    
    public static Collection<Profile> Profile.fromJsonArrayToProfiles(String json) {
        return new JSONDeserializer<List<Profile>>().use(null, ArrayList.class).use("values", Profile.class).deserialize(json);
    }
    
}
