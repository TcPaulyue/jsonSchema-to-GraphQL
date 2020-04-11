package com.sddm.querybuilder;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sddm.querybuilder.domain.Schema;
import com.sddm.querybuilder.repository.SchemaRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.junit4.SpringRunner;
import graphql.language.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
class QuerybuilderApplicationTests {
    @Autowired
    private SchemaRepository schemaRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {
        Schema schema = schemaRepository.findById("5df1ed8d7a47184df89fde63").get();
        Map<String,Type> map = schema.getTypeMap();
        map.toString();
    }

    @Test
    void testGetDocumentDataList(){
        ProjectionOperation projectionOperation = Aggregation.project("data");
        List<AggregationOperation> operations = Lists.newArrayList();
        operations.add(projectionOperation);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<JSONObject> contractAggregationResults = mongoTemplate.aggregate(aggregation, "sddm_orders", JSONObject.class);
        List<JSONObject> documents = contractAggregationResults.getMappedResults();
        List<JSONObject> results = new ArrayList<>();
        for (JSONObject document : documents){
            String  id = document.getString("_id");
            JSONObject tmp = document.getJSONObject("data");
            tmp.put("id",id);
            results.add(tmp);
        }
        System.out.println(documents.size());
        System.out.println(results.size());
    }

    @Test
    void testGetDocumentData(){
        String id ="5df1f63517e609bb64d9215b";
        String MONGODB_ID = "_id";
        List<AggregationOperation> operations = Lists.newArrayList();
        operations.add(Aggregation.match(Criteria.where(MONGODB_ID).is(id)));
        ProjectionOperation projectionOperation = Aggregation.project("data");
        operations.add(projectionOperation);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        List<JSONObject> documents = mongoTemplate.aggregate(aggregation, "sddm_orders", JSONObject.class).getMappedResults();
        JSONObject results = documents.get(0).getJSONObject("data");
        results.put("id",documents.get(0).getString("_id"));
        documents.toArray();
    }

}
