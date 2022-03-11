package com.hdq.miaosha.validator;

import com.hdq.miaosha.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * IsMobileValidator必须实现ConstraintValidator接口，并且实现其中的方法
 * 因为@consraint注解中的value属性是一个类，这个类需要实现ConstraintValidator接口
 * @author hdq
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {



    // 参数是否是必须的
    private boolean required = false;
    /**
     *
     * @param constraintAnnotation 自定义注解
     */
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        //如果不是必须的，并且值为空，直接验证
        if (required) {
            return ValidatorUtil.isMobile(value);
        }else {
            //如果是必须的，判断值是否为空
            if (StringUtils.isEmpty(value)) {
                return true;
            }else {
                //如果不为空，判断是否是手机号
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
