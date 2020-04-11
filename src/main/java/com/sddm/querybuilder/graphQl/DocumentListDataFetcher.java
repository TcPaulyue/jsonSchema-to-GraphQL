package com.sddm.querybuilder.graphQl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sddm.querybuilder.domain.Document;
import com.sddm.querybuilder.domain.Status;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentListDataFetcher implements DataFetcher<List<JSONObject>> {

    private String documentCollectionName;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void setDocumentCollectionName(String documentCollectionName){
        this.documentCollectionName = documentCollectionName;
    }

    @Override
    public List<JSONObject> get(DataFetchingEnvironment dataFetchingEnvironment) {
        ProjectionOperation projectionOperation = Aggregation.project("data");
        List<AggregationOperation> operations = Lists.newArrayList();
        operations.add(projectionOperation);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<JSONObject> contractAggregationResults = mongoTemplate.aggregate(aggregation, documentCollectionName, JSONObject.class);
        List<JSONObject> documents = contractAggregationResults.getMappedResults();
        List<JSONObject> results = new ArrayList<>();
        for (JSONObject document : documents){
            JSONObject tmp = document.getJSONObject("data");
            tmp.put("id",document.getString("_id"));
            results.add(tmp);
        }
        return results;
   }
}
