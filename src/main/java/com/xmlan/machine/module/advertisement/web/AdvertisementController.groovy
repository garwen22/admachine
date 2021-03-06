package com.xmlan.machine.module.advertisement.web

import cn.jiguang.common.ClientConfig
import cn.jiguang.common.resp.APIConnectionException
import cn.jiguang.common.resp.APIRequestException
import cn.jpush.api.JPushClient
import cn.jpush.api.push.PushResult
import cn.jpush.api.push.model.PushPayload
import com.github.pagehelper.PageInfo
import com.google.common.collect.Maps
import com.xmlan.machine.common.base.BaseController
import com.xmlan.machine.common.base.ModuleEnum
import com.xmlan.machine.common.base.ObjectEnum
import com.xmlan.machine.common.base.OperateEnum
import com.xmlan.machine.common.cache.AdvertisementCache
import com.xmlan.machine.common.cache.AdvertisementMachineCache
import com.xmlan.machine.common.cache.UserCache
import com.xmlan.machine.common.config.Global
import com.xmlan.machine.common.util.*
import com.xmlan.machine.module.advertisement.entity.Advertisement
import com.xmlan.machine.module.advertisement.service.AdvertisementService
import com.xmlan.machine.module.advertisementMachine.entity.AdvertisementMachine
import com.xmlan.machine.module.advertisementMachine.service.AdvertisementMachineService
import com.xmlan.machine.module.system.service.SysLogService
import com.xmlan.machine.module.user.entity.User
import com.xmlan.machine.module.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by ayakurayuki on 2017/12/13-17:21.
 * Package: com.xmlan.machine.module.advertisement.web
 */
@Controller
@RequestMapping('${adminPath}/advertisement')
class AdvertisementController extends BaseController {

    @Autowired
    private AdvertisementService service
    @Autowired
    private UserService userService
    @Autowired
    private AdvertisementMachineService machineService
    private final SysLogService sysLogService;


    /**
     * 页面属性
     * @param id 广告ID
     * @return 获取到的实体类
     */
    @ModelAttribute
    Advertisement get(@RequestParam(required = false) String id) {
        Advertisement entity = null
        if (StringUtils.isNotBlank(id)) {
            entity = service.get(id)
        }
        if (null == entity) {
            entity = new Advertisement()
            entity.id = NEW_INSERT_ID
            entity.userID = SessionUtils.getAdmin(request).id
        }
        return entity
    }

    /**
     * 广告详细信息
     * @param id 广告ID
     * @return JSON封装的广告对象
     */
    @RequestMapping(value = "/detail/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    Map<String, Object> detail(@PathVariable String id) {
        Map<String, Object> data = Maps.newHashMap()
        Advertisement advertisement = AdvertisementCache.get(id.toInteger())
        data["ad"] = advertisement
        data["machine"] = AdvertisementMachineCache.getMachineNameByID(advertisement.machineID)
        return data
    }

