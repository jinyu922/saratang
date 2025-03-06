package com.swyp.saratang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.CrawlDTO;
import com.swyp.saratang.service.CrawlService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class CrawlingController {
	
	@Autowired
	private CrawlService crawlService;
	
	@Operation(summary = "사이트 정보 크롤링", description = "쇼핑몰의 상세상품정보 url 을 입력하세요. 현재 지원 사이트는 \"www.musinsa.com\" \"www.ssfshop.com\" \"www.elandmall.co.kr\" 입니다 <br>예시url"
			+ "<br>https://www.musinsa.com/products/3048203 "
			+ "<br>https://www.ssfshop.com/Maison-Kitsune/GM0024121385273/good?dspCtgryNo=&brandShopNo=BDMA07A22&brndShopId=BQMKT&leftBrandNM=&utag=ref_evt:special*105653$ref_cnr:$set:$dpos:51"
			+ "<br>https://www.elandmall.co.kr/i/item?itemNo=2404189253&lowerVendNo=LV16003579&ins_sr=eyJwcm9kdWN0SWQiOiIyNDA0MTg5MjUzIn0&pageId=1741259958747&preCornerNo=R01401001_insider_0")
	@GetMapping("crawl")
	public ApiResponseDTO<CrawlDTO> getCrawlData(@RequestParam String requestUrl){
		CrawlDTO crawlDTO=null;
		try {
			crawlDTO=crawlService.getCrawlData(requestUrl);
		} catch (Exception e) {
			return new ApiResponseDTO<CrawlDTO>(400, "잘못된 url: "+e.getMessage(), null);
		}
		return new ApiResponseDTO<CrawlDTO>(200, "성공적으로 url 정보 크롤링", crawlDTO);
	}

}
