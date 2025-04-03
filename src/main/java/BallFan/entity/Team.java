package BallFan.entity;

public enum Team {
    삼성("삼성 라이온즈"),
    LG("LG 트윈스"),
    SSG("SSG 랜더스"),
    KIA("KIA 타이거즈"),
    KT("KT wiz"),
    한화("한화 이글스"),
    NC("NC 다이노스"),
    두산("두산 베어스"),
    롯데("롯데 자이언츠"),
    키움("키움 히어로즈");

    private final String fullName;

    Team(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return fullName;
    }


}
