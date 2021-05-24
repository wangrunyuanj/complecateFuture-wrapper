## complecateFuture-wrapper
分离 action 与 callback

### Definition Callback
```$xslt
    // 定义Action
    SupplierAction<AInfo> actionA = new SupplierAction<>("actionA", () -> fetchA());
    SupplierAction<BInfo> actionB = new SupplierAction<>("actionB", () -> fetchB());
    
    SupplierAction actionC = new SupplierAction("actionC");
    // fetchC(): a.say(), b.say()
    actionC.setAction(() -> fetchC((AInfo) actionC.getProps()[0], (BInfo) actionC.getProps()[1], (String) actionC.getProps()[2]));
    
    FunctionAction actionD = new FunctionAction("actionD", (info) -> {
        CInfo c = (CInfo) info;
        c.say();
        return fetchD();
    });
    FunctionAction actionE = new FunctionAction("actionE", (info) -> {
        DInfo d = (DInfo) info;
        return fetchE(d); // d.say()
    });
```

### Add To Container
```$xslt
    // 添加到容器
    ActionDefinitionContainer container = new ActionDefinitionContainer();
    container.addSupplier(actionA, actionB, actionC).addFunction(actionD, actionE);
```

### Definition action sequence
```$xslt
    // 执行
    CompletableFuture result = ActuatorWrapper.build(container)
            .call("actionA", "actionB")
            .callOfParam("actionC", getFuture("actionA"), getFuture("actionB"), "name-c")
            .andThen("actionD")
            .andThen("actionE")
            .close();

    EInfo e = (EInfo) result.get();
    e.say();
```

### Run
```$xslt
This is A
This is B
This is name-c
This is D
This is E
```