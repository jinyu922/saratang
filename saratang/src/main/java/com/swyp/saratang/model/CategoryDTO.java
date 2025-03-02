package com.swyp.saratang.model;

import lombok.Data;

@Data
public class CategoryDTO {
    private int userId;
    
    private boolean outerwear;
    private boolean tops;
    private boolean bottoms;
    private boolean underwearHomewear;
    private boolean shoes;
    
    private boolean bags;
    private boolean fashionAccessories;
    private boolean kids;
    private boolean sportsLeisure;
    private boolean digitalLife;
    
    private boolean beauty;
    private boolean food;
    
    public void setAll() {
    	outerwear=true;//1
    	tops=true;//2
    	bottoms=true;//3
    	underwearHomewear=true;//4
    	shoes=true;//5
    	bags=true;//6
    	fashionAccessories=true;//7
    	kids=true;//8
    	sportsLeisure=true;//9
    	digitalLife=true;
    	beauty=true;
    	food=true;
    }
}
