package com.yupe.siyun.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class PermissionAssignPayload {
    private List<Integer> permissionIds;
}
