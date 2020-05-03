package com.sddm.querybuilder.graphQl;

import com.alibaba.fastjson.JSONObject;
import com.sddm.querybuilder.domain.Document;
import com.sddm.querybuilder.domain.Status;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.javers.core.Javers;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;


@Component
public class CreateDocumentMutation implements DataFetcher<JSONObject> {

    private final MongoTemplate mongoTemplate;
    private final Javers javers;

    private String documentCollectionName;
    public CreateDocumentMutation(MongoTemplate mongoTemplate,Javers javers){
        this.mongoTemplate = mongoTemplate;
        this.javers = javers;
    }
    void setDocumentCollectionName(String documentCollectionName){
        this.documentCollectionName = documentCollectionName;
    }

    @Override
    public JSONObject get(DataFetchingEnvironment dataFetchingEnvironment) {
        String schemaId = dataFetchingEnvironment.getArgument("schemaId").toString();
        JSONObject content = new JSONObject(dataFetchingEnvironment.getArgument("content"));
        return this.createNewDocument(schemaId,content).getData();
    }

    private Document createNewDocument(String schemaId,JSONObject content){
        Document document = new Document(schemaId, this.documentCollectionName, content);
        document.setStatus(Status.Created);
        document =  mongoTemplate.insert(document,this.documentCollectionName);
        javers.commit(document.getId(),document);
        return document;
    }
}
