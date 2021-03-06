package com.xmlan.machine.module.role.web

import com.github.pagehelper.PageInfo
import com.xmlan.machine.common.base.BaseController
import com.xmlan.machine.common.util.SessionUtils
import com.xmlan.machine.common.util.StringUtils
import com.xmlan.machine.common.util.TokenUtils
import com.xmlan.machine.module.role.entity.Role
import com.xmlan.machine.module.role.service.RoleService
import com.xmlan.machine.module.user.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by ayakurayuki on 2017/12/13-11:09.
 * Package: com.xmlan.machine.module.role.web
 */
@Controller
@RequestMapping('${adminPath}/role')
class RoleController extends BaseController {

    @Autowired
    private RoleService service

    @ModelAttribute
    Role get(@RequestParam(required = false) String id) {
        Role entity = null
        if (StringUtils.isNotBlank(id)) {
            entity = service.get(id)
        }
        if (null == entity) {
            entity = new Role()
            entity.id = NEW_INSERT_ID
        }
        return entity
    }

    @RequestMapping(value = "/detail/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    Role detail(@PathVariable String id) {
        service.get(id)
    }

    @RequestMapping(value = "/list/{pageNo}")
    String list(Role role, @PathVariable int pageNo, Model model) {
        List<Role> list = service.findList role, pageNo
        // 查询
        PageInfo<Role> page = new PageInfo<>(list)
        // 处理分页数据
        model.addAttribute "page", page
        model.addAttribute "userCount", service.getUserCount(list)
        model.addAttribute "name", role.name
        model.addAttribute "deleteToken", TokenUtils.getFormToken(request, "deleteToken")
        "role/roleList"
    }

    @RequestMapping(value = "/form")
    String form(Role role, Model model) {
        model.addAttribute "role", role
        model.addAttribute "token", TokenUtils.getFormToken(request)
        "role/roleForm"
    }

    @RequestMapping(value = "/save/{id}")
    String save(Role role,
                @PathVariable String id, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        if (!TokenUtils.validateFormToken(request, request.getParameter("token"))) {
            addMessage redirectAttributes, "本次提交的表单验证失败"
            return "redirect:$adminPath/role/list/1"
        }
        if (!beanValidator(model, role)) {
            return form(role, model)
        }
        if (StringUtils.equals(id, NEW_INSERT_ID.toString())) {
            service.insert role
            addMessage redirectAttributes, "创建角色成功"
        } else {
            role.id = id.toInteger()
            int result = service.update role
            if (result == DATABASE_DO_NOTHING) {
                addMessage redirectAttributes, "管理员角色不能修改"
            } else {
                addMessage redirectAttributes, "修改角色成功"
            }
        }
        "redirect:$adminPath/role/list/1"
    }

    @RequestMapping(value = "/delete")
    String delete(Role role, String deleteToken, RedirectAttributes redirectAttributes) {
        if (!TokenUtils.validateFormToken(request, "deleteToken", deleteToken)) {
            addMessage redirectAttributes, "表单验证失败"
            return "redirect:$adminPath/role/list/1"
        }
        int responseCode = service.delete(role)
        if (responseCode == DATABASE_DO_NOTHING) {
            addMessage redirectAttributes, "什么都没有删除"
        } else if (responseCode == ROLE_HAVE_SOME_USERS) {
            addMessage redirectAttributes, "这个角色有从属用户，不能删除"
        } else {
            addMessage redirectAttributes, "删除角色成功"
        }
        "redirect:$adminPath/role/list/1"
    }

}
