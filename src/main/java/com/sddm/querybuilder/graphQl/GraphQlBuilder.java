package com.sddm.querybuilder.graphQl;


import com.sddm.querybuilder.domain.Link;
import com.sddm.querybuilder.domain.Schema;
import com.sddm.querybuilder.repository.SchemaRepository;
import graphql.GraphQL;
import graphql.language.*;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.GraphQL.newGraphQL;

@Component
public class GraphQlBuilder {
    private MyTypeRegistry myTypeRegistry;
    private MyRuntimeWiring myRuntimeWiring;
    private SchemaRepository schemaRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    GraphQlBuilder(MyTypeRegistry myTypeRegistry
            , MyRuntimeWiring myRuntimeWiring
            , SchemaRepository schemaRepository
            ){
        this.myTypeRegistry = myTypeRegistry;
        this.myRuntimeWiring = myRuntimeWiring;
        this.schemaRepository = schemaRepository;
    }

    @PostConstruct
    public GraphQL createGraphQl(){
        myTypeRegistry.initSchemaDefinition();
        myTypeRegistry.initTypeDefinition();
        myRuntimeWiring.initRuntimeWiring();
//        schemaRepository.findAll().forEach(this::addNewTypeAndDataFetcherInGraphQl);

        Schema schema = schemaRepository.findById("5ea393a23541b77e6d0052b7").get();
        this.addNewTypeAndDataFetcherInGraphQl(schema);
        schema = schemaRepository.findById("5df1ee417a47184df89fde67").get();
        this.addNewTypeAndDataFetcherInGraphQl(schema);
        schema = schemaRepository.findById("5df1eedb7a47184df89fde6a").get();
        this.addNewTypeAndDataFetcherInGraphQl(schema);

        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(myTypeRegistry.getTypeDefinitionRegistry(), myRuntimeWiring.getRuntimeWiring());
        return  newGraphQL(graphQLSchema).build();
    }

    public Map<String, TypeDefinition> getTypeDefinitionsInGraphQl(){
        return myTypeRegistry.getFieldDefinitionsInMyTypeRegistry();
    }

    public GraphQL addTypeInGraphQl(Schema schema){
        this.addNewTypeAndDataFetcherInGraphQl(schema);
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(myTypeRegistry.getTypeDefinitionRegistry(), myRuntimeWiring.getRuntimeWiring());
        return  newGraphQL(graphQLSchema).build();
    }

    public GraphQL updateTypeInGraphQl(Schema schema){
        this.updateTypeAndDataFetcherInGraphQl(schema);
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(myTypeRegistry.getTypeDefinitionRegistry(), myRuntimeWiring.getRuntimeWiring());
        return  newGraphQL(graphQLSchema).build();
    }

    public GraphQL deleteTypeInGraphQl(Schema schema){
        this.deleteTypeAndDataFetcherInGraphQl(schema);
        this.deleteTypeAndDataFetcherInGraphQl(schema);
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(myTypeRegistry.getTypeDefinitionRegistry(), myRuntimeWiring.getRuntimeWiring());
        return  newGraphQL(graphQLSchema).build();
    }

    private void updateTypeAndDataFetcherInGraphQl(Schema schema){
        myTypeRegistry.updateFieldDefinitions(schema.getDocumentTypeName(),schema.getTypeMap());
        if(!schema.getLinkList().isEmpty()){
            myRuntimeWiring.updateDataFetcherByName(schema.getDocumentTypeName(),addDataFetchers(schema.getLinkList()));
        }else myRuntimeWiring.deleteDataFetcherByName(schema.getDocumentTypeName());
    }

    private void deleteTypeAndDataFetcherInGraphQl(Schema schema){
        //delete orderDocumentList in Query
        myTypeRegistry.deleteFieldDefinitionsInQueryType(schema.getSchemaTypeListName());
        //delete orderDocument in Query
        myTypeRegistry.deleteFieldDefinitionsInQueryType(schema.getSchemaTypeName());
        //delete orderDocument
        myTypeRegistry.deleteTypeDefinition(schema.getDocumentTypeName());

        myRuntimeWiring.deleteEntryInQueryDataFetcher(schema.getSchemaTypeListName());

        myRuntimeWiring.deleteEntryInQueryDataFetcher(schema.getSchemaTypeName());

        myRuntimeWiring.deleteDataFetcherByName(schema.getDocumentTypeName());
    }

    private void addNewTypeAndDataFetcherInGraphQl(Schema schema){
        //Type orderDocument{...}
        myTypeRegistry.addTypeDefinition(schema.getDocumentTypeName(),schema.getTypeMap());

        List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("id",new TypeName("String")));
        //orderDocument(id:String):OrderDocument
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getSchemaTypeName(),new TypeName(schema.getDocumentTypeName()),inputValueDefinitions);


        //input filter Type in Type
        myTypeRegistry.addInputObjectTypeDefinition(schema.getFilterTypeName(), schema.getFilterMap());

        //orderDocumentList:[OrderDocument]
        inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("filter",new TypeName(schema.getFilterTypeName())));
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getSchemaTypeListName(),new ListType(new TypeName(schema.getDocumentTypeName())),inputValueDefinitions);

        //orderDocumentList ==> documentListDataFetcher
        DocumentListDataFetcher documentListDataFetcher = new DocumentListDataFetcher(this.mongoTemplate);
        documentListDataFetcher.setDocumentCollectionName(schema.getDocumentCollectionName());
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getSchemaTypeListName(), documentListDataFetcher);

        //orderDocument ==>  documentDataFetcher
        DocumentDataFetcher documentDataFetcher = new DocumentDataFetcher(this.mongoTemplate);
        documentDataFetcher.setDocumentCollectionName(schema.getDocumentCollectionName());
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getSchemaTypeName(), documentDataFetcher);

        //embedded Type in Type
        if(!schema.getLinkList().isEmpty()){
            myRuntimeWiring.addDataFetchers(schema.getDocumentTypeName(),addDataFetchers(schema.getLinkList()));
        }
    }

    private Map<String,DataFetcher> addDataFetchers(List<Link> linkList){
        Map<String,DataFetcher> dataFetcherMap = new HashMap<>();
        linkList.forEach(link -> {
            if(link.getLinkType().equals("Link")){
                DocumentDataFetcher documentDataFetcher1 = new DocumentDataFetcher(this.mongoTemplate);
                documentDataFetcher1.setDocumentCollectionName(link.getCollectionName());
                documentDataFetcher1.setKeyNameInParent(link.getName());
                dataFetcherMap.put(link.getName(),documentDataFetcher1);
            }else if(link.getLinkType().equals("LinkList")){
                DocumentListDataFetcher documentListDataFetcher1 = new DocumentListDataFetcher(this.mongoTemplate);
                documentListDataFetcher1.setDocumentCollectionName(link.getCollectionName());
                documentListDataFetcher1.setKeyNameInParent(link.getName());
                dataFetcherMap.put(link.getName(),documentListDataFetcher1);
            }
        });
        return dataFetcherMap;
    }
}
