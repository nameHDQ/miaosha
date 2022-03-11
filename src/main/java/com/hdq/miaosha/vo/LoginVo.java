package com.hdq.miaosha.vo;

import com.hdq.miaosha.validator.IsMobile;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.StringJoiner;

/**
 * @author hdq
 */
public class LoginVo {
    @NotNull
    /**
     * 自定义验证器
     */
    @IsMobile()
    private String mobile;
    @NotNull
    @Length(min = 32)
    private String password;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LoginVo.class.getSimpleName() + "[", "]")
                .add("mobile='" + mobile + "'")
                .add("password='" + password + "'")
                .toString();
    }
}
