package com.xmlan.machine.module.user.web

import com.github.pagehelper.PageInfo
import com.google.common.collect.Maps
import com.xmlan.machine.common.base.BaseController
import com.xmlan.machine.common.cache.RoleCache
import com.xmlan.machine.common.cache.UserCache
import com.xmlan.machine.common.util.DateUtils
import com.xmlan.machine.common.util.MediaUtils
import com.xmlan.machine.common.util.SessionUtils
import com.xmlan.machine.common.util.StringUtils
import com.xmlan.machine.common.util.TokenUtils
import com.xmlan.machine.module.advertisement.entity.Advertisement
import com.xmlan.machine.module.advertisementMachine.entity.AdvertisementMachine
import com.xmlan.machine.module.advertisementMachine.service.AdvertisementMachineService
import com.xmlan.machine.module.role.service.RoleService
import com.xmlan.machine.module.user.entity.User
import com.xmlan.machine.module.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import org.springframework.web.servlet.mvc.support.RedirectAttributes

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.Field

/**
 * Created by ayakurayuki on 2017/12/13-14:26.
 * Package: com.xmlan.machine.module.user.web
 */
@Controller
@RequestMapping('${adminPath}/user')
class UserController extends BaseController {

    @Autowired
    private UserService service
    @Autowired
    private RoleService roleService
    @Autowired
    private AdvertisementMachineService advertisementMachineService

    @ModelAttribute
    User get(@RequestParam(required = false) String id) {
        User entity = null
        if (StringUtils.isNotBlank(id)) {
            entity = service.get(id)
        }
        if (null == entity) {
            entity = new User()
            entity.id = NEW_INSERT_ID
        }
        return entity
    }

    /**
     * 查看用户详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/detail/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    Map<String, Object> detail(@PathVariable int id) {
        Map<String, Object> data = Maps.newHashMap()
        data['user'] = UserCache.get(id)
        data['roleName'] = RoleCache.get(UserCache.get(id).roleID).name
        return data
    }

    /**
     * 媒体请求
     * @param id 广告ID
     * @param response 输出流
     */
    @RequestMapping('/media/{id}')
    @ResponseBody
    void media(@PathVariable int id, HttpServletResponse response) {
        User user = UserCache.get(id)
        MediaUtils.mediaTransfer user.url, response
    }

    /**
     * 用户列表查询
     * @param user
     * @param pageNo
     * @param model
     * @return
     */
    @RequestMapping(value = '/list/{pageNo}')
    String list(User user, @PathVariable int pageNo, Model model,ModelMap modelMap) {
        if (StringUtils.isNotBlank(user.addTime)) {
            user.addTime = user.addTime.toString().substring 0, 10
        }
        if (user.roleID < 1) {
            user.roleID = -2
        }

        User user2= modelMap.get("loginUser")
        int userID = user2.id
        if (userID == 1){
            List<User> list = service.findList(user, pageNo)
            PageInfo<User> page = new PageInfo<>(list)
            model.addAttribute "page", page
            model.addAttribute "machineCount", advertisementMachineService.getMachineCountByUserID(list)
            model.addAttribute "roleList", RoleCache.roleList
            model.addAttribute "username", list.username
            model.addAttribute "authname", list.authname
            model.addAttribute "addTime", list.addTime
            model.addAttribute "roleID", list.roleID.toString()
            model.addAttribute "deleteToken", TokenUtils.getFormToken(request, "deleteToken")
            model.addAttribute "passwdToken", TokenUtils.getFormToken(request, "passwdToken")
            model.addAttribute "chgrpToken", TokenUtils.getFormToken(request, "chgrpToken")
        }else {
            List<User> list = service.findListByUserID(userID)
            PageInfo<User> page = new PageInfo<>(list)
            model.addAttribute "page", page
            model.addAttribute "machineCount", advertisementMachineService.getMachineCountByUserID(list)
            model.addAttribute "roleList", RoleCache.roleList
            model.addAttribute "username", list.username
            model.addAttribute "authname", list.authname
            model.addAttribute "addTime", list.addTime
            model.addAttribute "roleID", list.roleID.toString()
            model.addAttribute "deleteToken", TokenUtils.getFormToken(request, "deleteToken")
            model.addAttribute "passwdToken", TokenUtils.getFormToken(request, "passwdToken")
            model.addAttribute "chgrpToken", TokenUtils.getFormToken(request, "chgrpToken")
        }
        "user/userList"
    }


    @RequestMapping(value = '/form')
    String form(User user, Model model) {
        model.addAttribute "user", user
        model.addAttribute "roleList", RoleCache.roleList
        model.addAttribute "token", TokenUtils.getFormToken(request)
        "user/userForm"
    }

