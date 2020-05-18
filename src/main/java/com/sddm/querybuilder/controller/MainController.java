package com.sddm.querybuilder.controller;

import com.alibaba.fastjson.JSONObject;
import com.sddm.querybuilder.domain.Fomily;
import com.sddm.querybuilder.domain.Schema;
import com.sddm.querybuilder.domain.Status;
import com.sddm.querybuilder.graphQl.GraphQlBuilder;
import com.sddm.querybuilder.repository.FomilyRepository;
import com.sddm.querybuilder.repository.SchemaRepository;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.javers.core.Javers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MainController {
    private GraphQL graphQL;
    private GraphQlBuilder graphQlBuilder;
    private SchemaRepository schemaRepository;
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final Javers javers;
    private FomilyRepository fomilyRepository;

    @Autowired
    public MainController(GraphQlBuilder graphQlBuilder, SchemaRepository schemaRepository
            , Javers javers, FomilyRepository fomilyRepository){
        this.graphQL = graphQlBuilder.createGraphQl();
        this.graphQlBuilder = graphQlBuilder;
        this.schemaRepository = schemaRepository;
        this.javers = javers;
        this.fomilyRepository = fomilyRepository;
    }

    @CrossOrigin
    @PostMapping(value = "documents/query")
    public ResponseEntity query(@RequestBody String query){
        ExecutionResult result = graphQL.execute(query);
        logger.info("errors: "+result.getErrors());
        if(result.getErrors().isEmpty())
            return ResponseEntity.ok(result.getData());
        else return ResponseEntity.badRequest().body(result.getErrors());
    }

//    @GetMapping(value = "/types")
//    public Map<String, TypeDefinition> getTypes(){
//        return this.graphQlBuilder.getTypeDefinitionsInGraphQl();
//    }
    @GetMapping("/schemas")
    public List<Schema> getSchemas() {
        logger.info("get all schemas.");
        return schemaRepository.findAllByStatus(Status.Created);
    }

    @PostMapping("/fomily/new")
    public Fomily createFomily(@RequestBody JSONObject params){
        logger.info("create new Fomily.");
        Fomily fomily = new Fomily();
        fomily.setSchemaContent(params);
        return fomilyRepository.save(fomily);
    }

    @PutMapping("/fomily/{id}")
    public Fomily updateFomily(@PathVariable String id
            , @RequestBody JSONObject params) {
        logger.info("update fomily by Id " + id);
        fomilyRepository.findById(id).ifPresent(fomily -> {
            fomily.setSchemaContent(params);
            fomily = fomilyRepository.save(fomily);
        });
        return fomilyRepository.findById(id).get();
    }

    @PostMapping("/schemas/new")
    public Schema createSchema(@RequestBody JSONObject params) {
        logger.info("create new Schema.");
        Schema schema = new Schema();
        schema.setSchemaContent(params);
        schema.setStatus(Status.Created);
        schema = schemaRepository.save(schema);
        javers.commit(schema.getId(),schema);
        this.graphQL = graphQlBuilder.addTypeInGraphQl(schema);
        return schema;
    }

    @GetMapping("/fomily/{fomilyId}")
    public Fomily getFomilyWithId(@PathVariable String fomilyId){
        return fomilyRepository.findById(fomilyId).get();
    }

    @GetMapping("/schemas/{schemaId}")
    public Schema getSchemaWithId(@PathVariable String schemaId){
        return schemaRepository.findById(schemaId).get();
    }

    @DeleteMapping("/schemas/{id}")
    public List<Schema> deleteSchema(@PathVariable String id) {
        logger.info("delete schema by id "+id);
        schemaRepository.findById(id).ifPresent(schema -> {
            schema.setStatus(Status.Deleted);
            schema = schemaRepository.save(schema);
            javers.commit(schema.getId(),schema);
            this.graphQL = graphQlBuilder.deleteTypeInGraphQl(schema);
        });
        return schemaRepository.findAllByStatus(Status.Created);
    }

    @PutMapping("/schemas/{id}")
    public Schema updateSchema(@PathVariable String id
            , @RequestBody JSONObject params) {
        logger.info("update schema by Id " + id);
        schemaRepository.findById(id).ifPresent(schema -> {
            schema.setSchemaContent(params);
            schema = schemaRepository.save(schema);
            javers.commit(schema.getId(),schema);
            this.graphQL = graphQlBuilder.updateTypeInGraphQl(schema);
        });
        return schemaRepository.findById(id).get();
    }
}
