package com.hdq.miaosha.exception;

import com.hdq.miaosha.result.CodeMsg;

/**
 * 所有的全局异常
 * @author hdq
 */
public class GlobalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private CodeMsg codeMsg;

    public GlobalException(CodeMsg codeMsg) {
        super(codeMsg.toString());
        this.codeMsg = codeMsg;
    }



    public CodeMsg getCodeMsg() {
        return codeMsg;
    }

}
