## complecateFuture-wrapper
分离 action 与 callback, 用简洁的方式处理异步操作树. 只需要.call(name) .then(name, before) .anyOf(name, ...before)的组合就能轻松完成业务逻辑.

![alt allOf-function](http://www.runyuanj.com/action/supplier-func-consumer.png)

### Definition Callback
```$xslt
    // 定义Action, 不需要带泛型
    SupplierAction actionA = new SupplierAction<>("actionA", () -> fetchA());
    SupplierAction actionB = new SupplierAction<>("actionB", () -> fetchB());
    
    // 带有入参的action分2步创建, 可以使用泛型.
    // <CInfo> in left is necessary
    SupplierAction<CInfo> actionC = new SupplierAction("actionC");
    actionC.setAction(() -> fetchC(
           // fetchC(): a.say(), b.say()
           actionC.getProp(0, AInfo.class),
           actionC.getProp(1, BInfo.class),
           actionC.getProp(2, String.class)
    ));
    
    // <CInfo, DInfo> in right is necessary
    FunctionAction actionD = new FunctionAction<CInfo, DInfo>("actionD", (c) -> {
       c.say();
       return fetchD();
    });
    
    // consumer表达式可以修改入参对象, 前提是使用sync(), 但是不能返回其他对象
    ConsumerAction<DInfo> actionE = new ConsumerAction("actionE");
    actionE.setAction((d) -> {
       d.say();
       d.setName("D from actionE");
    });
```

### Add To Container
```$xslt
    // 创建容器并添加action
    ActionDefinitionContainer container = new ActionDefinitionContainer()
            .addSupplier(actionA, actionB, actionC)
            .addFunction(actionD)
            .addConsumer(actionE);
```

### Definition action sequence
```$xslt
    ActionActuator actuator = ActionActuator.build(container);
    CompletableFuture e = actuator.call("actionA", "actionB")
            .callOfParam("actionC", getFuture("actionA"), getFuture("actionB"), "name-c")
            .andThen("actionD")
            .andThen("actionE", "name-e")
            .closeBranch();

    Object result = e.get();
    System.out.println(result); // consumer return null

    CompletableFuture d = actuator.getResult("actionD");
    DInfo dInfo = (DInfo) d.get();
    dInfo.say(); // This is D from actionE
```

### Run
```$xslt
This is A
This is B
This is name-c
This is D
null
This is D from action
```
### anyOf
![alt anyOf(consumer)](http://www.runyuanj.com/action/anyof_consumer.png)
![alt anyOf(function)](http://www.runyuanj.com/action/anyof_function.png)

C等待A,B其中任意一个完成, CompletableFuture要求anyOf的lambda必须使用Consumer或Function, 但是Function的返回值无效. 不知道是bug还是限制.

![alt anyOf-function-consumer](http://www.runyuanj.com/action/anyof-func-consumer.png)

### allOf
![alt allOf-function](http://www.runyuanj.com/action/allof-function.png)

