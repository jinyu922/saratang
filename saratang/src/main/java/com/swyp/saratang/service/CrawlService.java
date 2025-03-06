package com.swyp.saratang.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import com.swyp.saratang.model.CrawlDTO;

@Service
public class CrawlService {
	
	public CrawlDTO getCrawlData(String requestUrl) throws RuntimeException{
		CrawlDTO crawlDTO=new CrawlDTO();
		//크롤링 가능한 url 검증
		if(!requestUrl.contains("www.musinsa.com")&&!requestUrl.contains("www.ssfshop.com")&&!requestUrl.contains("www.elandmall.co.kr")) {
			throw new IllegalArgumentException("미지원 사이트, 현재 지원 사이트는 무신사 , SSF, 이랜드몰 ");
		}
        String imageUrl=null;
        String brand=null;
        String productName=null;
        Integer price=null;
        
        //크롤링
        try {
            String url = requestUrl;
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .get();

            if(url.contains("www.musinsa.com")){
            	//무신사
            	
            	//제품명 (무신사는 제품명만 따로 추출이 안돼서 문자열 파싱해줘야함)
                String scriptData = doc.select("script[id=category-data]").html();
                
                Pattern pattern = Pattern.compile("\"goodsNm\":\"([^\"]+)\""); //정규 표현식을 사용하여 goodsNm 추출
                Matcher matcher = pattern.matcher(scriptData);

                if (matcher.find()) {
                    productName = matcher.group(1);
                }
                
                //브랜드
                Element brandElement = doc.selectFirst("meta[property=product:brand]"); 
                brand = brandElement != null ? brandElement.attr("content") : "브랜드 정보를 찾을 수 없습니다.";
            	
                
                //가격																									
                Element priceElement = doc.selectFirst("meta[property=product:price:amount]");
                if (priceElement != null) {
                    String priceString = priceElement.attr("content");
                    priceString = priceString.replaceAll("[^\\d]", "");
                    price = Integer.parseInt(priceString);
                }
                
            	//이미지url
                Element imageElement = doc.selectFirst("meta[property=og:image]");  // Open Graph 이미지 URL
                imageUrl = imageElement != null ? imageElement.attr("content") : "이미지 URL을 찾을 수 없습니다.";
                
            }
            if(url.contains("www.ssfshop.com")){
            	//SSF몰
            	
            	//제품명
                Element productNameElement = doc.selectFirst("meta[property=og:title]");
                if (productNameElement != null) {
                    productName = productNameElement.attr("content");
                }
                
                //브랜드
                Element brandElement = doc.selectFirst(".godsInfo-area .brand-name a");
                if (brandElement != null) {
                    brand = brandElement.text();
                }

                //가격 (,붙은거 파싱해야함)
                Element priceElement = doc.selectFirst(".gods-price .sale .price");
                if (priceElement != null) {																																																													
                    String priceString = priceElement.text();
                    priceString = priceString.replaceAll("[^\\d]", "");
                    price = Integer.parseInt(priceString);
                }
                
            	//이미지url
                Element imageElement = doc.selectFirst(".godsImg-area .preview-img .img-wrap .img-item img");
                if (imageElement != null) {
                    imageUrl = imageElement.attr("src");
                }

            }
            if(url.contains("www.elandmall.co.kr")){
                // 제품명
                Element productNameElement = doc.selectFirst(".title_wrap .tit");
                if (productNameElement != null) {
                    productName = productNameElement.text();
                }
                
                // 브랜드
                Element brandElement = doc.selectFirst(".wish_box .btn_wish");
                if (brandElement != null) {
                    brand = brandElement.attr("data-brand-name"); 
                }

                // 가격
                Element PriceElement = doc.selectFirst(".price_item .price_inner .price strong"); 
                if (PriceElement != null) {
                    String priceString = PriceElement.text();  
                    priceString = priceString.replaceAll("[^\\d]", "");
                    price = Integer.parseInt(priceString);
                }

                // 이미지 url
                Element imageUrlElement = doc.selectFirst("meta[property=og:image]"); 
                if (imageUrlElement != null) {
                    imageUrl = imageUrlElement.attr("content");
                }
            }
          
            crawlDTO.setTitle(productName);
            crawlDTO.setBrand(brand);
            crawlDTO.setPrice(price);
            crawlDTO.setUrl(imageUrl);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
		return crawlDTO;
	}

}
