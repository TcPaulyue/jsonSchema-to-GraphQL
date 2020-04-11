package com.sddm.querybuilder.graphQl;

import com.sddm.querybuilder.domain.Schema;
import com.sddm.querybuilder.repository.SchemaRepository;
import graphql.GraphQL;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.TypeName;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

import static graphql.GraphQL.newGraphQL;

@Component
public class GraphQlBuilder {
    private MyTypeRegistry myTypeRegistry;
    private MyRuntimeWiring myRuntimeWiring;
    private SchemaRepository schemaRepository;
    private DocumentListDataFetcher documentListDataFetcher;
    private DocumentDataFetcher documentDataFetcher;

    @Autowired
    GraphQlBuilder(MyTypeRegistry myTypeRegistry
            , MyRuntimeWiring myRuntimeWiring
            , SchemaRepository schemaRepository
            , DocumentListDataFetcher documentListDataFetcher
            ,DocumentDataFetcher documentDataFetcher){
        this.myTypeRegistry = myTypeRegistry;
        this.myRuntimeWiring = myRuntimeWiring;
        this.schemaRepository = schemaRepository;
        this.documentListDataFetcher = documentListDataFetcher;
        this.documentDataFetcher = documentDataFetcher;
    }

    @PostConstruct
    public GraphQL createGraphQl(){
        myTypeRegistry.initSchemaDefinition();
        myTypeRegistry.initTypeDefinition();
        myRuntimeWiring.initRuntimeWiring();
        Schema schema = schemaRepository.findById("5df1ed8d7a47184df89fde63").get();
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
        myTypeRegistry.addTypeDefinition(schema.getDocumentTypeName(),schema.getTypeMap());

        List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();
        inputValueDefinitions.add(new InputValueDefinition("id",new TypeName("String")));

        //orderDocument(id:String):OrderDocument
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getSchemaTypeName(),new TypeName(schema.getDocumentTypeName()),inputValueDefinitions);

        //orderDocumentList:[OrderDocument]
        myTypeRegistry.addFieldDefinitionsInQueryType(schema.getSchemaTypeListName(),new ListType(new TypeName(schema.getDocumentTypeName())));

        documentListDataFetcher.setDocumentCollectionName(schema.getDocumentCollectionName());
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getSchemaTypeListName(), documentListDataFetcher);

        documentDataFetcher.setDocumentCollectionName(schema.getDocumentCollectionName());
        myRuntimeWiring.addNewEntryInQueryDataFetcher(schema.getSchemaTypeName(), documentDataFetcher);
    }
}
