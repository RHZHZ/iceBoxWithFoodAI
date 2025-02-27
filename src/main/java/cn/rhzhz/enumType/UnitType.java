package cn.rhzhz.enumType;

public enum UnitType {
    GRAM("g"),       // 克
    MILLILITER("ml"),// 毫升
    PIECE("piece");  // 片/个

    private final String code;
    UnitType(String code) { this.code = code; }
    public String getCode() { return code; }
}

