package com.sddm.querybuilder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sddm.querybuilder.domain.Document;
import com.sddm.querybuilder.domain.Schema;
import com.sddm.querybuilder.domain.Status;
import com.sddm.querybuilder.repository.DocumentRepository;
import com.sddm.querybuilder.repository.SchemaRepository;
import org.apache.commons.io.IOUtils;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private DocumentRepository documentRepository;

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
//        operations.add(Aggregation.match(Criteria.where("data.ContactName").is("Thomas Hardy")));
//        operations.add(Aggregation.match(Criteria.where("data.CompanyName").is("Around the Horn")));
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("data.City").is("London"));
        criteriaList.add(Criteria.where("data.ContactName").is("Thomas Hardy"));
        Criteria[] criteria = criteriaList.toArray(new Criteria[criteriaList.size()]);
        operations.add(Aggregation.match(new Criteria().orOperator(criteria)));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<JSONObject> contractAggregationResults = mongoTemplate.aggregate(aggregation, "sddm_customers", JSONObject.class);
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

    @Test
    void testReadJsonFile() throws IOException {
        InputStream is = new FileInputStream("/Users/congtang/Desktop/sddm-backend/sddm-querybuilder/src/main/resources/test.json");
        String jsonTxt = IOUtils.toString(is, "UTF-8");
        System.out.println(jsonTxt);
        JSONObject json = JSON.parseObject(jsonTxt);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("schema",json);
        Schema schema = new Schema();
        schema.setSchemaContent(jsonObject);
        schema.setStatus(Status.Created);
        schemaRepository.save(schema);
//        Schema schema = schemaRepository.findById("5df1ed8d7a47184df89fde63").get();
//        schema.setSchemaContent(jsonObject);
//        schemaRepository.save(schema);
    }

    //customerid : 5df1f62917e609bb64d920d3
//employeeid : 5df1f62f17e609bb64d92140
    @Test
    public void resetOrderDocument(){
        Document document =documentRepository.findById("5df1f63517e609bb64d9215b").get();
        JSONObject jsonObject = document.getData();
        jsonObject.put("CustomerID","5df1f62917e609bb64d920d3");
        jsonObject.put("EmployeeID","5df1f62f17e609bb64d92140");
        document.setData(jsonObject);
        documentRepository.save(document);
    }

    @Test
    public void createEmbeddedOrderDocument() throws IOException {
        InputStream is = new FileInputStream("/Users/congtang/Desktop/sddm-backend/sddm-querybuilder/src/main/resources/orderDocument.json");
        String jsonTxt = IOUtils.toString(is, "UTF-8");
        System.out.println(jsonTxt);
        JSONObject json = JSON.parseObject(jsonTxt);
        Document document = documentRepository.findById("5ea39637af36a0436297ed49").get();
        document.setData(json);
        documentRepository.save(document);
    }

}
