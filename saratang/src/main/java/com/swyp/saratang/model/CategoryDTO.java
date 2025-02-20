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
}
