package org.example.domain;

public class FoodItem {
    private Long id;
    private String name;
    private String info;
    
    public FoodItem() {}
    
    public FoodItem(Long id, String name, String info) {
        this.id = id;
        this.name = name;
        this.info = info;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getInfo() {
        return info;
    }
    
    public void setInfo(String info) {
        this.info = info;
    }
    
    @Override
    public String toString() {
        return "FoodItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}
