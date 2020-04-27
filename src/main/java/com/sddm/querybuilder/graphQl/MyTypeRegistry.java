package com.sddm.querybuilder.graphQl;

import graphql.language.*;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MyTypeRegistry {
    private TypeDefinitionRegistry typeDefinitionRegistry;

    @Autowired
    MyTypeRegistry(){
        this.typeDefinitionRegistry = new TypeDefinitionRegistry();
    }

    TypeDefinitionRegistry getTypeDefinitionRegistry() {
        return typeDefinitionRegistry;
    }

    void initSchemaDefinition(){
        SchemaDefinition schemaDefinition = new SchemaDefinition();
        OperationTypeDefinition operationTypeDefinition = new OperationTypeDefinition("query",new TypeName("Query"));
        schemaDefinition.getOperationTypeDefinitions().add(operationTypeDefinition);
        typeDefinitionRegistry.add(schemaDefinition);
    }

    void initTypeDefinition(){
        Map<String, Type> typeMap = new HashMap<>();
        typeDefinitionRegistry.add(newObjectTypeDefinition("Query",newFieldDefinitions(typeMap)));
    }

    void addInputObjectTypeDefinition(String name,Map<String,Type> typeMap){
        InputObjectTypeDefinition inputObjectTypeDefinition = new InputObjectTypeDefinition(name);
        inputObjectTypeDefinition.getInputValueDefinitions().add(new InputValueDefinition("OR",new ListType(new TypeName(name))));
        inputObjectTypeDefinition.getInputValueDefinitions().add(new InputValueDefinition("AND",new ListType(new TypeName(name))));
        typeMap.forEach((key,value)-> inputObjectTypeDefinition.getInputValueDefinitions().add(new InputValueDefinition(key,value)));
        typeDefinitionRegistry.add(inputObjectTypeDefinition);
    }

    void addTypeDefinition(String name, Map<String, Type> typeMap){
        typeDefinitionRegistry.add(newObjectTypeDefinition(name,newFieldDefinitions(typeMap)));
    }

    //todo: typeDefinitionRegistry.types()不能获取
    void deleteTypeDefinition(String name){
//        typeDefinitionRegistry.getType(name).ifPresent(typeDefinition -> {
//            if(typeDefinition instanceof ObjectTypeDefinition)
//                ((ObjectTypeDefinition) typeDefinition).getFieldDefinitions().clear();
//        });
        typeDefinitionRegistry.types().remove(name);
    }

    void addFieldDefinitionsInQueryType(String name, Type type){
        typeDefinitionRegistry.getType("Query").ifPresent(queryType->{
            if(queryType instanceof ObjectTypeDefinition)
                ((ObjectTypeDefinition) queryType).getFieldDefinitions().add(new FieldDefinition(name,type));
        });
    }

    void deleteFieldDefinitionsInQueryType(String name){
        typeDefinitionRegistry.getType("Query").ifPresent(queryType->{
            if(queryType instanceof ObjectTypeDefinition)
                ((ObjectTypeDefinition) queryType).getFieldDefinitions().remove(this.getFieldDefinitionInQueryType(name));
        });
    }

    void updateFieldDefinitionsInQueryType(String name,Type type){
        typeDefinitionRegistry.getType("Query").ifPresent(queryType->{
            if(queryType instanceof ObjectTypeDefinition) {
                ((ObjectTypeDefinition) queryType).getFieldDefinitions().remove(this.getFieldDefinitionInQueryType(name));
                ((ObjectTypeDefinition) queryType).getFieldDefinitions().add(new FieldDefinition(name,type));
            }
        });
    }

    void updateFieldDefinitions(String name,Map<String, Type> typeMap){
        typeDefinitionRegistry.getType(name).ifPresent(typeDefinition -> {
            if(typeDefinition instanceof  ObjectTypeDefinition) {
                ((ObjectTypeDefinition) typeDefinition).getFieldDefinitions().clear();
                ((ObjectTypeDefinition) typeDefinition).getFieldDefinitions()
                        .addAll(newFieldDefinitions(typeMap));
            }
        });
    }

    private FieldDefinition getFieldDefinitionInQueryType(String name){
        if(typeDefinitionRegistry.getType("Query").get() instanceof ObjectTypeDefinition){
            for(FieldDefinition fieldDefinition:((ObjectTypeDefinition) typeDefinitionRegistry.getType("Query").get()).getFieldDefinitions()){
                if(fieldDefinition.getName().equals(name))
                    return fieldDefinition;
            }
        }
        return null;
    }

    private List<FieldDefinition> getFieldDefinitionsInType(String name){
        if(typeDefinitionRegistry.getType(name).get() instanceof ObjectTypeDefinition)
            return ((ObjectTypeDefinition)typeDefinitionRegistry.getType(name).get()).getFieldDefinitions();
        return null;
    }

    Map<String,TypeDefinition> getFieldDefinitionsInMyTypeRegistry(){
        return typeDefinitionRegistry.types();
    }

    void addFieldDefinitionsInQueryType(String name, Type type, List<InputValueDefinition> inputValueDefinitions){
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
        typeMap.forEach((name,Type)-> fieldDefinitions.add(new FieldDefinition(name,Type)));
        return fieldDefinitions;
    }

    private ObjectTypeDefinition newObjectTypeDefinition(String name, List<FieldDefinition> fieldDefinitions){
        ObjectTypeDefinition objectTypeDefinition = new ObjectTypeDefinition(name);
        fieldDefinitions.forEach(fieldDefinition -> objectTypeDefinition.getFieldDefinitions().add(fieldDefinition));
        return objectTypeDefinition;
    }
}
