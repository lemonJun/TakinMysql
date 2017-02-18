package com.lemonjun.mysql.orm.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.lemonjun.mysql.orm.annotation.Column;
import com.lemonjun.mysql.orm.annotation.Id;
import com.lemonjun.mysql.orm.annotation.NotDBColumn;
import com.lemonjun.mysql.orm.annotation.ProcedureName;
import com.lemonjun.mysql.orm.annotation.Table;
import com.lemonjun.mysql.orm.annotation.TableRename;

class ClassInfo {

    /**
     * key: field.getName()  value:field
     */
    private Map<String, Field> mapIDField;

    /**
     * key: field.getName()  value:field
     */
    private Map<String, Field> mapAllDBField;

    /**
     * key: field.getName()  value:field
     */
    private Map<String, Field> mapInsertableField;

    /**
     * key: field.getName()  value:field
     */
    private Map<String, Field> mapUpdatableField;

    /**
     * key: field.getName()  value:field
     */
    private Map<String, Field> mapIdentityField;

    /**
     * key: field.getName()  value:db column name
     */
    private Map<String, String> mapDBColumnName;

    /**
     * key: field.getName()  value:Method
     */
    private Map<String, Method> mapSetMethod;

    /**
     * key: field.getName()  value:Method
     */
    private Map<String, Method> mapGetMethod;

    /**
     * key: field.getName()  value:field
     * add by haoxb
     * 针对表结构相同，表名不同的个性处理
     * 针对表结构相同，表名不同的情况，通过相同的一个实体类做到兼容
     */
    private Map<String, Field> mapTableRenameField;
    /**
     * key: field.getName()  value:Method
     */
    private Map<String, Method> mapTableRenameSetMethod;

    /**
     * key: field.getName()  value:Method
     */
    private Map<String, Method> mapTableRenameGetMethod;

    private String tableName;

    private ProcedureName procdure;

    public ClassInfo(Class<?> clazz) throws Exception {
        this.setTableName(getTableName(clazz));
        this.setMapIDField(getIdFields(clazz));
        this.setMapAllDBField(getAllDBFields(clazz));
        this.setMapInsertableField(getInsertableFields(clazz));
        this.setMapUpdatableField(getUpdatableFields(clazz));
        this.setMapDBColumnName(getCloumnName(clazz));
        this.setMapSetMethod(getSetterMethod(clazz));
        this.setMapGetMethod(getGetterMethod(clazz));
        this.setProcdure(getProc(clazz));
        this.setMapIdentityField(getIdentityFields(clazz));
        /**
         * 针对表结构相同，表名不同的个性处理
         * @author haoxb 2013-05-15
         */
        this.setMapTableRenameField(getTableRenameFields(clazz));
        this.setMapTableRenameGetMethod(getTableRenameGetterMethod(clazz));
        this.setMapTableRenameSetMethod(getTableRenameSetterMethod(clazz));
    }

