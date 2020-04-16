package com.sddm.querybuilder.graphQl;

import com.mongodb.MongoClient;
import com.sddm.querybuilder.domain.Schema;
import com.sddm.querybuilder.repository.SchemaRepository;
import graphql.GraphQL;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.TypeName;
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

        Schema schema = schemaRepository.findById("5df1ed8d7a47184df89fde63").get();
        this.addNewTypeAndDataFetcherInGraphQl(schema);
        schema = schemaRepository.findById("5df1ee417a47184df89fde67").get();
        this.addNewTypeAndDataFetcherInGraphQl(schema);
        schema = schemaRepository.findById("5df1eedb7a47184df89fde6a").get();
        this.addNewTypeAndDataFetcherInGraphQl(schema);
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(myTypeRegistry.getTypeDefinitionRegistry(), myRuntimeWiring.getRuntimeWiring());
        return  newGraphQL(graphQLSchema).build();
    }

    public GraphQL updateGraphQl(Schema schema){
        this.addNewTypeAndDataFetcherInGraphQl(schema);
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(myTypeRegistry.getTypeDefinitionRegistry(), myRuntimeWiring.getRuntimeWiring());
        return  newGraphQL(graphQLSchema).build();
    }


    private void addNewTypeAndDataFetcherInGraphQl(Schema schema){

        //Type orderDocument{...}
        myTypeRegistry.addTypeDefinition(schema.getDocumentTypeName(),schema.getTypeMap());

        List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("id",new TypeName("String")));

        //orderDocument(id:String):OrderDocument
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getSchemaTypeName(),new TypeName(schema.getDocumentTypeName()),inputValueDefinitions);

        //orderDocumentList:[OrderDocument]
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getSchemaTypeListName(),new ListType(new TypeName(schema.getDocumentTypeName())));

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
            Map<String, DataFetcher> dataFetcherMap = new HashMap<>();
            schema.getLinkList().forEach(item->{
                DocumentDataFetcher documentDataFetcher1 = new DocumentDataFetcher(this.mongoTemplate);
                documentDataFetcher1.setDocumentCollectionName(item.getCollectionName());
                documentDataFetcher1.setKeyNameInParent(item.getName());
                dataFetcherMap.put(item.getName(),documentDataFetcher1);
            });
            myRuntimeWiring.addDataFetchers(schema.getDocumentTypeName(),dataFetcherMap);
        }
    }
}
