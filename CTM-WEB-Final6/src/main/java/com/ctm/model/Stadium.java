package com.ctm.model;

public enum Stadium {
    EDEN_GARDENS("Eden Gardens, Kolkata"),
    WANKHEDE("Wankhede Stadium, Mumbai"),
    M_CHINNASWAMY("M. Chinnaswamy Stadium, Bengaluru"),
    ARUN_JAITLEY("Arun Jaitley Stadium, Delhi"),
    MA_CHIDAMBARAM("MA Chidambaram Stadium, Chennai"),
    NARENDRA_MODI("Narendra Modi Stadium, Ahmedabad"),
    DY_PATIL("DY Patil Stadium, Navi Mumbai"),
    PCA_MOHALI("IS Bindra Stadium, Mohali"),
    SAWAI_MANSINGH("Sawai Mansingh Stadium, Jaipur"),
    GREENFIELD("Greenfield Stadium, Thiruvananthapuram"),
    BRSABV_EKANA("Ekana Stadium, Lucknow"),
    VCA_NAGPUR("VCA Stadium, Nagpur"),
    HOLKAR("Holkar Stadium, Indore"),
    BARABATI("Barabati Stadium, Cuttack"),
    HPCA("HPCA Stadium, Dharamshala"),
    SCA_RAJKOT("SCA Stadium, Rajkot"),
    ACA_VIZAG("ACA-VDCA Stadium, Visakhapatnam"),
    JSCA_RANCHI("JSCA Stadium, Ranchi"),
    ACA_GUWAHATI("ACA Stadium, Guwahati"),
    SHAHEED_VEER_NARAYAN("Shaheed Veer Narayan Stadium, Raipur");

    private final String fullName;

    Stadium(String fullName) { this.fullName = fullName; }
    public String getFullName() { return fullName; }
}