    @RequestMapping(value = '/save/{id}')
    String save(User user,
                @PathVariable String id, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!TokenUtils.validateFormToken(request, request.getParameter("token"))) {
            addMessage redirectAttributes, "本次提交的表单验证失败"
            return "redirect:$adminPath/user/list/1"
        }
        if (!beanValidator(model, user)) {
            return form(user, model)
        }
        if (!StringUtils.equals(user.password, request.getParameter("confirmPassword"))) {
            addMessage redirectAttributes, "确认密码与登录密码不匹配"
            return form(user, model)
        }
        if (!(user.phone ==~ /(([+])?(86)(-)?)?1[3|4|5|7|8]\d{9}/)) {
            addMessage redirectAttributes, "手机号码格式不正确"
            return form(user, model)
        }
        user.addTime = "${user.addTime} ${DateUtils.time}"
        if (StringUtils.equals(id, NEW_INSERT_ID.toString())) {
            service.insert user
            addMessage redirectAttributes, "创建用户成功"
        } else {
            user.id = id.toInteger()
            service.update user
            addMessage redirectAttributes, "修改用户成功"
        }
        if (SessionUtils.getAdmin(request).id == user.id) { // 如果修改的用户是本人
            SessionUtils.setAdmin(request, user) // 刷新Session
        }
        "redirect:$adminPath/user/list/1"
    }

    @RequestMapping(value = '/delete')
    String delete(User user, RedirectAttributes redirectAttributes) {
        if (!TokenUtils.validateFormToken(request, "deleteToken", request.getParameter("deleteToken"))) {
            addMessage redirectAttributes, "本次提交的表单验证失败"
            return "redirect:$adminPath/user/list/1"
        }
        int responseCode = service.delete(user)
        if (responseCode == DATABASE_DO_NOTHING) {
            addMessage redirectAttributes, "这个操作没有删除任何用户"
        } else if (responseCode == UserService.USER_HAVE_SOME_MACHINES) {
            addMessage redirectAttributes, "这个用户拥有广告机，不能删除"
        } else {
            addMessage redirectAttributes, "删除用户成功"
        }
        "redirect:$adminPath/user/list/1"
    }

    @RequestMapping(value = '/passwd/{id}')
    @ResponseBody
    String passwd(@PathVariable String id, String oldPasswd, String newPasswd, String rePasswd, String pto) {
        if (!TokenUtils.validateFormToken(request, "passwdToken", pto)) {
            return "本次提交的表单验证失败"
        }
        if (StringUtils.isBlank(newPasswd)) {
            return "没有输入新密码"
        }
        if (StringUtils.isBlank(rePasswd)) {
            return "请确认密码"
        }
        int responseCode = service.passwd(id, oldPasswd, newPasswd, rePasswd)
        if (responseCode == ADMIN_DONE) {
            return "修改成功，建议重新登录"
        }
        if (responseCode == DONE) {
            return "修改完毕"
        }
        if (responseCode == INCORRECT_OLD_PASSWD) {
            if (StringUtils.isBlank(oldPasswd)) {
                return "没有填原密码"
            } else {
                return "原密码不匹配"
            }
        }
        if (responseCode == INCORRECT_REPEAT_PASSWD) {
            return "确认密码和新密码不匹配"
        }
    }

    /**
     * 管理员模式列表
     * @param user
     * @param pageNo
     * @param model
     * @return
     */
    @RequestMapping(value ='/adminMode/{pageNo}')
    String adminMode(User user,@PathVariable int pageNo, Model model){
        List<User> list = service.findAll(pageNo)
        PageInfo<User> page = new PageInfo<>(list)
        model.addAttribute "page", page
        return "system/adminMode"
    }

    String upload(@RequestParam("file") MultipartFile file, HttpServletRequest request){
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver(httpRequest.getSession().getServletContext());
        MultipartHttpServletRequest multipartRequest = commonsMultipartResolver.resolveMultipart(httpRequest);
        String authname = multipartRequest.getParameter("authname");

        "system/adminMode"
    }

    @RequestMapping(value = '/chgrp/{id}')
    @ResponseBody
    String chgrp(@PathVariable String id, int roleID, String cto) {
        if (!TokenUtils.validateFormToken(request, "chgrpToken", cto)) {
            return "本次提交的表单验证失败"
        }
        int responseCode = service.chgrp(id, roleID)
        if (responseCode == ROOT_ADMIN_CAN_NOT_CHANGE_ROLE) {
            return "ROOT管理员不能修改角色"
        }
        if (responseCode == ROLE_IS_NOT_EXISTS) {
            return "指定的角色不存在"
        }
        if (responseCode == DONE) {
            return "修改用户角色完成"
        }
    }

    /**
     * 上传用户头像
     * @param id
     * @param request
     * @param attributes
     * @return
     */
    @RequestMapping(value = '/uploadMedia/{id}')
    String uploadMedia(@PathVariable int id, HttpServletRequest request, RedirectAttributes attributes) {
        if (!TokenUtils.validateFormToken(request, "uploadToken", request.getParameter("uploadToken"))) {
            addMessage attributes, "本次提交的表单验证失败"
            return "redirect:$adminPath/user/adminMode/1"
        }
        def responseCode = service.uploadAdmin(String.valueOf(id), request)
        if (responseCode == DONE) {
            addMessage attributes, "上传成功"
        }
        if (responseCode == FAILURE) {
            addMessage attributes, "上传失败，文件类型错误"
        }
        "redirect:$adminPath/user/adminMode/1"
    }


}
