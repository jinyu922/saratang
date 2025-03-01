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
    	outerwear=true;
    	tops=true;
    	bottoms=true;
    	underwearHomewear=true;
    	shoes=true;
    	bags=true;
    	fashionAccessories=true;
    	kids=true;
    	sportsLeisure=true;
    	digitalLife=true;
    	beauty=true;
    	food=true;
    }
}