    /**
     * 列表和列表查询
     * @param advertisement 查询条件实体
     * @param pageNo 当前页面
     * @param model 传参对象
     * @return
     */
    @RequestMapping(value = "/list/{pageNo}")
    String list(Advertisement advertisement, @PathVariable int pageNo, Model model, ModelMap modelMap) {
        if (advertisement.name == 'null') advertisement.name = StringUtils.EMPTY
        if (advertisement.addTime == 'null') advertisement.addTime = StringUtils.EMPTY
        if (advertisement.machineID < 1) advertisement.machineID = NEW_INSERT_ID
        if (StringUtils.isNotBlank(advertisement.addTime)) {
            advertisement.addTime = "${advertisement.addTime.substring(0, 10)} 00:00:00".toString()
        }
//        if (SessionUtils.getAdmin(request).roleID != ADMIN_ROLE_ID) {
//            // 角色不是管理员则限定仅查询属于自己的广告
//            advertisement.userID = SessionUtils.getAdmin(request).id
//        } else {
//            advertisement.userID = 0
//        }
        // 分页
        //获取有权限的人以及处理反馈字符
        int userID = advertisement.userID
        User user= modelMap.get("loginUser")
        if (userID==1 || userID == 10){
            List<Advertisement> advertisementList =service.findAll(pageNo)
            PageInfo<Advertisement> page = new PageInfo<>(advertisementList)
            model.addAttribute "page", page
            if (SessionUtils.getAdmin(request).roleID != ADMIN_ROLE_ID) {
                // 添加广告判断，普通用户没有广告机的时候不能添加广告
                model.addAttribute "machines", AdvertisementMachineCache.getMachineCount(SessionUtils.getAdmin(request).id)
            } else {
                // 管理员则通过总的广告机数量判断可否添加广告
                model.addAttribute "machines", AdvertisementMachineCache.machineCount
            }
        }else if (user.roleID==1){
            List<AdvertisementMachine> machineList = service.findListByUserID(userID,pageNo)
            PageInfo<Advertisement> page = new PageInfo<>(machineList)
            model.addAttribute "page", page
            if (SessionUtils.getAdmin(request).roleID != ADMIN_ROLE_ID) {
                // 添加广告判断，普通用户没有广告机的时候不能添加广告
                model.addAttribute "machines", AdvertisementMachineCache.getMachineCount(SessionUtils.getAdmin(request).id)
            } else {
                // 管理员则通过总的广告机数量判断可否添加广告
                model.addAttribute "machines", AdvertisementMachineCache.machineCount
            }
        }else{
            List<User> userList = userService.findListByUserID(userID)
            for (int i=0 ; i < userList.size() ; i ++){
                if (userList.get(i).roleID == 1){
                    userID = userList.get(i).id
                    List<AdvertisementMachine> machineList = service.findListByUserID(userID,pageNo)
                    PageInfo<Advertisement> page = new PageInfo<>(machineList)
                    model.addAttribute "page", page
                    if (SessionUtils.getAdmin(request).roleID != ADMIN_ROLE_ID) {
                        // 添加广告判断，普通用户没有广告机的时候不能添加广告
                        model.addAttribute "machines", AdvertisementMachineCache.getMachineCount(SessionUtils.getAdmin(request).id)
                    } else {
                        // 管理员则通过总的广告机数量判断可否添加广告
                        model.addAttribute "machines", AdvertisementMachineCache.machineCount
                    }
                    break
                }
            }
        }
        model.addAttribute "machineList", AdvertisementMachineCache.dropdownAdvertisementMachineList
        model.addAttribute "name", advertisement.name
        model.addAttribute "machineID", advertisement.machineID
        model.addAttribute "time", advertisement.time
        model.addAttribute "addTime", advertisement.addTime
        model.addAttribute "deleteToken", TokenUtils.getFormToken(request, "deleteToken")
        model.addAttribute "uploadToken", TokenUtils.getFormToken(request, "uploadToken")
        "advertisement/advertisementList"
    }

    /**
     * 表单编辑视图跳转
     * @param advertisement 广告对象
     * @param model 传参对象
     * @return
     */
    @RequestMapping(value = "/form")
    String form(Advertisement advertisement, Model model) {
        model.addAttribute "advertisement", advertisement
        model.addAttribute "machineList", AdvertisementMachineCache.dropdownAdvertisementMachineList
        model.addAttribute "userList", UserCache.dropdownUserList
        model.addAttribute "token", TokenUtils.getFormToken(request)
        "advertisement/advertisementForm"
    }

