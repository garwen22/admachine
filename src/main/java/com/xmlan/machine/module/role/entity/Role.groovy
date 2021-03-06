package com.xmlan.machine.module.role.entity

import javax.validation.constraints.NotNull

/**
 * Created by ayakurayuki on 2017/12/12-11:01.
 * Package: com.xmlan.machine.module.role.entity
 */
class Role implements Serializable {

    private int id
    // 角色ID
    private String name
    // 角色名称
    private String remark
    // 备注

    int getId() {
        return id
    }

    void setId(int id) {
        this.id = id
    }

    @NotNull(message = "角色名称不能为空")
    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getRemark() {
        return remark
    }

    void setRemark(String remark) {
        this.remark = remark
    }

}