    /**
     * 获得表名
     * @param clazz
     * @return
     */
    private String getTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = (Table) clazz.getAnnotation(Table.class);
            if (!table.name().equalsIgnoreCase("className")) {
                return table.name();
            }
        }
        String name = clazz.getName();
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * 获得所有允许插入的字段
     * @param bean
     * @return
     */
    private Map<String, Field> getInsertableFields(Class<?> clazz) {
        Map<String, Field> mapFfields = new HashMap<String, Field>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (!f.isAnnotationPresent(NotDBColumn.class)) {
                if (f.isAnnotationPresent(Id.class)) {
                    Id id = (Id) f.getAnnotation(Id.class);
                    if (id.insertable()) {
                        mapFfields.put(f.getName(), f);
                    }
                } else {
                    //					mapFfields.put(f.getName(), f);
                    Column column = (Column) f.getAnnotation(Column.class);
                    if (column != null) {
                        if (!column.defaultDBValue()) {
                            mapFfields.put(f.getName(), f);
                        }
                    } else {
                        mapFfields.put(f.getName(), f);
                    }
                }
            }
        }
        return mapFfields;
    }

    /**
     * 获得DB所有字段
     * @param bean
     * @return
     */
    private Map<String, Field> getAllDBFields(Class<?> clazz) {
        Map<String, Field> mapFfields = new HashMap<String, Field>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (!f.isAnnotationPresent(NotDBColumn.class)) {
                if (!"serialVersionUID".equalsIgnoreCase(f.getName())) {
                    mapFfields.put(f.getName(), f);
                }
            }
        }
        return mapFfields;
    }

    /**
     * 获得主键字段
     * @param clazz
     * @return
     */
    private Map<String, Field> getIdFields(Class<?> clazz) {
        Map<String, Field> mapFfields = new HashMap<String, Field>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(Id.class)) {
                mapFfields.put(f.getName(), f);
            }
        }
        return mapFfields;
    }

    /**
     * 获得所有允许更新的字段
     * @param clazz
     * @return
     */
    private Map<String, Field> getUpdatableFields(Class<?> clazz) {
        Map<String, Field> mapFfields = new HashMap<String, Field>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (!f.isAnnotationPresent(NotDBColumn.class)) {
                if (f.isAnnotationPresent(Id.class)) {
                    Id id = (Id) f.getAnnotation(Id.class);
                    if (id.updatable()) {
                        mapFfields.put(f.getName(), f);
                    }
                } else {
                    //					mapFfields.put(f.getName(), f);
                    Column column = (Column) f.getAnnotation(Column.class);
                    if (column != null) {
                        if (!column.defaultDBValue()) {
                            mapFfields.put(f.getName(), f);
                        }
                    } else {
                        mapFfields.put(f.getName(), f);
                    }
                }
            }
        }
        return mapFfields;
    }

    /**
     * 获得字段名
     * @param fList
     * @return
     */
    private Map<String, String> getCloumnName(Class<?> clazz) {
        Map<String, String> mapNames = new HashMap<String, String>();
        Collection<Field> fList = mapAllDBField.values();
        for (Field f : fList) {
            if (f.isAnnotationPresent(Column.class)) {
                Column column = (Column) f.getAnnotation(Column.class);
                if (!column.name().equalsIgnoreCase("fieldName")) {
                    mapNames.put(f.getName(), column.name());
                } else {
                    mapNames.put(f.getName(), f.getName());
                }
            } else {
                mapNames.put(f.getName(), f.getName());
            }
        }
        return mapNames;
    }

    /*
     * 获得字段对应的set方法
     */
    private Map<String, Method> getSetterMethod(Class<?> clazz) throws Exception {
        Map<String, Method> mapMethod = new HashMap<String, Method>();
        Collection<Field> fList = mapAllDBField.values();
        PropertyDescriptor[] lPropDesc = Introspector.getBeanInfo(clazz, Introspector.USE_ALL_BEANINFO).getPropertyDescriptors();

        for (Field f : fList) {
            for (PropertyDescriptor aLPropDesc : lPropDesc) {
                if (aLPropDesc.getName().equalsIgnoreCase(f.getName())) {
                    Method setMethod = aLPropDesc.getWriteMethod();
                    mapMethod.put(f.getName(), setMethod);
                    break;
                }
            }
        }

        for (Field f : fList) {
            Method setterMethod = mapMethod.get(f.getName());
            if (setterMethod == null) {
                String setFunName = "";
                if (f.isAnnotationPresent(Column.class)) {
                    Column column = (Column) f.getAnnotation(Column.class);
                    if (!column.setFuncName().equalsIgnoreCase("setField")) {
                        setFunName = column.setFuncName();
                    } else {
                        setFunName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                    }
                } else {
                    setFunName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                }

                for (Method m : clazz.getMethods()) {
                    if (m.getName().equals(setFunName)) {
                        setterMethod = m;
                        break;
                    }
                }

                mapMethod.put(f.getName(), setterMethod);
            }
        }

        for (Field f : fList) {
            Method setterMethod = mapMethod.get(f.getName());
            if (setterMethod == null) {
                throw new Exception("can't find set method field:" + f.getName() + "  class:" + clazz.getName());
            }
        }

        return mapMethod;
    }

    /*
     * 获得字段对应的get方法
     */
    private Map<String, Method> getGetterMethod(Class<?> clazz) throws Exception {
        Map<String, Method> mapMethod = new HashMap<String, Method>();
        Collection<Field> fList = mapAllDBField.values();
        PropertyDescriptor[] lPropDesc = Introspector.getBeanInfo(clazz, Introspector.USE_ALL_BEANINFO).getPropertyDescriptors();

        for (Field f : fList) {
            for (PropertyDescriptor aLPropDesc : lPropDesc) {
                if (aLPropDesc.getName().equalsIgnoreCase(f.getName())) {
                    Method getMethod = aLPropDesc.getReadMethod();
                    mapMethod.put(f.getName(), getMethod);
                    break;
                }
            }
        }

        for (Field f : fList) {
            Method getterMethod = mapMethod.get(f.getName());
            String getFunName = "";
            if (f.isAnnotationPresent(Column.class)) {
                Column column = (Column) f.getAnnotation(Column.class);
                if (!column.getFuncName().equalsIgnoreCase("getField")) {
                    getFunName = column.getFuncName();
                } else {
                    getFunName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                }
            } else {
                getFunName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            }

            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(getFunName)) {
                    getterMethod = m;
                    break;
                }
            }
            mapMethod.put(f.getName(), getterMethod);
        }

        for (Field f : fList) {
            Method getterMethod = mapMethod.get(f.getName());
            if (getterMethod == null) {
                throw new Exception("can't find get method field:" + f.getName() + "  class:" + clazz.getName());
            }
        }

        return mapMethod;
    }

    private ProcedureName getProc(Class<?> clazz) {
        return clazz.getAnnotation(ProcedureName.class);
    }

    /*
     * 获得自增字段
     */
    private Map<String, Field> getIdentityFields(Class<?> clazz) {
        Map<String, Field> mapField = new HashMap<String, Field>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(Id.class)) {
                Id id = (Id) f.getAnnotation(Id.class);
                if (!id.insertable() && !id.updatable()) {
                    mapField.put(f.getName(), f);
                }
            }
        }
        return mapField;
    }

    /**
     * 获得重命名表名
     * @param clazz
     * @return
     */
    private Map<String, Field> getTableRenameFields(Class<?> clazz) {
        Map<String, Field> mapTableRenamefields = new HashMap<String, Field>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (f.isAnnotationPresent(TableRename.class)) {
                mapTableRenamefields.put(f.getName(), f);
            }
        }
        return mapTableRenamefields;
    }

    /**
     * 获得TableRename字段对应的set方法
     * @author haoxb 2013-05-15
     */
    private Map<String, Method> getTableRenameSetterMethod(Class<?> clazz) throws Exception {
        Map<String, Method> mapMethod = new HashMap<String, Method>();
        Collection<Field> fList = mapTableRenameField.values();
        PropertyDescriptor[] lPropDesc = Introspector.getBeanInfo(clazz, Introspector.USE_ALL_BEANINFO).getPropertyDescriptors();

        for (Field f : fList) {
            for (PropertyDescriptor aLPropDesc : lPropDesc) {
                if (aLPropDesc.getName().equalsIgnoreCase(f.getName())) {
                    Method setMethod = aLPropDesc.getWriteMethod();
                    mapMethod.put(f.getName(), setMethod);
                    break;
                }
            }
        }

        for (Field f : fList) {
            Method setterMethod = mapMethod.get(f.getName());
            if (setterMethod == null) {
                String setFunName = "";
                if (f.isAnnotationPresent(Column.class)) {
                    Column column = (Column) f.getAnnotation(Column.class);
                    if (!column.setFuncName().equalsIgnoreCase("setField")) {
                        setFunName = column.setFuncName();
                    } else {
                        setFunName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                    }
                } else {
                    setFunName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                }

                for (Method m : clazz.getMethods()) {
                    if (m.getName().equals(setFunName)) {
                        setterMethod = m;
                        break;
                    }
                }

                mapMethod.put(f.getName(), setterMethod);
            }
        }

        for (Field f : fList) {
            Method setterMethod = mapMethod.get(f.getName());
            if (setterMethod == null) {
                throw new Exception("can't find set method field:" + f.getName() + "  class:" + clazz.getName());
            }
        }

        return mapMethod;
    }

    /**
     * 获得TableRename字段对应的get方法
     * @author haoxb 2013-05-15
     */
    private Map<String, Method> getTableRenameGetterMethod(Class<?> clazz) throws Exception {
        Map<String, Method> mapMethod = new HashMap<String, Method>();
        Collection<Field> fList = mapTableRenameField.values();
        PropertyDescriptor[] lPropDesc = Introspector.getBeanInfo(clazz, Introspector.USE_ALL_BEANINFO).getPropertyDescriptors();

        for (Field f : fList) {
            for (PropertyDescriptor aLPropDesc : lPropDesc) {
                if (aLPropDesc.getName().equalsIgnoreCase(f.getName())) {
                    Method getMethod = aLPropDesc.getReadMethod();
                    mapMethod.put(f.getName(), getMethod);
                    break;
                }
            }
        }

        for (Field f : fList) {
            Method getterMethod = mapMethod.get(f.getName());
            String getFunName = "";
            if (f.isAnnotationPresent(Column.class)) {
                Column column = (Column) f.getAnnotation(Column.class);
                if (!column.getFuncName().equalsIgnoreCase("getField")) {
                    getFunName = column.getFuncName();
                } else {
                    getFunName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                }
            } else {
                getFunName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            }

            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(getFunName)) {
                    getterMethod = m;
                    break;
                }
            }
            mapMethod.put(f.getName(), getterMethod);
        }

        for (Field f : fList) {
            Method getterMethod = mapMethod.get(f.getName());
            if (getterMethod == null) {
                throw new Exception("can't find get method field:" + f.getName() + "  class:" + clazz.getName());
            }
        }

        return mapMethod;
    }

    public Map<String, Field> getMapIDField() {
        return mapIDField;
    }

    public void setMapIDField(Map<String, Field> mapIDField) {
        this.mapIDField = mapIDField;
    }

    public Map<String, Field> getMapAllDBField() {
        return mapAllDBField;
    }

    public void setMapAllDBField(Map<String, Field> mapAllDBField) {
        this.mapAllDBField = mapAllDBField;
    }

    public Map<String, Field> getMapInsertableField() {
        return mapInsertableField;
    }

    public void setMapInsertableField(Map<String, Field> mapInsertableField) {
        this.mapInsertableField = mapInsertableField;
    }

    public Map<String, Field> getMapUpdatableField() {
        return mapUpdatableField;
    }

    public void setMapUpdatableField(Map<String, Field> mapUpdatableField) {
        this.mapUpdatableField = mapUpdatableField;
    }

    public Map<String, String> getMapDBColumnName() {
        return mapDBColumnName;
    }

    public void setMapDBColumnName(Map<String, String> mapDBColumnName) {
        this.mapDBColumnName = mapDBColumnName;
    }

    public Map<String, Method> getMapSetMethod() {
        return mapSetMethod;
    }

    public void setMapSetMethod(Map<String, Method> mapSetMethod) {
        this.mapSetMethod = mapSetMethod;
    }

    public Map<String, Method> getMapGetMethod() {
        return mapGetMethod;
    }

    public void setMapGetMethod(Map<String, Method> mapGetMethod) {
        this.mapGetMethod = mapGetMethod;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setProcdure(ProcedureName procdure) {
        this.procdure = procdure;
    }

    public ProcedureName getProcdure() {
        return procdure;
    }

    public void setMapIdentityField(Map<String, Field> mapIdentityField) {
        this.mapIdentityField = mapIdentityField;
    }

    public Map<String, Field> getMapIdentityField() {
        return mapIdentityField;
    }

    public Map<String, Field> getMapTableRenameField() {
        return mapTableRenameField;
    }

    public void setMapTableRenameField(Map<String, Field> mapTableRenameField) {
        this.mapTableRenameField = mapTableRenameField;
    }

    public Map<String, Method> getMapTableRenameSetMethod() {
        return mapTableRenameSetMethod;
    }

    public void setMapTableRenameSetMethod(Map<String, Method> mapTableRenameSetMethod) {
        this.mapTableRenameSetMethod = mapTableRenameSetMethod;
    }

    public Map<String, Method> getMapTableRenameGetMethod() {
        return mapTableRenameGetMethod;
    }

    public void setMapTableRenameGetMethod(Map<String, Method> mapTableRenameGetMethod) {
        this.mapTableRenameGetMethod = mapTableRenameGetMethod;
    }

}