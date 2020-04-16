package com.sddm.querybuilder.controller;

import com.sddm.querybuilder.domain.Schema;
import com.sddm.querybuilder.graphQl.GraphQlBuilder;
import com.sddm.querybuilder.repository.SchemaRepository;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    private GraphQL graphQL;
    private GraphQlBuilder graphQlBuilder;
    private SchemaRepository schemaRepository;
    @Autowired
    public MainController(GraphQlBuilder graphQlBuilder,SchemaRepository schemaRepository){
        this.graphQL = graphQlBuilder.createGraphQl();
        this.graphQlBuilder = graphQlBuilder;
        this.schemaRepository = schemaRepository;
    }

    @PostMapping(value = "/query")
    public ResponseEntity query(@RequestBody String query){
        ExecutionResult result = graphQL.execute(query);
        System.out.println("errors: "+result.getErrors());
        return ResponseEntity.ok(result.getData());
    }

    @GetMapping(value = "/dynamicRefresh")
    public String update(){
        Schema schema = schemaRepository.findById("5df1f04e7a47184df89fde72").get();
        this.graphQL = graphQlBuilder.updateGraphQl(schema);
        return "Success";
    }
}
