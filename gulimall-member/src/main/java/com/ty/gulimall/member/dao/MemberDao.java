package com.ty.gulimall.member.dao;

import com.ty.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 23:03:48
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
