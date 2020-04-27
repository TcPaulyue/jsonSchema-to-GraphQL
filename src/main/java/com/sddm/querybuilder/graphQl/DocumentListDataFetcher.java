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

import java.util.*;

@Component
public class DocumentListDataFetcher implements DataFetcher<List<JSONObject>> {
    private String documentCollectionName;
    private final MongoTemplate mongoTemplate;
    private String keyNameInParent;

    @Autowired
    public DocumentListDataFetcher(MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }
    void setDocumentCollectionName(String documentCollectionName){
        this.documentCollectionName = documentCollectionName;
    }
    void setKeyNameInParent(String keyNameInParent) {
        this.keyNameInParent = keyNameInParent;
    }

    @Override
    public List<JSONObject> get(DataFetchingEnvironment dataFetchingEnvironment) {
        if(keyNameInParent != null){
            JSONObject jsonObject = dataFetchingEnvironment.getSource();
            List<String> ids = (List<String>) jsonObject.get(keyNameInParent);
            return this.getDocumentsByLinkList(ids);
        }
        LinkedHashMap<String,Object> filters = dataFetchingEnvironment.getArgument("filter");
        return this.getDocumentsByAggregation(filters);
   }

   public List<JSONObject> getDocumentsByLinkList(List<String> ids){
        List<JSONObject> results = new ArrayList<>();
        for(String id: ids){
            List<AggregationOperation> operations = Lists.newArrayList();
            operations.add(Aggregation.match(Criteria.where("_id").is(id)));
            ProjectionOperation projectionOperation = Aggregation.project("data");
            operations.add(projectionOperation);
            Aggregation aggregation = Aggregation.newAggregation(operations);
            List<JSONObject> documents = mongoTemplate.aggregate(aggregation, documentCollectionName, JSONObject.class).getMappedResults();
            JSONObject doc = documents.get(0).getJSONObject("data");
            doc.put("id",documents.get(0).getString("_id"));
            results.add(doc);
        }
        return results;
   }

   private List<JSONObject> getDocumentsByAggregation(LinkedHashMap<String,Object> filters){
       ProjectionOperation projectionOperation = Aggregation.project("data");
       List<AggregationOperation> operations = Lists.newArrayList();
       operations.add(projectionOperation);
       if(filters!=null) {
           operations = this.addFilterOperations(filters,operations);
       }
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

   private List<AggregationOperation> addFilterOperations(LinkedHashMap<String, Object> filters, List<AggregationOperation> operations){
       String DATADOG ="data.";
       for(Map.Entry<String,Object> entry : filters.entrySet()){
           String key = entry.getKey();
           switch (key) {
               case "OR":
                   ArrayList<LinkedHashMap> linkedHashMaps = (ArrayList<LinkedHashMap>) entry.getValue();
                   List<Criteria> criteriaList = new ArrayList<>();
                   linkedHashMaps.forEach(linkedHashMap
                           ->linkedHashMap.forEach((key1,value1)
                           ->criteriaList.add(Criteria.where(DATADOG + key1.toString()).is(value1.toString()))));
                   Criteria[] criteria = criteriaList.toArray(new Criteria[criteriaList.size()]);
                   operations.add(Aggregation.match(new Criteria().orOperator(criteria)));
                   break;
               case "AND":
                   ArrayList<LinkedHashMap> value = (ArrayList<LinkedHashMap>) entry.getValue();
                   value.forEach(linkedHashMap
                           -> linkedHashMap.forEach((key1, value1)
                           -> operations.add(Aggregation.match(Criteria.where(DATADOG+key1.toString()).is(value1.toString())))));
                   break;
               default:
                   operations.add(Aggregation.match(Criteria.where(DATADOG+key).is(entry.getValue().toString())));
                   break;
           }
       }
       return operations;
   }
}
