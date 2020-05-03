# jsonSchema-to-GraphQL
a demo about query in mongodb, by translating a jsonSchema in mongodb to satisfy GraphQL without *.graphqls files dynamically.
<img src="https://github.com/TcPaulyue/jsonSchema-to-GraphQL/blob/master/architecture.png" style="zoom:50%;" />


## introduction

+ Swagger: http://localhost:8000/swagger-ui.html#

### json-schema说明

Json-schema的定义遵循基本的规范，有如下的一些说明：

#### 基本类型

filter=true表示可以作为查询时的筛选条件，filter=false则不可以
```json
   "XXX": {
      "type": "number","string","date"
      "filter": true 
    }
```

#### Link

```shell
###schema Type = A 
"B": {    
        "linkTo" :"B"   //A链接的schema Type为B
        "collectionName": "sddm_customers",  //对应的collectionName
        "type" : "Link",   //link类型表示单个文档的链接操作
        "filter": false   //不可作为筛选对象
    },
```

#### LinkList

```shell
###schema Type = A 
"B": {    
        "linkTo" :"B"   //A链接的schema Type为B
        "collectionName": "sddm_customers",  //对应的collectionName
        "type" : "LinkList",   //link类型表示单个文档的链接操作
        "filter": false   //不可作为筛选对象
    },
```

#### Embedded

```shell
    "B": {
      "type": "Embedded",  //内嵌文档
      "typeName":"B",   //内嵌文档的类型
      "filter": false
    },
```

#### EmbeddedList

```shell
    "BList": {
      "type": "EmbeddedList",  //内嵌文档
      "typeName":"B",   //内嵌文档的类型
      "filter": false
    },
```

### 文档操作接口

对文档的增删改查有统一的接口：`localhost:8000/api/documents/query`

#### 查询

查询利用graphql的query写法，举例如下：

假设有schema为`order`,它的schema定义如下：

```json
{
  "type": "object",
  "title": "empty object",
  "properties": {
    "OrderID": {
      "type": "number",
      "filter": true
    },
    "Customer": {
        "linkTo" : "Customer",
        "linkType" : "Single",
        "collectionName" : "sddm_customers",
        "type" : "Link",
        "filter": false
    },
    "Customers": {
      "linkTo" : "Customer",
      "collectionName" : "sddm_customers",
      "type" : "LinkList",
      "filter": false
    },
    "Employee": {
      "type": "Embedded",
      "typeName": "Employee",
      "filter": false
    },
    "Employees": {
      "type": "EmbeddedList",
      "typeName": "Employee",
      "filter": false
    },
    "OrderDate": {
      "type": "date",
      "format": "date",
      "filter": true
    },
    "Freight": {
      "type": "number",
      "filter": true
    },
    "ShipName": {
      "type": "string",
      "filter": true
    }
  },
"description": "Order",
"required": [
"ShipCountry",
"ShipPostalCode",
"ShipRegion",
"ShipName",
"Freight",
"OrderDate",
"Employee",
"Employees",
"Customer",
"Customers",
"OrderID"
]
}
```



```shell
//查询整个order文档列表
{
	orderDocuments(filter:{
		OrderID: "XXX",
		...
	}){
		OrderID
		OrderDate
		Employee{
			employeeId
			...
		}
		Employees{
			employeeId
			...
		}
		Customer{
			customerId
			name
			...
		}
		Customers{
			customerId
			name
			...
		}
	}
}

//查询某个具体的order文档
{
	orderDocument(id : "xxxxxx"){
		OrderID
		OrderDate
		Employee{
			employeeId
			...
		}
		Employees{
			employeeId
			...
		}
		Customer{
			customerId
			name
			...
		}
		Customers{
			customerId
			name
			...
		}
	}
}

//创建文档
{
	createNewOrder(
	schemaId: "5ea393a23541b77e6d0052b7",
	content:{
		OrderID : 10249,
    Customer : "5df1f62917e609bb64d920d3",
    Customers: ["5df1f62917e609bb64d920d4","5df1f62917e609bb64d920d5"],
    Employee : {
    	employeeID : 3,
    	lastName : "Leverling",
    	firstName : "Janet",
    	title : "sddm_employees",
    	titleOfCourtesy : "Ms.",
    	birthDate : "1963-08-30 00:00:00.000",
    	hireDate : "1992-04-01 00:00:00.000",
    	address : "722 Moss Bay Blvd.",
    	city : "Kirkland"
    }
    Employees : [
    		{
          lastName : "Fuller",
    			firstName : "Janet",
    			title : "sddm_employees",
    			titleOfCourtesy : "Ms.",
    			birthDate : "1963-08-30 00:00:00.000",
    			hireDate : "1992-04-01 00:00:00.000",
    			address : "722 Moss Bay Blvd.",
    			city : "Kirkland"
        }, 
        {
          lastName : "Leverling",
    			firstName : "Janet",
    			title : "sddm_employees",
    			...
        }
    	]
		}
	){
		OrderID
		Customer{
			ContactName
			...
		}
		Employee{
			employeeID
		}
		Employees{
			lastName
		}
	}
}

//更新文档
{
	updateOrder(
	id:"5eaa8e2dee9d5042b910da5d",
	content:{
		//同上
	}){
		OrderID
		Customer{
			ContactName
		}
		Employee{
			employeeID
		}
		Employees{
			lastName
		}
	}
}

//删除文档
{
	deleteOrder(id: "5eaa8e2dee9d5042b910da5d"){
		deleteResult
	}
}

//获取某个文档的整个版本记录
{
	orderCommits(id:"5eaa8e2dee9d5042b910da5d"){
		commitId      //commit的id
		commitDate   //commit的时间
		data{    //文档具体内容
			OrderID
			Employee{
				employeeID
    			lastName
    			firstName
			}
			Customer{
				CompanyName
				CustomerID
			}
		}
	}
}
```

