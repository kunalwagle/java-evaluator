package controllers;

import java.util.Date;

public class DataPoint {

    private Integer price;
    private Date date;

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DataPoint(Integer price, Date date) {

        this.price = price;
        this.date = date;
    }
}
