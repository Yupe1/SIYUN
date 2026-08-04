package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("op_front_user_wallet")
public class OpFrontUserWallet implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "front_user_id")
    private Integer frontUserId;
    private Double wallet;
    @Version
    private Integer version;
}
