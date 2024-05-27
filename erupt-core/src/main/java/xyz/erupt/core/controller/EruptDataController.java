package xyz.erupt.core.controller;import com.google.gson.Gson;import com.google.gson.JsonObject;import lombok.RequiredArgsConstructor;import lombok.SneakyThrows;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.springframework.web.bind.annotation.*;import xyz.erupt.annotation.config.QueryExpression;import xyz.erupt.annotation.constant.AnnotationConst;import xyz.erupt.annotation.fun.OperationHandler;import xyz.erupt.annotation.fun.PowerObject;import xyz.erupt.annotation.model.Row;import xyz.erupt.annotation.query.Condition;import xyz.erupt.annotation.sub_erupt.Filter;import xyz.erupt.annotation.sub_erupt.RowOperation;import xyz.erupt.annotation.sub_erupt.Tree;import xyz.erupt.annotation.sub_field.Edit;import xyz.erupt.annotation.sub_field.sub_edit.CheckboxType;import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTableType;import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;import xyz.erupt.core.annotation.EruptRecordOperate;import xyz.erupt.core.annotation.EruptRouter;import xyz.erupt.core.config.GsonFactory;import xyz.erupt.core.constant.EruptConst;import xyz.erupt.core.constant.EruptRestPath;import xyz.erupt.core.exception.EruptNoLegalPowerException;import xyz.erupt.core.i18n.I18nTranslate;import xyz.erupt.core.invoke.DataProcessorManager;import xyz.erupt.core.invoke.DataProxyInvoke;import xyz.erupt.core.invoke.ExprInvoke;import xyz.erupt.core.naming.EruptRowOperationNaming;import xyz.erupt.core.query.Column;import xyz.erupt.core.query.EruptQuery;import xyz.erupt.core.service.EruptCoreService;import xyz.erupt.core.service.EruptService;import xyz.erupt.core.service.PreEruptDataService;import xyz.erupt.core.util.EruptSpringUtil;import xyz.erupt.core.util.EruptUtil;import xyz.erupt.core.util.Erupts;import xyz.erupt.core.util.TypeUtil;import xyz.erupt.core.view.*;import java.io.Serializable;import java.util.*;import java.util.stream.Collectors;/** * @author YuePeng * date 9/28/18. */@RestController@RequestMapping(EruptRestPath.ERUPT_DATA)@RequiredArgsConstructor@Slf4jpublic class EruptDataController {    private final EruptService eruptService;    private final PreEruptDataService preEruptDataService;    public static final int MAX_PAGE_SIZE = 50000;    private final Gson gson = GsonFactory.getGson();    @PostMapping({"/table/{erupt}"})    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)    public Page getEruptData(@PathVariable("erupt") String eruptName, @RequestBody TableQuery tableQuery) {        if (tableQuery.getPageSize() > MAX_PAGE_SIZE) {            tableQuery.setPageSize(MAX_PAGE_SIZE);        }        return eruptService.getEruptData(EruptCoreService.getErupt(eruptName), tableQuery, null);    }    @GetMapping("/tree/{erupt}")    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)    public Collection<TreeModel> getEruptTreeData(@PathVariable("erupt") String eruptName) {        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);        Erupts.powerLegal(eruptModel, PowerObject::isQuery);        Tree tree = eruptModel.getErupt().tree();        return preEruptDataService.geneTree(eruptModel, tree.id(), tree.label(), tree.pid(), tree.rootPid(), EruptQuery.builder().build());    }    //获取初始化数据    @GetMapping("/init-value/{erupt}")    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)    public Map<String, Object> initEruptValue(@PathVariable("erupt") String eruptName) throws IllegalAccessException, InstantiationException {        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);        Object obj = eruptModel.getClazz().newInstance();        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.addBehavior(obj)));        return EruptUtil.generateEruptDataMap(eruptModel, obj);    }    @GetMapping("/{erupt}/{id}")    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    public Map<String, Object> getEruptDataById(@PathVariable("erupt") String eruptName, @PathVariable("id") String id) {        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isEdit() || powerObject.isViewDetails());        eruptService.verifyIdPermissions(eruptModel, id);        Object data = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())                .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id));        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.editBehavior(data)));        return EruptUtil.generateEruptDataMap(eruptModel, data);    }    public static final String OPERATOR_PATH_STR = "/operator";    /**     * Custom button form initialization values     *     * @param eruptName eruptName     * @param code      btn code     * @param ids       link ids     * @return form value     */    @PostMapping("/{erupt}" + OPERATOR_PATH_STR + "/{code}/form-value")    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    @SneakyThrows    public Object eruptOperatorFormValue(@PathVariable("erupt") String eruptName, @PathVariable("code") String code, @RequestParam List<Object> ids) {        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);        RowOperation rowOperation = Arrays.stream(eruptModel.getErupt().rowOperation()).filter(it -> code.equals(it.code())).findFirst().orElseThrow(EruptNoLegalPowerException::new);        EruptModel operatorErupt = EruptCoreService.getErupt(rowOperation.eruptClass().getSimpleName());        if (rowOperation.operationHandler().isInterface()) return null;        OperationHandler<Object, Object> operationHandler = EruptSpringUtil.getBean(rowOperation.operationHandler());        Object eruptForm = rowOperation.eruptClass().newInstance();        DataProxyInvoke.invoke(operatorErupt, (dataProxy -> dataProxy.addBehavior(eruptForm)));        try {            operationHandler.getClass().getDeclaredMethod("eruptFormValue", List.class, operatorErupt.getClazz(), String[].class);            return operationHandler.eruptFormValue(ids.stream().map(id -> DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())                    .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id.toString()))).collect(Collectors.toList()), eruptForm, rowOperation.operationParam());        } catch (NoSuchMethodException ignored) {        }        return eruptForm;    }    @PostMapping("/{erupt}" + OPERATOR_PATH_STR + "/{code}")    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    @EruptRecordOperate(value = "", dynamicConfig = EruptRowOperationNaming.class)    public EruptApiModel eruptOperatorExec(@PathVariable("erupt") String eruptName, @PathVariable("code") String code, @RequestBody JsonObject body) {        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);        RowOperation rowOperation = Arrays.stream(eruptModel.getErupt().rowOperation()).filter(it -> code.equals(it.code())).findFirst().orElseThrow(EruptNoLegalPowerException::new);        Erupts.powerLegal(ExprInvoke.getExpr(rowOperation.show()));        if (rowOperation.eruptClass() != void.class) {            EruptModel erupt = EruptCoreService.getErupt(rowOperation.eruptClass().getSimpleName());            EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(erupt, body.getAsJsonObject("param"));            if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) return eruptApiModel;        }        if (rowOperation.operationHandler().isInterface()) {            return EruptApiModel.errorApi("Please implement the 'OperationHandler' interface for " + rowOperation.title());        }        OperationHandler<Object, Object> operationHandler = EruptSpringUtil.getBean(rowOperation.operationHandler());        Object param = null;        if (!body.get("param").isJsonNull()) {            param = gson.fromJson(body.getAsJsonObject("param"), rowOperation.eruptClass());        }        if (rowOperation.mode() == RowOperation.Mode.BUTTON) {            String eval = operationHandler.exec(null, param, rowOperation.operationParam());            if (StringUtils.isNotBlank(eval)) {                return EruptApiModel.successApi(eval);            } else {                return EruptApiModel.successApi(I18nTranslate.$translate("erupt.exec_success"), null);            }        }        if (body.get("ids").isJsonArray() && body.getAsJsonArray("ids").size() > 0) {            List<Object> list = new ArrayList<>();            body.getAsJsonArray("ids").forEach(id -> list.add(DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())                    .findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id.getAsString()))));            String eval = operationHandler.exec(list, param, rowOperation.operationParam());            if (StringUtils.isNotBlank(eval)) {                return EruptApiModel.successApi(eval);            }        }        return EruptApiModel.successApi(I18nTranslate.$translate("erupt.exec_success"), null);    }    @GetMapping("/tab/tree/{erupt}/{tabFieldName}")    @EruptRouter(authIndex = 3, verifyType = EruptRouter.VerifyType.ERUPT)    public Collection<TreeModel> findTabTree(@PathVariable("erupt") String eruptName, @PathVariable("tabFieldName") String tabFieldName) {        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);//        Erupts.powerLegal(eruptModel, powerObject -> powerObject.isViewDetails() || powerObject.isEdit());        EruptModel tabEruptModel = EruptCoreService.getErupt(eruptModel.getEruptFieldMap().get(tabFieldName).getFieldReturnName());        Tree tree = tabEruptModel.getErupt().tree();        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(tabFieldName);        EruptQuery eruptQuery = EruptQuery.builder().conditionStrings(                Arrays.stream(eruptFieldModel.getEruptField().edit().filter()).map(Filter::value).collect(Collectors.toList())        ).build();        return preEruptDataService.geneTree(tabEruptModel, tree.id(), tree.label(), tree.pid(), tree.rootPid(), eruptQuery);    }    @GetMapping("/{erupt}/checkbox/{fieldName}")    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    public Collection<CheckboxModel<Object, Object, Object>> findCheckbox(@PathVariable("erupt") String eruptName, @PathVariable("fieldName") String fieldName) {        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);        EruptModel tabEruptModel = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());        CheckboxType checkboxType = eruptFieldModel.getEruptField().edit().checkboxType();        List<Column> columns = new ArrayList<>();        columns.add(new Column(checkboxType.id(), AnnotationConst.ID));        columns.add(new Column(checkboxType.label(), AnnotationConst.LABEL));        if (!AnnotationConst.EMPTY_STR.equals(checkboxType.remark())) {            columns.add(new Column(checkboxType.remark(), AnnotationConst.REMARK));        }        EruptQuery eruptQuery = EruptQuery.builder().conditionStrings(                Arrays.stream(eruptFieldModel.getEruptField().edit().filter()).map(Filter::value).collect(Collectors.toList())        ).build();        Collection<Map<String, Object>> collection = preEruptDataService.createColumnQuery(tabEruptModel, columns, eruptQuery);        Collection<CheckboxModel<Object, Object, Object>> checkboxModels = new ArrayList<>(collection.size());        collection.forEach(map -> checkboxModels.add(new CheckboxModel<>(map.get(AnnotationConst.ID), map.get(AnnotationConst.LABEL), map.get(AnnotationConst.REMARK))));        return checkboxModels;    }    // REFERENCE API    @PostMapping("/{erupt}/reference-table/{fieldName}")    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    public Page getReferenceTable(@PathVariable("erupt") String eruptName,                                  @PathVariable("fieldName") String fieldName,                                  @RequestParam(value = "dependValue", required = false) Serializable dependValue,                                  @RequestParam(value = "tabRef", required = false) Boolean tabRef,                                  @RequestBody TableQuery tableQuery) {        EruptModel eruptModel = EruptCoreService.getErupt(eruptName);        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);        Edit edit = eruptFieldModel.getEruptField().edit();        String dependField = edit.referenceTableType().dependField();        List<Condition> serverConditions = new ArrayList<>();        List<String> conditions = Arrays.stream(edit.filter()).map(Filter::value).collect(Collectors.toList());        if (!AnnotationConst.EMPTY_STR.equals(dependField)) {            Erupts.requireNonNull(dependValue, I18nTranslate.$translate("erupt.select") + " " + eruptModel.getEruptFieldMap().get(dependField).getEruptField().edit().title());            EruptModel refErupt = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());            serverConditions.add(new Condition(                    eruptFieldModel.getFieldReturnName() + EruptConst.DOT + edit.referenceTableType().dependColumn(),                    TypeUtil.typeStrConvertObject(dependValue, refErupt.getEruptFieldMap().get(refErupt.getErupt().primaryKeyCol()).getField().getType()),                    QueryExpression.EQ            ));        }        EruptModel eruptReferenceModel = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());        if (!tabRef) {            //由于类加载顺序问题，并未选择在启动时检测            ReferenceTableType referenceTableType = eruptFieldModel.getEruptField().edit().referenceTableType();            Erupts.requireTrue(eruptReferenceModel.getEruptFieldMap().containsKey(referenceTableType.label().split("\\.")[0])                    , eruptReferenceModel.getEruptName() + " not found '" + referenceTableType.label()                            + "' field，please use @ReferenceTableType annotation 'label' config");        }        return eruptService.getEruptData(eruptReferenceModel, tableQuery,                serverConditions, conditions.toArray(new String[0]));    }    @SneakyThrows    @GetMapping("/depend-tree/{erupt}")    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)    public Collection<TreeModel> getDependTree(@PathVariable("erupt") String erupt) {        EruptModel eruptModel = EruptCoreService.getErupt(erupt);        String field = eruptModel.getErupt().linkTree().field();        if (null == eruptModel.getEruptFieldMap().get(field)) {            String treeErupt = eruptModel.getClazz().getDeclaredField(field).getType().getSimpleName();            return this.getEruptTreeData(treeErupt);        }        return this.getReferenceTree(eruptModel.getEruptName(), field, null);    }    @GetMapping("/{erupt}/reference-tree/{fieldName}")    @EruptRouter(authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    public Collection<TreeModel> getReferenceTree(@PathVariable("erupt") String erupt,                                                  @PathVariable("fieldName") String fieldName,                                                  @RequestParam(value = "dependValue", required = false) Serializable dependValue) {        EruptModel eruptModel = EruptCoreService.getErupt(erupt);        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);        String dependField = eruptFieldModel.getEruptField().edit().referenceTreeType().dependField();        if (!AnnotationConst.EMPTY_STR.equals(dependField)) {            Erupts.requireNonNull(dependValue, I18nTranslate.$translate("erupt.select") + " " + eruptModel.getEruptFieldMap().get(dependField).getEruptField().edit().title());        }        Edit edit = eruptFieldModel.getEruptField().edit();        ReferenceTreeType treeType = edit.referenceTreeType();        EruptModel referenceEruptModel = EruptCoreService.getErupt(eruptFieldModel.getFieldReturnName());        Erupts.requireTrue(referenceEruptModel.getEruptFieldMap().containsKey(treeType.label().split("\\.")[0]),                referenceEruptModel.getEruptName() + " not found " + treeType.label() + " field, please use @ReferenceTreeType annotation config");        List<Condition> conditions = new ArrayList<>();        //处理depend参数代码        if (StringUtils.isNotBlank(treeType.dependField()) && null != dependValue) {            conditions.add(new Condition(edit.referenceTreeType().dependColumn(), dependValue, QueryExpression.EQ));        }        List<String> conditionStrings = Arrays.stream(edit.filter()).map(Filter::value).collect(Collectors.toList());        return preEruptDataService.geneTree(referenceEruptModel, treeType.id(), treeType.label(), treeType.pid(), treeType.rootPid(),                EruptQuery.builder().orderBy(edit.orderBy()).conditionStrings(conditionStrings).conditions(conditions).build());    }    //自定义行    @PostMapping("/extra-row/{erupt}")    @EruptRouter(authIndex = 2, verifyType = EruptRouter.VerifyType.ERUPT)    public List<Row> extraRow(@PathVariable("erupt") String erupt, @RequestBody TableQuery tableQuery) {        List<Row> rows = new ArrayList<>();        DataProxyInvoke.invoke(EruptCoreService.getErupt(erupt), dataProxy ->                Optional.ofNullable(dataProxy.extraRow(tableQuery.getCondition())).ifPresent(rows::addAll));        return rows;    }}