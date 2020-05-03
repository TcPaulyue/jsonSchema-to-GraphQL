package com.sddm.querybuilder.graphQl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sddm.querybuilder.domain.Document;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentCommitsMutation implements DataFetcher<List<JSONObject>> {
    private final MongoTemplate mongoTemplate;
    private final Javers javers;
    private String documentCollectionName;

    public DocumentCommitsMutation(MongoTemplate mongoTemplate,Javers javers){
        this.mongoTemplate = mongoTemplate;
        this.javers = javers;
    }

    void setDocumentCollectionName(String documentCollectionName){
        this.documentCollectionName = documentCollectionName;
    }


    @Override
    public List<JSONObject> get(DataFetchingEnvironment dataFetchingEnvironment) {
        String id = dataFetchingEnvironment.getArgument("id").toString();
         return trackDocumentChangesWithJaVers(id);
    }

    private List<JSONObject> trackDocumentChangesWithJaVers(String documentId) {
        Document document = this.mongoTemplate.findById(documentId,Document.class, documentCollectionName);
        List<JSONObject> jsonArray = new ArrayList<>();
        try {
            JqlQuery jqlQuery = QueryBuilder.byInstance(document).build();
            List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery);
            for (CdoSnapshot snapshot : snapshots) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("commitId", snapshot.getCommitId().getMajorId());
                jsonObject.put("commitDate", snapshot.getCommitMetadata().getCommitDate());
                JSON.parseObject(javers.getJsonConverter().toJson(snapshot.getState()), Document.class);
                jsonObject.put("data", JSON.parseObject(javers.getJsonConverter().toJson(snapshot.getState()), Document.class).getData());
                jsonArray.add(jsonObject);
            }
            return jsonArray;
        } catch (NullPointerException e) {
            System.out.println("track document with jaVers failed by id " + documentId);
            return null;
        }
    }

}
