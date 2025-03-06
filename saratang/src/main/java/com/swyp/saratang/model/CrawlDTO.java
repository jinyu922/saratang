package com.swyp.saratang.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrawlDTO {
	private String title;
	private String brand;
	private Integer price;
	private String url;
	
	public CrawlDTO() {}
	
    public CrawlDTO(String title, String brand, Integer price, String url) {
        this.title = title;
        this.brand = brand;
        this.price = price;
        this.url = url;
    }

}
