## complecateFuture-wrapper
分离 action 与 callback

### Definition Callback
```$xslt
    // 定义Action, 不需要带泛型
    SupplierAction actionA = new SupplierAction<>("actionA", () -> fetchA());
    SupplierAction actionB = new SupplierAction<>("actionB", () -> fetchB());

    SupplierAction<CInfo> actionC = new SupplierAction("actionC");
    actionC.setAction(() -> fetchC(
            // fetchC(): a.say(), b.say()
            actionC.getProp(0, AInfo.class),
            actionC.getProp(1, BInfo.class),
            actionC.getProp(2, String.class)
    ));

    FunctionAction actionD = new FunctionAction<CInfo, DInfo>("actionD", (c) -> {
        c.say();
        return fetchD();
    });

    FunctionAction<DInfo, EInfo> actionE = new FunctionAction("actionE");
    actionE.setAction((d) -> {
        d.say();
        return fetchE(actionE.getProp(0, String.class));
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
    CompletableFuture result = ActionActuator.build(container)
            .call("actionA", "actionB")
            .callOfParam("actionC", getFuture("actionA"), getFuture("actionB"), "name-c")
            .andThen("actionD")
            .andThen("actionE", "name-e")
            .closeBranch();

    EInfo e = (EInfo) result.get();
    e.say();
```

### Run
```$xslt
This is A
This is B
This is name-c
This is D
This is name-e
```