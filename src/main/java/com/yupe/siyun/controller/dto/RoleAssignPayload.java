package com.yupe.siyun.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleAssignPayload {
    private List<Integer> roleIds;
}