    /**
     * 表单保存
     * @param advertisement 广告对象
     * @param id 广告ID
     * @param model 传参对象
     * @param redirectAttributes 跳转属性
     * @return
     */
    @RequestMapping(value = "/save/{id}")
    String save(Advertisement advertisement,
                @PathVariable int id, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!TokenUtils.validateFormToken(request, request.getParameter("token"))) {
            addMessage redirectAttributes, "本次提交的表单验证失败"
            return "redirect:$adminPath/advertisement/list/1"
        }
        if (!beanValidator(model, advertisement)) {
            return form(advertisement, model)
        }
        if (SessionUtils.getAdmin(request).roleID != ADMIN_ROLE_ID) {
            advertisement.userID = SessionUtils.getAdmin(request).id
        }
        if (id == NEW_INSERT_ID) {
            advertisement.addTime = DateUtils.dateTime
            service.insert advertisement
            addMessage redirectAttributes, "创建广告成功"
        } else {
            advertisement.id = id
            service.update advertisement
            addMessage redirectAttributes, "修改广告成功"
        }
        "redirect:$adminPath/advertisement/list/1"
    }

    /**
     * 删除广告
     * @param advertisement 广告对象
     * @param attributes 跳转属性
     * @return
     */
    @RequestMapping(value = "/delete")
    String delete(Advertisement advertisement, HttpServletRequest request, RedirectAttributes attributes) {
        if (!TokenUtils.validateFormToken(request, "deleteToken", request.getParameter("deleteToken"))) {
            addMessage attributes, "本次提交的表单验证失败"
            return "redirect:$adminPath/advertisement/list/1"
        }
//        String sys =request.getParameter("deleteToken")

        Advertisement advertisementCach = AdvertisementCache.get(advertisement.id);
        pushUpdate(advertisementCach.id,DONE,"删除广告")
        if (service.delete(advertisementCach) == DATABASE_DO_NOTHING) {
            addMessage attributes, "这个操作没有删除任何广告"
        } else {
            addMessage attributes, "删除广告成功"
        }
        "redirect:$adminPath/advertisement/list/1"
    }

    /**
     * 上传媒体
     * @param id 广告ID
     * @param request 上传请求
     * @param attributes 跳转属性
     * @return
     */
    @RequestMapping(value = '/uploadMedia/{id}')
    String uploadMedia(@PathVariable int id, HttpServletRequest request, RedirectAttributes attributes) {
        if (!TokenUtils.validateFormToken(request, "uploadToken", request.getParameter("uploadToken"))) {
            addMessage attributes, "本次提交的表单验证失败"
            return "redirect:$adminPath/advertisement/list/1"
        }
        def responseCode = service.uploadMedia(String.valueOf(id), request)
        if (responseCode == DONE) {
            pushUpdate(id,DEFAULT,"上传文件")
            addMessage attributes, "上传成功"
        }
        if (responseCode == FAILURE) {
            addMessage attributes, "上传失败，文件类型错误"
        }
        "redirect:$adminPath/advertisement/list/1"
    }

    /**
     * 媒体请求
     * @param id 广告ID
     * @param response 输出流
     */
    @RequestMapping('/media/{id}')
    @ResponseBody
    void media(@PathVariable int id, HttpServletResponse response) {
        Advertisement advertisement = AdvertisementCache.get(id)
        MediaUtils.mediaTransfer advertisement.url, response
    }

    /**
     * 推送方法
     */
    private def push = { int machineID, int advertisementID, String message ->
        logger.trace "New push task with machine-No.${machineID}, ad-No.${advertisementID}, message-${message}"
        def machine = AdvertisementMachineCache.get(machineID)
        HashMap<String, Integer> map = Maps.newHashMap()
        map['advertisementID'] = advertisementID
        map['type'] = TYPE_MEDIA_UPDATE
        map['needUpdate'] = YES
        def pushClient = new JPushClient(Global.masterSecret, Global.appKey, null, ClientConfig.instance)
        def payload = PushUtils.buildPayload(String.valueOf(machine.id), message, map)
        try {
            def result = pushClient.sendPush(payload)
            logger.trace result.originalContent
        } catch (APIRequestException e) {
            logger.error "API exception with: ${e.message}"
        }catch (APIConnectionException e) {
            map.put(keyResponseCode, ERROR_API_CONNECTION_EXCEPTION);
            map.put(keyMessage, "Push connect error.");
            logger.error("API exception with: " + e.getMessage());
        }
    }
    private HashMap<String, Object> pushUpdate(int id, int responseCode, String message) {
        // 获取广告对象
        Advertisement advertisement = AdvertisementCache.get(id);
        // 获取广告机对象
        AdvertisementMachine machine = AdvertisementMachineCache.get(advertisement.getMachineID());
        // 封装推送体数据
        HashMap<String, Integer> pushData = Maps.newHashMap();
        // 添加推送体数据内容（广告ID）
        pushData.put("advertisementID", advertisement.getId());
        pushData.put("type", TYPE_MEDIA_UPDATE);
        pushData.put("needUpdate", YES);
        // 创建推送客户端
        JPushClient client = new JPushClient(Global.getMasterSecret(), Global.getAppKey(), null, ClientConfig.getInstance());
        // 创建预处理推送体
        PushPayload payload = PushUtils.buildPayload(String.valueOf(machine.getId()), message, pushData);
        PushResult result;
        HashMap<String, Object> map = Maps.newHashMap();
        try {
            result = client.sendPush(payload);
            if (responseCode == DEFAULT) {
                map.put(keyResponseCode, result.statusCode);
            } else {
                map.put(keyResponseCode, responseCode);
            }
            map.put(keyMessage, message);
            logger.trace(result.getOriginalContent());
        }catch (APIConnectionException e) {
            map.put(keyResponseCode, ERROR_API_CONNECTION_EXCEPTION);
            map.put(keyMessage, "Push connect error.");
            logger.error("API exception with: " + e.getMessage());
        } catch (APIRequestException e) {
            map.put(keyResponseCode, ERROR_API_REQUEST_EXCEPTION);
            map.put(keyMessage, "Push request error.");
            logger.error("API exception with: " + e.getMessage());
        }
    }


}