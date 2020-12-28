package com.ty.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.ty.common.valid.AddGroup;
import com.ty.common.valid.ListValue;
import com.ty.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 21:37:28
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须指定品牌ID", groups = {UpdateGroup.class})
	@Null(message = "新增不能指定ID", groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(groups = {AddGroup.class})
	@URL(message = "logo必须是一个合法url", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@ListValue(vals = {0,1}, groups = {AddGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "^[a-z][A-Z]$", message = "检索首字母必须是一个字母")
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull
	@Min(value=0, message = "必须大于0")
	private Integer sort;

}
