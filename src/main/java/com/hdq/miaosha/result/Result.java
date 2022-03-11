package com.hdq.miaosha.result;

public class Result<T> {

    private int code;
    private String msg;
    private T data;

    /**
     * 私有构造函数 类外不可以初始化这个类
     * @param data
     */
    private Result(T data){
        this.code = 0;
        this.data = data;
        this.msg = "succes";
    }
    private Result(CodeMsg codeMsg){
        if (codeMsg == null){
            return;
        }
        this.code = codeMsg.getCode();
        this.msg = codeMsg.getMsg();

    }

    public static <T> Result<T> success(T data){
        return new Result<>(data);
    }
    public static <T> Result<T> error(CodeMsg cm){
        return new Result<>(cm);
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
