package org.leoliu.framework;

import org.leoliu.framework.helper.BeanHelper;
import org.leoliu.framework.helper.ClassHelper;
import org.leoliu.framework.helper.ControllerHelper;
import org.leoliu.framework.helper.IocHelper;
import org.leoliu.framework.util.ClassUtil;

public final class HelperLoader {

    public static void init(){
        Class<?>[] classList = {
                ClassHelper.class,
                BeanHelper.class,
                IocHelper.class,
                ControllerHelper.class
        };

        for (Class<?> cls : classList){
            ClassUtil.loadClass(cls.getName());
        }
    }


}
