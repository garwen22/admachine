package com.xmlan.machine.common.cache

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.xmlan.machine.common.util.CacheUtils
import com.xmlan.machine.common.util.SpringContextUtils
import com.xmlan.machine.module.advertisement.dao.AdvertisementDAO
import com.xmlan.machine.module.advertisement.entity.Advertisement

/**
 * Created by ayakurayuki on 2017/12/26-14:28.
 * <p>
 * Package: com.xmlan.machine.common.cache
 */
final class AdvertisementCache {

    private static final def CACHE_NAME = "advertisementCache"
    private static final def MAP_NAME = "adCacheMap"
    private static final def AD_LIST_NAME = "advertisementList"

    private static def advertisementDAO = SpringContextUtils.getBean(AdvertisementDAO.class)

    /**
     * 初始化
     * @return 初始化的map
     */
    static Map<String, List> initialCacheMap() {
        Map map = CacheUtils.get(CACHE_NAME, MAP_NAME) as Map<String, List>
        if (map == null) {
            map = Maps.newHashMap()
            List<Advertisement> advertisementList = advertisementDAO.findAll()
            map.put AD_LIST_NAME, advertisementList
        }
        CacheUtils.put(CACHE_NAME, MAP_NAME, map)
        return map
    }

    /**
     * 获取缓存中的list
     * @param key
     * @return
     */
    static List getList(String key) {
        Map<String, List> map = initialCacheMap()
        List list = map.get(key)
        if (list == null) {
            list = Lists.newArrayList()
        }
        return list
    }

    /**
     * 获取完整的广告列表
     * @return 完整的广告列表
     */
    static List<Advertisement> getAdvertisementList() {
        return getList(AD_LIST_NAME)
    }

    /**
     * 根据ID获取对象
     * @param id
     * @return
     */
    static Advertisement get(int id) {
        List<Advertisement> list = advertisementList
        for (advertisement in list) {
            if (advertisement.id == id) {
                return advertisement
            }
        }
        return null
    }

    /**
     * 根据广告机ID获取所属广告数量
     * @param id
     * @return
     */
    static int getAdvertisementCountByMachineID(int id) {
        List<Advertisement> list = advertisementList
        int count = 0
        list.each {
            if (it.machineID == id) {
                count++
            }
        }
        return count
    }

    /**
     * 获取新添加的条目
     * @param ad
     * @return
     */
    static Advertisement getNewInserted(Advertisement ad) {
        def list = advertisementList
        for (item in list) {
            if (item.addTime == ad.addTime
                    && item.name == ad.name
                    && item.time == ad.time
                    && item.machineID == ad.machineID
                    && item.userID == ad.userID) {
                return item
            }
        }
        return null
    }

}
