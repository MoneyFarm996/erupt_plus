package xyz.erupt.core.controller;import com.google.gson.Gson;import com.google.gson.JsonObject;import lombok.RequiredArgsConstructor;import lombok.SneakyThrows;import lombok.extern.slf4j.Slf4j;import org.apache.commons.lang3.StringUtils;import org.springframework.web.bind.annotation.*;import xyz.erupt.annotation.EruptField;import xyz.erupt.annotation.SceneEnum;import xyz.erupt.annotation.sub_erupt.LinkTree;import xyz.erupt.annotation.sub_field.EditType;import xyz.erupt.core.annotation.EruptRecordOperate;import xyz.erupt.core.annotation.EruptRouter;import xyz.erupt.core.config.GsonFactory;import xyz.erupt.core.constant.EruptRestPath;import xyz.erupt.core.exception.EruptNoLegalPowerException;import xyz.erupt.core.invoke.DataProcessorManager;import xyz.erupt.core.invoke.DataProxyInvoke;import xyz.erupt.core.invoke.PowerInvoke;import xyz.erupt.core.naming.EruptOperateConfig;import xyz.erupt.core.service.EruptCoreService;import xyz.erupt.core.service.EruptService;import xyz.erupt.core.service.IEruptDataService;import xyz.erupt.core.util.EruptUtil;import xyz.erupt.core.util.ReflectUtil;import xyz.erupt.core.view.EruptApiModel;import xyz.erupt.core.view.EruptModel;import javax.servlet.http.HttpServletRequest;import javax.transaction.Transactional;import java.lang.reflect.Field;import java.util.Collection;/** * Erupt 对数据的增删改查 * * @author YuePeng * date 9/28/18. */@Slf4j@RestController@RequestMapping(EruptRestPath.ERUPT_DATA_MODIFY)@RequiredArgsConstructorpublic class EruptModifyController {    private final Gson gson = GsonFactory.getGson();    private final EruptService eruptService;    @SneakyThrows    @PostMapping({"/{erupt}"})    @EruptRecordOperate(value = "新增", dynamicConfig = EruptOperateConfig.class)    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    @Transactional    public EruptApiModel addEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data,                                      JsonObject jsonObject, HttpServletRequest request) {        EruptModel eruptModel = EruptCoreService.getErupt(erupt);        if (PowerInvoke.getPowerObject(eruptModel).isAdd()) {            LinkTree dependTree = eruptModel.getErupt().linkTree();            if (StringUtils.isNotBlank(dependTree.field()) && dependTree.dependNode()) {                String linkVal = request.getHeader("link");                //必须是强依赖才会自动注入值                if (dependTree.dependNode()) {                    if (StringUtils.isBlank(linkVal)) {                        return EruptApiModel.errorApi("请选择树节点");                    } else {                        if (null == jsonObject) {                            jsonObject = new JsonObject();                        }                        String rm = ReflectUtil.findClassField(eruptModel.getClazz(), dependTree.field()).getType().getSimpleName();                        JsonObject sub = new JsonObject();                        sub.addProperty(EruptCoreService.getErupt(rm).getErupt().primaryKeyCol(), linkVal);                        jsonObject.add(dependTree.field(), sub);                    }                }            }            EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(eruptModel, data);            if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) {                return eruptApiModel;            }            Object o = gson.fromJson(data.toString(), eruptModel.getClazz());            this.clearObjectDefaultValueByJson(o, data);            Object obj = this.dataTarget(eruptModel, o, eruptModel.getClazz().newInstance(), SceneEnum.ADD);            if (null != jsonObject) {                for (String key : jsonObject.keySet()) {                    Field field = ReflectUtil.findClassField(eruptModel.getClazz(), key);                    field.setAccessible(true);                    field.set(obj, gson.fromJson(jsonObject.get(key).toString(), field.getType()));                }            }            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeAdd(obj)));            DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).addData(eruptModel, obj);            this.modifyLog(eruptModel, "ADD", data.toString());            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterAdd(obj)));            return EruptApiModel.successApi();        } else {            throw new EruptNoLegalPowerException();        }    }    @PutMapping("/{erupt}")    @EruptRecordOperate(value = "修改", dynamicConfig = EruptOperateConfig.class)    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    @Transactional    public EruptApiModel editEruptData(@PathVariable("erupt") String erupt, @RequestBody JsonObject data) throws IllegalAccessException {        EruptModel eruptModel = EruptCoreService.getErupt(erupt);        if (PowerInvoke.getPowerObject(eruptModel).isEdit()) {            EruptApiModel eruptApiModel = EruptUtil.validateEruptValue(eruptModel, data);            if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) {                return eruptApiModel;            }            if (!eruptService.verifyIdPermissions(eruptModel, data.get(eruptModel.getErupt().primaryKeyCol()).getAsString())) {                throw new EruptNoLegalPowerException();            }            Object o = this.gson.fromJson(data.toString(), eruptModel.getClazz());            this.clearObjectDefaultValueByJson(o, data);            Object obj = this.dataTarget(eruptModel, o, DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())                    .findDataById(eruptModel, ReflectUtil.findClassField(eruptModel.getClazz(), eruptModel.getErupt().primaryKeyCol()).get(o)), SceneEnum.EDIT);            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeUpdate(obj)));            DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz()).editData(eruptModel, obj);            this.modifyLog(eruptModel, "EDIT", data.toString());            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterUpdate(obj)));            return EruptApiModel.successApi();        } else {            throw new EruptNoLegalPowerException();        }    }    private Object dataTarget(EruptModel eruptModel, Object data, Object target, SceneEnum sceneEnum) {        ReflectUtil.findClassAllFields(eruptModel.getClazz(), (field) -> {            EruptField eruptField = field.getAnnotation(EruptField.class);            if (null != eruptField) {                boolean readonly = sceneEnum == SceneEnum.EDIT                        ? eruptField.edit().readonly().edit()                        : eruptField.edit().readonly().add();                if (StringUtils.isNotBlank(eruptField.edit().title()) && !readonly) {                    try {                        Field f = ReflectUtil.findClassField(eruptModel.getClazz(), field.getName());                        if (eruptField.edit().type() == EditType.TAB_TABLE_ADD) {                            Collection<?> s = (Collection<?>) f.get(target);                            if (null == s) {                                f.set(target, f.get(data));                            } else {                                s.clear();                                s.addAll((Collection) f.get(data));                                f.set(target, s);                            }                        } else {                            f.set(target, f.get(data));                        }                    } catch (IllegalAccessException e) {                        e.printStackTrace();                    }                }            }        });        return target;    }    //清理序列化后对象所产生的默认值（通过json串进行校验）    private void clearObjectDefaultValueByJson(Object obj, JsonObject data) {        ReflectUtil.findClassAllFields(obj.getClass(), field -> {            try {                field.setAccessible(true);                if (null != field.get(obj)) {                    if (!data.has(field.getName())) {                        field.set(obj, null);                    }                }            } catch (IllegalAccessException e) {                e.printStackTrace();            }        });    }    @DeleteMapping("/{erupt}/{id}")    @EruptRecordOperate(value = "删除", dynamicConfig = EruptOperateConfig.class)    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    @Transactional    public EruptApiModel deleteEruptData(@PathVariable("erupt") String erupt, @PathVariable("id") String id) {        EruptModel eruptModel = EruptCoreService.getErupt(erupt);        if (PowerInvoke.getPowerObject(eruptModel).isDelete()) {            if (!eruptService.verifyIdPermissions(eruptModel, id)) {                throw new EruptNoLegalPowerException();            }            IEruptDataService dataService = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz());            //获取对象数据信息用于DataProxy函数中            Object obj = dataService.findDataById(eruptModel, EruptUtil.toEruptId(eruptModel, id));            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.beforeDelete(obj)));            dataService.deleteData(eruptModel, obj);            this.modifyLog(eruptModel, "DELETE", id);            DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterDelete(obj)));            return EruptApiModel.successApi();        } else {            throw new EruptNoLegalPowerException();        }    }    //为了事务性考虑所以增加了批量删除功能    @Transactional    @DeleteMapping("/{erupt}")    @EruptRouter(skipAuthIndex = 3, authIndex = 1, verifyType = EruptRouter.VerifyType.ERUPT)    @EruptRecordOperate(value = "批量删除", dynamicConfig = EruptOperateConfig.class)    public EruptApiModel deleteEruptDataList(@PathVariable("erupt") String erupt, @RequestParam("ids") String[] ids) {        EruptApiModel eruptApiModel = EruptApiModel.successApi();        for (String id : ids) {            eruptApiModel = this.deleteEruptData(erupt, id);            if (eruptApiModel.getStatus() == EruptApiModel.Status.ERROR) {                break;            }        }        return eruptApiModel;    }    private void modifyLog(EruptModel eruptModel, String placeholder, String content) {        log.info("[" + eruptModel.getEruptName() + " -> " + placeholder + "]:" + content);    }}