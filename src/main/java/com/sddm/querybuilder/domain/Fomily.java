package com.sddm.querybuilder.domain;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "Fomily")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fomily {
    @Id
    private String id;
    private JSONObject schemaContent;
}
