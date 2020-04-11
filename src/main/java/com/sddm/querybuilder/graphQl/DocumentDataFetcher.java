package com.sddm.querybuilder.graphQl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentDataFetcher implements DataFetcher<JSONObject> {
    @Autowired
    private MongoTemplate mongoTemplate;

    private String documentCollectionName;

    public void setDocumentCollectionName(String documentCollectionName){
        this.documentCollectionName = documentCollectionName;
    }

    @Override
    public JSONObject get(DataFetchingEnvironment dataFetchingEnvironment) {
        String id = String.valueOf(dataFetchingEnvironment.getArguments().get("id"));
        String MONGODB_ID = "_id";
        List<AggregationOperation> operations = Lists.newArrayList();
        operations.add(Aggregation.match(Criteria.where(MONGODB_ID).is(id)));
        ProjectionOperation projectionOperation = Aggregation.project("data");
        operations.add(projectionOperation);
        Aggregation aggregation = Aggregation.newAggregation(operations);
       List<JSONObject> documents = mongoTemplate.aggregate(aggregation, documentCollectionName, JSONObject.class).getMappedResults();
       JSONObject results = documents.get(0).getJSONObject("data");
       results.put("id",documents.get(0).getString("_id"));
       return results;
    }
}
