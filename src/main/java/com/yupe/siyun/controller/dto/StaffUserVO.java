package com.yupe.siyun.controller.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class StaffUserVO {
    private Integer id;
    private String name;
    private Integer gender;
    private String avataUrl;
    private String email;
    private String tel;
    private String chinaId;
    private LocalDate birth;
    private Integer deptId;
    private String deptName;
    private LocalDate regieterDate;
    private String registerIp;
    private Integer status;
    private Integer level;
    private BigDecimal salary;
    private String remark;
    private List<Integer> roleIds;
    private List<String> roleNames;
}
