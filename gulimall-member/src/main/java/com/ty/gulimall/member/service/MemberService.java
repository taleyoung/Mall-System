package com.ty.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ty.common.utils.PageUtils;
import com.ty.gulimall.member.entity.MemberEntity;
import com.ty.gulimall.member.exception.PhoneExistException;
import com.ty.gulimall.member.exception.UserNameExistException;
import com.ty.gulimall.member.vo.MemberLoginVo;
import com.ty.gulimall.member.vo.SocialUser;
import com.ty.gulimall.member.vo.UserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 23:03:48
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo userRegisterVo);

    void checkPhone(String phone) throws PhoneExistException;

    void checkUserName(String username) throws UserNameExistException;

    /**
     * 普通登录
     */
    MemberEntity login(MemberLoginVo vo);

    /**
     * 社交登录
     */
    MemberEntity login(SocialUser socialUser);
}

