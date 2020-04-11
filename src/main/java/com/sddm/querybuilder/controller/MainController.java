package com.sddm.querybuilder.controller;

import com.sddm.querybuilder.graphQl.GraphQlBuilder;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    private GraphQL graphQL;

    @Autowired
    public MainController(GraphQlBuilder graphQlBuilder){
        this.graphQL = graphQlBuilder.createGraphQl();
    }

    @PostMapping(value = "/query")
    public ResponseEntity query(@RequestBody String query){
        ExecutionResult result = graphQL.execute(query);
        System.out.println("errors: "+result.getErrors());
        return ResponseEntity.ok(result.getData());
    }
}
