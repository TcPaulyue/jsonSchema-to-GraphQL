package com.sddm.querybuilder.graphQl;

import graphql.language.*;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MyTypeRegistry {
    private TypeDefinitionRegistry typeDefinitionRegistry;

    @Autowired
    MyTypeRegistry(){
        this.typeDefinitionRegistry = new TypeDefinitionRegistry();
    }

    public TypeDefinitionRegistry getTypeDefinitionRegistry() {
        return typeDefinitionRegistry;
    }

    public void initSchemaDefinition(){
        SchemaDefinition schemaDefinition = new SchemaDefinition();
        OperationTypeDefinition operationTypeDefinition = new OperationTypeDefinition("query",new TypeName("Query"));
        schemaDefinition.getOperationTypeDefinitions().add(operationTypeDefinition);
        typeDefinitionRegistry.add(schemaDefinition);
    }

    public void initTypeDefinition(){
        Map<String, Type> typeMap = new HashMap<>();
        typeDefinitionRegistry.add(newObjectTypeDefinition("Query",newFieldDefinitions(typeMap)));
    }

    public void addTypeDefinition(String name, Map<String,Type> typeMap){
        typeDefinitionRegistry.add(newObjectTypeDefinition(name,newFieldDefinitions(typeMap)));
    }

    public void addFieldDefinitionsInQueryType(String name,Type type){
        typeDefinitionRegistry.getType("Query").ifPresent(queryType->{
            if(queryType instanceof ObjectTypeDefinition)
                ((ObjectTypeDefinition) queryType).getFieldDefinitions().add(new FieldDefinition(name,type));
        });
    }

    public void addFieldDefinitionsInQueryType(String name,Type type,List<InputValueDefinition> inputValueDefinitions){
        FieldDefinition fieldDefinition = new FieldDefinition(name,type);
        inputValueDefinitions.forEach(inputValueDefinition -> {
            fieldDefinition.getInputValueDefinitions().add(inputValueDefinition);
        });
        typeDefinitionRegistry.getType("Query").ifPresent(queryType->{
            if(queryType instanceof ObjectTypeDefinition){
                ((ObjectTypeDefinition) queryType).getFieldDefinitions()
                        .add(fieldDefinition);
            }
        });
    }

    private List<FieldDefinition> newFieldDefinitions(Map<String, Type> typeMap){
        List<FieldDefinition> fieldDefinitions = new ArrayList<>();
        typeMap.forEach((name,Type)->{
            fieldDefinitions.add(new FieldDefinition(name,Type));
        });
        return fieldDefinitions;
    }

    private ObjectTypeDefinition newObjectTypeDefinition(String name, List<FieldDefinition> fieldDefinitions){
        ObjectTypeDefinition objectTypeDefinition = new ObjectTypeDefinition(name);
        fieldDefinitions.forEach(fieldDefinition -> {
            objectTypeDefinition.getFieldDefinitions().add(fieldDefinition);
        });
        return objectTypeDefinition;
    }

}
