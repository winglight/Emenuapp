package com.yi4all.emenu.newgen.db;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "course")
public class CourseModel implements Serializable{

	public  static final String NAME = "NAME";
	public  static final String NAME_EN = "NAME_EN";
	public  static final String CODE = "CODE";
	public  static final String IMG_URL = "IMG_URL";
	public  static final String CATEGORY = "CATEGORY";
	public  static final String PRICE = "PRICE";
	public  static final String CREATED_AT = "CREATED_AT";
	
	@DatabaseField(generatedId = true)
	private long id = -1;
	@DatabaseField(index = true, columnName = NAME)
	private String name;
	@DatabaseField(index = true, columnName = NAME_EN)
	private String nameEn;
	@DatabaseField(index = true, columnName = CODE)
	private String code;
	@DatabaseField( columnName = IMG_URL)
	private String imgUrl; 
	@DatabaseField(columnName = CATEGORY)
	private String category; // 0 - 100
	@DatabaseField(columnName = PRICE)
	private int price; 
	@DatabaseField(columnName = CREATED_AT)
	private Date createdAt; 
	
	public CourseModel(){
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getNameEn() {
		return nameEn;
	}
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}
	
	
	
}
