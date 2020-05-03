package com.sddm.querybuilder.graphQl;

import com.alibaba.fastjson.JSONObject;
import com.sddm.querybuilder.domain.Document;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.javers.core.Javers;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.print.Doc;

@Component
public class UpdateDocumentMutation implements DataFetcher<JSONObject> {

    private final MongoTemplate mongoTemplate;
    private final Javers javers;

    private String documentCollectionName;
    public UpdateDocumentMutation(MongoTemplate mongoTemplate,Javers javers){
        this.mongoTemplate = mongoTemplate;
        this.javers = javers;
    }
    void setDocumentCollectionName(String documentCollectionName){
        this.documentCollectionName = documentCollectionName;
    }

    @Override
    public JSONObject get(DataFetchingEnvironment dataFetchingEnvironment) {
        String id = dataFetchingEnvironment.getArgument("id").toString();
        JSONObject content = new JSONObject(dataFetchingEnvironment.getArgument("content"));
        return this.updateDocumentById(id,content).getData();
    }

    private Document updateDocumentById(String id,JSONObject content){
        Document document = mongoTemplate.findById(id,Document.class,documentCollectionName);
        assert document != null;
        document.setData(content);
        document = mongoTemplate.save(document,documentCollectionName);
        javers.commit(document.getId(),document);
        return document;
    }

}
