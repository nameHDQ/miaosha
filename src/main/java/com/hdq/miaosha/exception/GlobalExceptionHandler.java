package com.hdq.miaosha.exception;

import com.hdq.miaosha.result.CodeMsg;
import com.hdq.miaosha.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局异常处理
 * @author hdq
 */
@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 全局异常处理 有异常不报错跳到这里来处理
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public Result<String> handleException(HttpServletRequest request, Exception e) {
        if (e instanceof GlobalException) {
            // 如果是自定义异常，直接取出异常信息 装入result 返回前端
            // 而不是在controller 中抛出异常
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getCodeMsg());
        }else if (e instanceof BindException) {
            // 参数校验异常 BindException springboot validation
            BindException bindException = (BindException) e;
            List<ObjectError> allErrors = bindException.getAllErrors();
            // 简单处理 只处理第一个错误 后期可以优化
            ObjectError objectError = allErrors.get(0);
            String defaultMessage = objectError.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(defaultMessage));
        }else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }

}
