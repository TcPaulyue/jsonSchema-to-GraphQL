package com.sddm.querybuilder.domain;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import graphql.language.ListType;
import graphql.language.TypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import graphql.language.Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "Schema")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schema {
    @Id
    private String id;
    private JSONObject schemaContent;
    private Status status;

    private List<Link> linkList = new ArrayList<>();

    public Schema(JSONObject schemaContent) {
        this.schemaContent = schemaContent;
    }

    @Override
    public String toString() {
        return String.format(
                "Schema[id='%s',schemaContent='%s',schemaStatus='%s']",
                id, schemaContent, status);
    }

    public Map<String,Type> getTypeMap() {
        Map<String, Type> typeMap = new HashMap<>();
        JSONObject properties = schemaContent.getJSONObject("schema").getJSONObject("properties");
        HashMap<String,LinkedTreeMap> hashMap = new Gson().fromJson(properties.toString(), HashMap.class);
        hashMap.forEach((key, value1) -> {
            String typeName = value1.get("type").toString();
            switch (typeName){
                case "Link":
                    typeName = value1.get("linkTo").toString();
                    linkList.add(new Link(key
                            ,value1.get("collectionName").toString()
                            ,"Link"));
                    typeMap.put(key,new TypeName(typeName));
                    break;
                case "LinkList":
                    typeName = value1.get("linkTo").toString();
                    linkList.add(new Link(key
                            ,value1.get("collectionName").toString()
                            ,"LinkList"));
                    typeMap.put(key,new ListType(new TypeName(typeName)));
                    break;
                case "Embedded":
                    typeMap.put(key,new TypeName(value1.get("typeName").toString()));
                    break;
                case "EmbeddedList":
                    typeMap.put(key,new ListType(
                            new TypeName(value1.get("typeName").toString())));
                    break;
                case "string":
                    typeMap.put(key, new TypeName("String"));
                    break;
                case "number":
                    typeMap.put(key,new TypeName("Int"));
                    break;
                case "date":
                    typeMap.put(key,new TypeName("String"));
                    break;
            }
        });
        typeMap.put("id",new TypeName("String"));
        return typeMap;
    }

    //filter types
    public Map<String, Type> getFilterMap(){
        Map<String, Type> typeMap = new HashMap<>();
        JSONObject properties = schemaContent.getJSONObject("schema").getJSONObject("properties");
        HashMap<String,LinkedTreeMap> hashMap = new Gson().fromJson(properties.toString(), HashMap.class);
        hashMap.forEach((key, value1) -> {
            String typeName = value1.get("type").toString();
            if(value1.get("filter").equals(true))
                typeMap.put(key, new TypeName(adjustTypeName(typeName)));
        });
        typeMap.put("id",new TypeName("String"));
        return typeMap;
    }

    private String adjustTypeName(String str){
        switch (str) {
            case "string":
                return "String";
            case "number":
                return "Int";
            case "date":
                return "String";
            default:
                return "String";
        }
    }

    //Order
    public String getDocumentTypeName(){
        return schemaContent.getJSONObject("schema").getString("description");
    }

    //sddm_orders
    public String getDocumentCollectionName(){
        return "sddm_"+getSchemaTypeName()+"s";
    }

    //order
    public String getSchemaTypeName(){
        String str = schemaContent.getJSONObject("schema").getString("description");
        return lowerCase(str);
    }

    //orderDocuments
    public String getSchemaTypeListName(){
        String str = schemaContent.getJSONObject("schema").getString("description");
        return lowerCase(str)+"Documents";
    }

    //orderFilters
    public String getFilterTypeName(){
        return getSchemaTypeName()+"Filters";
    }

    private String upperCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String lowerCase(String str){
        return str.substring(0,1).toLowerCase() + str.substring(1);
    }
}