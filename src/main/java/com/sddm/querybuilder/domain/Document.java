package com.sddm.querybuilder.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document(collection = "sddm_orders")
@Data
@AllArgsConstructor
public class Document {
    @Id
    private String id;

    private String schemaId;

    private String collectionName;

    private JSONObject data;

    private Status status;

    public Document() {
    }

    public Document(String schemaId, String collectionName, JSONObject data) {
        this.schemaId = schemaId;
        this.collectionName = collectionName;
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format(
                "{id:'%s', schemaId:'%s',collectionName:'%s', data:'%s',status:'%s'}",
                id, schemaId, collectionName, JSON.toJSONString(data), status);
    }

    public JSONObject toJSONObject() {
        return JSONObject.parseObject(this.toString());
    }
}
