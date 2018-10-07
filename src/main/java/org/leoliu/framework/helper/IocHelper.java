package org.leoliu.framework.helper;

import org.leoliu.framework.annotation.Inject;
import org.leoliu.framework.util.ArrayUtil;
import org.leoliu.framework.util.CollectionUtil;
import org.leoliu.framework.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 依赖注入助手
 */
public final class IocHelper {

    static {
        //先通过BeanHelper获取所有的BeanMap
        Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
        if (CollectionUtil.isNotEmpty(beanMap)){
            //遍历BeanMap，分别取出Bean类和实例
            for (Map.Entry<Class<?>, Object> beanEntry : beanMap.entrySet()){
                Class<?> beanClass = beanEntry.getKey();
                Object beanInstance = beanEntry.getValue();
                //通过反射获取所有类中所有成员变量
                Field[] beanFields = beanClass.getDeclaredFields();
                //遍历这些成员变量，判断是否带有inject注解
                if (ArrayUtil.isNotEmpty(beanFields)){
                    for (Field beanField : beanFields){
                        if(beanField.isAnnotationPresent(Inject.class)){
                            //若有，从BeanMap中根据Bean类取出成员变量的值
                            Class<?> beanFieldClass = beanField.getType();
                            Object beanFieldInstance = beanMap.get(beanFieldClass);
                            //通过ReflectionUtil的setField方法修改成员变量的值
                            if (beanFieldInstance != null){
                                ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
                            }

                        }
                    }
                }

            }
        }
    }

}
