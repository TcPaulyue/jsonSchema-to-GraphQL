package com.sddm.querybuilder.graphQl;

import com.sddm.querybuilder.domain.Link;
import com.sddm.querybuilder.domain.Schema;
import com.sddm.querybuilder.domain.Status;
import com.sddm.querybuilder.repository.SchemaRepository;
import graphql.GraphQL;
import graphql.language.*;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import org.javers.core.Javers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(GraphQlBuilder.class);
    private MyTypeRegistry myTypeRegistry;
    private MyRuntimeWiring myRuntimeWiring;
    private SchemaRepository schemaRepository;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Javers javers;

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
        List<Schema> schemaList = schemaRepository.findAllByStatus(Status.Created);
        for(Schema schema:schemaList){
            logger.info("add schema existed in mongodb "+schema.getId());
            this.addNewTypeAndDataFetcherInGraphQl(schema);
        }
//        Schema schema = schemaRepository.findById("5ea393a23541b77e6d0052b7").get();
//        this.addNewTypeAndDataFetcherInGraphQl(schema);
//        schema = schemaRepository.findById("5df1ee417a47184df89fde67").get();
//        this.addNewTypeAndDataFetcherInGraphQl(schema);
//        schema = schemaRepository.findById("5df1eedb7a47184df89fde6a").get();
//        this.addNewTypeAndDataFetcherInGraphQl(schema);

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
        String schemaName = schema.getSchemaTypeName();
        //delete orderDocumentList in Query
        myTypeRegistry.deleteFieldDefinitionsInQueryType(schema.getSchemaTypeListName(schemaName));
        //delete orderDocument in Query
        myTypeRegistry.deleteFieldDefinitionsInQueryType(schema.getSchemaTypeName());
        //delete createNewOrder in Query
        myTypeRegistry.deleteFieldDefinitionsInQueryType(schema.getCreateNewSchemaName(schemaName));
        //delete deleteOrder in Query
        myTypeRegistry.deleteFieldDefinitionsInQueryType(schema.getDeleteSchemaName(schemaName));
        //delete orderCommits in Query
        myTypeRegistry.deleteFieldDefinitionsInQueryType(schema.getDocumentCommitsName(schemaName));

        //delete orderDocument
        myTypeRegistry.deleteTypeDefinition(schema.getDocumentTypeName());

        myRuntimeWiring.deleteEntryInQueryDataFetcher(schema.getSchemaTypeListName(schemaName));

        myRuntimeWiring.deleteEntryInQueryDataFetcher(schema.getSchemaTypeName());

        myRuntimeWiring.deleteEntryInQueryDataFetcher(schema.getCreateNewSchemaName(schemaName));

        myRuntimeWiring.deleteEntryInQueryDataFetcher(schema.getDeleteSchemaName(schemaName));

        myRuntimeWiring.deleteEntryInQueryDataFetcher(schema.getDocumentCommitsName(schemaName));

        myRuntimeWiring.deleteDataFetcherByName(schema.getDocumentTypeName());

        myRuntimeWiring.deleteDataFetcherByName(schema.getDocumentCommitsTypeName(schemaName));
    }

    private void addNewTypeAndDataFetcherInGraphQl(Schema schema){
        //order
        String schemaName = schema.getSchemaTypeName();
        //Type orderDocument{...}
        myTypeRegistry.addTypeDefinition(schema.getDocumentTypeName(),schema.getTypeMap());
        //newOrder(schemaId:String,content:orderInputs):Order
        this.addCreateNewDocumentTypeInQuery(schema);
        //updateOrder(id: String,content:orderInputs):Order
        this.addUpdateDocumentByIdInQuery(schema);
        //deleteOrder(id: String):String
        this.addDeleteDocumentByIdInQuery(schema);
        //orderDocument(id:String):Order
        this.addDocumentTypeInQuery(schema);
        //orderDocuments(filter:orderFilters):[Order!]
        this.addDocumentListTypeInQuery(schema);
        //orderCommits(id:String):[DocumentCommits!]
        this.addDocumentCommitsInQuery(schema);

        //orderDocumentList ==> documentListDataFetcher
        DocumentListDataFetcher documentListDataFetcher = new DocumentListDataFetcher(this.mongoTemplate);
        documentListDataFetcher.setDocumentCollectionName(schema.getDocumentCollectionName(schemaName));
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getSchemaTypeListName(schemaName), documentListDataFetcher);

        //orderDocument ==>  documentDataFetcher
        DocumentDataFetcher documentDataFetcher = new DocumentDataFetcher(this.mongoTemplate);
        documentDataFetcher.setDocumentCollectionName(schema.getDocumentCollectionName(schemaName));
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getSchemaTypeName(), documentDataFetcher);

        //createNewOrder ==> createDocumentMutation
        CreateDocumentMutation documentMutation = new CreateDocumentMutation(this.mongoTemplate,this.javers);
        documentMutation.setDocumentCollectionName(schema.getDocumentCollectionName(schemaName));
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getCreateNewSchemaName(schemaName),documentMutation);

        //updateOrder ==> updateDocumentMutation
        UpdateDocumentMutation updateDocumentMutation = new UpdateDocumentMutation(this.mongoTemplate,this.javers);
        updateDocumentMutation.setDocumentCollectionName(schema.getDocumentCollectionName(schemaName));
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getUpdateSchemaName(schemaName),updateDocumentMutation);

        //deleteOrder ==> deleteDocumentMutation
        DeleteDocumentMutation deleteDocumentMutation = new DeleteDocumentMutation(this.mongoTemplate,this.javers);
        deleteDocumentMutation.setDocumentCollectionName(schema.getDocumentCollectionName(schemaName));
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getDeleteSchemaName(schemaName),deleteDocumentMutation);

        //orderCommits ==> DocumentCommitsMutation
        DocumentCommitsMutation documentCommitsMutation = new DocumentCommitsMutation(mongoTemplate,javers);
        documentCommitsMutation.setDocumentCollectionName(schema.getDocumentCollectionName(schemaName));
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getDocumentCommitsName(schemaName),documentCommitsMutation);

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

    private void addDocumentTypeInQuery(Schema schema){
        List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("id",new TypeName("String")));
        //orderDocument(id:String):OrderDocument
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getSchemaTypeName(),new TypeName(schema.getDocumentTypeName()),inputValueDefinitions);
    }

    private void addDocumentListTypeInQuery(Schema schema){
        String schemaName =schema.getSchemaTypeName();
        //input filter Type in Type
        myTypeRegistry.addInputObjectTypeDefinition(schema.getFilterTypeName(schemaName), schema.getFilterMap());
        //orderDocumentList:[OrderDocument]
        List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("filter",new TypeName(schema.getFilterTypeName(schemaName))));
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getSchemaTypeListName(schemaName),new ListType(new TypeName(schema.getDocumentTypeName())),inputValueDefinitions);
    }

    private void addCreateNewDocumentTypeInQuery(Schema schema){
        String schemaName = schema.getSchemaTypeName();
        myTypeRegistry.addInputObjectTypeDefinition(schema.getInputTypeName(schemaName),schema.getInputMap());
        List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("content",new TypeName(schema.getInputTypeName(schemaName))));
        inputValueDefinitions.add(new InputValueDefinition("schemaId",new TypeName("String")));
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getCreateNewSchemaName(schemaName),new TypeName(schema.getDocumentTypeName()),inputValueDefinitions);
    }

    private void addUpdateDocumentByIdInQuery(Schema schema){
        String schemaName = schema.getSchemaTypeName();
        List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("content",new TypeName(schema.getInputTypeName(schemaName))));
        inputValueDefinitions.add(new InputValueDefinition("id",new TypeName("String")));
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getUpdateSchemaName(schemaName),new TypeName(schema.getDocumentTypeName()),inputValueDefinitions);
    }

    private void addDeleteDocumentByIdInQuery(Schema schema){
        String schemaName = schema.getSchemaTypeName();
        List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("id",new TypeName("String")));
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getDeleteSchemaName(schemaName),new TypeName("DeleteResult"),inputValueDefinitions);
    }

    private void addDocumentCommitsInQuery(Schema schema){
        String schemaName = schema.getSchemaTypeName();
        //register DocumentCommits Type
        Map<String,Type> commitsTypeMap = new HashMap<>();
        commitsTypeMap.put("commitId",new TypeName("String"));
        commitsTypeMap.put("commitDate",new TypeName("String"));
        commitsTypeMap.put("data",new TypeName(schema.getDocumentTypeName()));
        myTypeRegistry.addTypeDefinition(schema.getDocumentCommitsTypeName(schemaName),commitsTypeMap);
        //init documentCommits(id:String,name:String):[DocumentCommits] in Query
        List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("id",new TypeName("String")));
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getDocumentCommitsName(schemaName)
                ,new ListType(new TypeName(schema.getDocumentCommitsTypeName(schemaName))),inputValueDefinitions);

    }
}
