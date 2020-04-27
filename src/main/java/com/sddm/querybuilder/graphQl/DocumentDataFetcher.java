package com.sddm.querybuilder.graphQl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentDataFetcher implements DataFetcher<JSONObject> {

    private final MongoTemplate mongoTemplate;
    private String documentCollectionName;
    private String keyNameInParent;

    public DocumentDataFetcher(MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }
    void setDocumentCollectionName(String documentCollectionName){
        this.documentCollectionName = documentCollectionName;
    }
    void setKeyNameInParent(String keyNameInParent) {
        this.keyNameInParent = keyNameInParent;
    }

    @Override
    public JSONObject get(DataFetchingEnvironment dataFetchingEnvironment) {
        String id = String.valueOf(dataFetchingEnvironment.getArguments().get("id"));
        if(id.equals("null")){
            JSONObject jsonObject = dataFetchingEnvironment.getSource();
            id = jsonObject.getString(keyNameInParent);
        }
        return this.getDocumentByAggregation(id);
    }

    private JSONObject getDocumentByAggregation(String id){
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
