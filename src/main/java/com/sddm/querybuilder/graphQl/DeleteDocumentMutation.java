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
public class DeleteDocumentMutation implements DataFetcher<JSONObject> {
    private final MongoTemplate mongoTemplate;
    private final Javers javers;
    private String documentCollectionName;
    public DeleteDocumentMutation(MongoTemplate mongoTemplate,Javers javers){
        this.mongoTemplate = mongoTemplate;
        this.javers = javers;
    }
    void setDocumentCollectionName(String documentCollectionName){
        this.documentCollectionName = documentCollectionName;
    }

    @Override
    public JSONObject get(DataFetchingEnvironment dataFetchingEnvironment) {
        String id = dataFetchingEnvironment.getArgument("id").toString();
        return  deleteDocumentById(id);
    }

    private JSONObject deleteDocumentById(String id){
        JSONObject result = new JSONObject();
        Document document = this.mongoTemplate
                .findById(id, Document.class,documentCollectionName);
        assert document != null;
        if(document.getStatus().equals(Status.Created)){
            document.setStatus(Status.Deleted);
            document = this.mongoTemplate.save(document,documentCollectionName);
            javers.commit(document.getId(),document);
            result.put("deleteResult",document.getStatus().toString());
            return result;
        } else {
            result.put("deleteResult", "document has been deleted");
            return result;
        }
    }
}
