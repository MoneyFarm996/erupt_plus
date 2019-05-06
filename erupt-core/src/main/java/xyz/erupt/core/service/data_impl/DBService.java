package xyz.erupt.core.service.data_impl;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Tree;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.TabType;
import xyz.erupt.core.dao.EruptJapUtils;
import xyz.erupt.core.dao.EruptJpaDao;
import xyz.erupt.core.model.EruptFieldModel;
import xyz.erupt.core.model.EruptModel;
import xyz.erupt.core.model.Page;
import xyz.erupt.core.model.TreeModel;
import xyz.erupt.core.service.DataService;
import xyz.erupt.core.service.InitService;
import xyz.erupt.core.util.*;

import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by liyuepeng on 2019-03-06.
 */
@Service
public class DBService implements DataService {

    @Autowired
    private EruptJpaDao eruptJpaDao;

    @Override
    public Page queryList(EruptModel eruptModel, JsonObject condition, Page page) {
        return eruptJpaDao.queryEruptList(eruptModel, condition, page);
    }

    @Override
    public Collection findTabListById(EruptModel eruptModel, String tabFieldName, Serializable id) {
        Object obj = eruptJpaDao.findDataById(eruptModel, id);
        try {
            Field tabField = obj.getClass().getDeclaredField(tabFieldName);
            tabField.setAccessible(true);
            Collection collection = (Collection) tabField.get(obj);
            for (Object tabData : collection) {
                EruptUtil.rinseEruptObj(tabData);
            }
            return collection;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List findTabList(EruptFieldModel eruptTabFieldModel) {
        return null;
//        return this.findTabListById(eruptTabFieldModel, null);
    }

    @Override
    public Set findTabTreeById(EruptModel eruptModel, String tabFieldName, Serializable id) {
        Collection collection = findTabListById(eruptModel, tabFieldName, id);
        Set<String> idSet = new HashSet<>();
        for (Object o : collection) {
            try {
                Field field = ReflectUtil.findClassAllField(o.getClass(), eruptModel.getErupt().primaryKeyCol());
                field.setAccessible(true);
                idSet.add(field.get(o).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return idSet;
    }

    @Override
    public List<TreeModel> findTabTree(EruptFieldModel eruptTabFieldModel) {
        EruptModel subEruptModel = InitService.ERUPTS.get(eruptTabFieldModel.getFieldReturnName());
        TabType tabType = eruptTabFieldModel.getEruptField().edit().tabType()[0];
        String condition = "";
        if (!"".equals(tabType.filter().condition())) {
            condition += AnnotationUtil.switchFilterConditionToStr(tabType.filter());
        }
        return treeDataUtil(subEruptModel, condition, null);
    }

    @Override
    public List<TreeModel> queryTree(EruptModel eruptModel) {
        return treeDataUtil(eruptModel, null, null);
    }

    private List<TreeModel> treeDataUtil(EruptModel eruptModel, String condition, String sort) {
        Tree tree = eruptModel.getErupt().tree();
        List<String> cols = new ArrayList<>();
        cols.add(EruptJapUtils.completeHqlPath(eruptModel.getEruptName(), tree.id()) + " as " + tree.id().replace(".", "_"));
        cols.add(EruptJapUtils.completeHqlPath(eruptModel.getEruptName(), tree.label()) + " as " + tree.label().replace(".", "_"));
        if (StringUtils.isNotBlank(tree.pid())) {
            cols.add(EruptJapUtils.completeHqlPath(eruptModel.getEruptName(), tree.pid()) + " as " + tree.pid().replace(".", "_"));
        }
        List list = eruptJpaDao.getDataMap(eruptModel, condition, sort, cols);
        List<TreeModel> treeModels = new ArrayList<>();
        for (Object o : list) {
            Map<String, Object> map = (Map) o;
            TreeModel treeModel = new TreeModel(map.get(tree.id()), map.get(tree.label()), map.get(tree.pid().replace(".", "_")), null);
            treeModels.add(treeModel);
        }
        if (StringUtils.isBlank(tree.pid())) {
            return treeModels;
        } else {
            return DataHandlerUtil.treeModelToTree(treeModels);
        }
    }

    @Override
    public Object findDataById(EruptModel eruptModel, Serializable id) {
        return eruptJpaDao.findDataById(eruptModel, id);
    }

    @Override
    public List getReferenceList(EruptModel eruptModel, String fieldName) {
        return eruptJpaDao.getReferenceList(eruptModel, fieldName);
    }

    @Override
    public Collection<TreeModel> getReferenceTree(EruptModel eruptModel, String fieldName) {
        return getReferenceTreeByDepend(eruptModel, fieldName, null);
    }

    @Override
    public Collection<TreeModel> getReferenceTreeByDepend(EruptModel eruptModel, String fieldName, String dependValue) {
        EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(fieldName);
        ReferenceTreeType refTree = eruptFieldModel.getEruptField().edit().referenceTreeType()[0];
        List<String> cols = new ArrayList<>();
        cols.add(EruptJapUtils.completeHqlPath(eruptFieldModel.getFieldReturnName(), refTree.id()) + " as " + refTree.id().replace(".", "_"));
        cols.add(EruptJapUtils.completeHqlPath(eruptFieldModel.getFieldReturnName(), refTree.label()) + " as " + refTree.label().replace(".", "_"));
        if (StringUtils.isNotBlank(refTree.pid())) {
            cols.add(EruptJapUtils.completeHqlPath(eruptFieldModel.getFieldReturnName(), refTree.pid()) + " as " + refTree.pid().replace(".", "_"));
        }
        StringBuilder condition = new StringBuilder();
        if (!"".equals(refTree.filter().condition())) {
            condition.append(AnnotationUtil.switchFilterConditionToStr(refTree.filter()));
        }
        if (!"".equals(refTree.depend()) && null != dependValue) {
            //TODO Depend value use function
            condition.append(EruptJapUtils.AND).append("");
        }
        List list = eruptJpaDao.getDataMap(InitService.ERUPTS.get(eruptFieldModel.getFieldReturnName()), condition.toString(), null, cols);
        List<TreeModel> treeModels = new ArrayList<>();
        for (Object o : list) {
            Map<String, Object> map = (Map) o;
            TreeModel treeModel = new TreeModel(map.get(refTree.id()), map.get(refTree.label()), map.get(refTree.pid().replace(".", "_")), null);
            treeModels.add(treeModel);
        }
        if (StringUtils.isBlank(refTree.pid())) {
            return treeModels;
        } else {
            return DataHandlerUtil.treeModelToTree(treeModels);
        }
    }

    @Transactional
    @Override
    public void addData(EruptModel eruptModel, Object object) {
        try {
            eruptJpaDao.addEntity(eruptModel, object);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException(gcRepeatHint(eruptModel));
        }

    }

    @Transactional
    @Override
    public void editData(EruptModel eruptModel, Object object) {
        try {
            eruptJpaDao.editEntity(eruptModel, object);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            throw new RuntimeException(gcRepeatHint(eruptModel));
        }
    }

    //生成数据重复的提示字符串
    private String gcRepeatHint(EruptModel eruptModel) {
        StringBuilder str = new StringBuilder();
        for (UniqueConstraint uniqueConstraint : eruptModel.getClazz().getAnnotation(Table.class).uniqueConstraints()) {
            for (String columnName : uniqueConstraint.columnNames()) {
                EruptField eruptField = eruptModel.getEruptFieldMap().get(columnName).getEruptField();
                str.append(eruptField.views()[0].title()).append(" ");
            }
        }
        str.append("重复");
        return str.toString();
    }

    @Transactional
    @Override
    public void deleteData(EruptModel eruptModel, Serializable id) {
        Object obj = eruptJpaDao.findDataById(eruptModel, id);
        eruptJpaDao.deleteEntity(obj);
    }
}
