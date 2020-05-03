package com.sddm.querybuilder.graphQl;

import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MyRuntimeWiring {
    private RuntimeWiring runtimeWiring;
    @Autowired
    public MyRuntimeWiring(){
        runtimeWiring = RuntimeWiring.newRuntimeWiring().build();
    }

    RuntimeWiring getRuntimeWiring() {
        return runtimeWiring;
    }

    void initRuntimeWiring(){
        Map<String,DataFetcher> map = new LinkedHashMap<>();
        runtimeWiring.getDataFetchers().put("Query",map);

    }

    void addDataFetchers(String name, Map<String, DataFetcher> dataFetcherMap){
        runtimeWiring.getDataFetchers().put(name,dataFetcherMap);
    }

    void addNewEntryInQueryDataFetcher(String name, DataFetcher dataFetcher){
        runtimeWiring.getDataFetchers().get("Query").put(name,dataFetcher);
    }

    void deleteEntryInQueryDataFetcher(String name){
        runtimeWiring.getDataFetchers().get("Query").remove(name);
    }

    void deleteDataFetcherByName(String name){
        runtimeWiring.getDataFetchers().remove(name);
    }

    void updateDataFetcherByName(String name,Map<String, DataFetcher> dataFetcherMap){
        runtimeWiring.getDataFetchers().put(name,dataFetcherMap);
    }
}
