package com.michele.appdegree;


public class MyMarker {

    // classe che gestisce le informazioni riguardanti i singoli marker da posizionare sulla mappa
    // di google maps

    private String mLabel;
    private String mIcon;
    private Double mLatitude;
    private Double mLongitude;
    private Float mDegree1;
    private Integer mDistance1;
    private Double mLatitude2;
    private Double mLongitude2;

    public MyMarker(String label, String icon, Double latitude, Double longitude, Float degree1,
                    Integer distance1, Double latitude2, Double longitude2)
    {
        this.mLabel = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
        this.mDegree1 = degree1;
        this.mDistance1 = distance1;
        this.mLatitude2 = latitude2;
        this.mLongitude2 = longitude2;
    }

    public String getmLabel()
    {
        return mLabel;
    }

    public void setmLabel(String mLabel)
    {
        this.mLabel = mLabel;
    }

    public String getmIcon()
    {
        return mIcon;
    }

    public void setmIcon(String icon)
    {
        this.mIcon = icon;
    }

    public Double getmLatitude()
    {
        return mLatitude;
    }

    public void setmLatitude(Double mLatitude)
    {
        this.mLatitude = mLatitude;
    }

    public Double getmLongitude()
    {
        return mLongitude;
    }

    public void setmLongitude(Double mLongitude)
    {
        this.mLongitude = mLongitude;
    }

    public Float getmDegree1()
    {
        return mDegree1;
    }

    public void setmDegree1(Float mDegree1)
    {
        this.mDegree1 = mDegree1;
    }

    public Integer getmDistance1()
    {
        return mDistance1;
    }

    public void setmDistance1(Integer mDistance1)
    {
        this.mDistance1 = mDistance1;
    }

    public Double getmLatitude2()
    {
        return mLatitude2;
    }

    public void setmLatitude2(Double mLatitude2)
    {
        this.mLatitude2 = mLatitude2;
    }

    public Double getmLongitude2()
    {
        return mLongitude2;
    }

    public void setmLongitude2(Double mLongitude2)
    {
        this.mLongitude2 = mLongitude2;
    }
}