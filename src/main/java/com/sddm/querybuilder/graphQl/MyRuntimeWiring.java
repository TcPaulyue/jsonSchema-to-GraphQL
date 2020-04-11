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

    public void initRuntimeWiring(){
        Map<String,DataFetcher> map = new LinkedHashMap<>();
        runtimeWiring.getDataFetchers().put("Query",map);
    }


    void addDataFetchers(String name, Map<String, DataFetcher> dataFetcherMap){
        runtimeWiring.getDataFetchers().put(name,dataFetcherMap);
    }

    public void addNewEntryInQueryDataFetcher(String name,DataFetcher dataFetcher){
        runtimeWiring.getDataFetchers().get("Query").put(name,dataFetcher);
    }

}
