package com.xmlan.machine.module.advertisementMachine.dao

import com.xmlan.machine.common.base.BaseDAO
import com.xmlan.machine.module.advertisementMachine.entity.AdvertisementMachine
import org.apache.ibatis.annotations.Param
import org.springframework.stereotype.Repository

/**
 * 广告机DAO <br/>
 * Created by ayakurayuki on 2017/12/13-08:52. <br/>
 * Package: com.xmlan.machine.module.advertisementMachine.dao <br/>
 */
@Repository
interface AdvertisementMachineDAO extends BaseDAO<AdvertisementMachine> {

    /**
     * 查看该用户的所有灯杆 (广告机)
     * @return 该用户的所有灯杆列表
     */
    List<AdvertisementMachine> findAllADMachineByUser(@Param("userID") int userID)

    /**
     * 通过机器码获取广告机
     * @param codeNumber 机器识别码
     * @return
     */
    AdvertisementMachine getByCodeNumber(@Param("codeNumber") String codeNumber)

    /**
     * 灯光控制
     * @param id 广告机ID
     * @param operate 操作码 (0/1)
     * @return
     */
    int lightControl(@Param("id") int id, @Param("operate") int operate)

    /**
     * 充电控制
     * @param id 广告机ID
     * @param operate 操作码 (0/1)
     * @return
     */
    int chargeControl(@Param("id") int id, @Param("operate") int operate)

    /**
     * 选中控制
     * @param id 广告机ID
     * @param operate 操作码 (0/1)
     * @return
     */
    int checkedControl(@Param("id") int id, @Param("operate") int operate)

}
