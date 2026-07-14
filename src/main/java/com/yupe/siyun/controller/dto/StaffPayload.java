package com.yupe.siyun.controller.dto;

import com.yupe.siyun.entity.ObjBackUser;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class StaffPayload {
    private Integer id;
    private String name;
    private Integer gender;
    private String avataUrl;
    private String email;
    private String password;
    private String tel;
    private String chinaId;
    private LocalDate birth;
    private Integer deptId;
    private Integer status;
    private Integer level;
    private BigDecimal salary;
    private String remark;
    private String registerIp;
    private List<Integer> roleIds;

    public ObjBackUser toUser() {
        ObjBackUser user = new ObjBackUser();
        user.setId(id);
        user.setName(name);
        user.setGender(gender);
        user.setAvataUrl(avataUrl);
        user.setEmail(email);
        user.setPassword(password);
        user.setTel(tel);
        user.setChinaId(chinaId);
        user.setBirth(birth);
        user.setDeptId(deptId);
        user.setStatus(status);
        user.setLevel(level);
        user.setSalary(salary);
        user.setRemark(remark);
        user.setRegisterIp(registerIp);
        return user;
    }
}
