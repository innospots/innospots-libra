package io.innospots.base.function.shift;

/**
 * @author Smars
 * @date 2023/9/3
 */
public enum ShiftFunctionType {

    SHIFT("shift","迁移",ShiftFunction.class),
    RANK("rank","排名",RankFunction.class),
    DIFF("diff","差值", DiffFunction.class),
    PCT_CHANGE("percent change","变化百分比", PercentChangeFunction.class);

    private String desc;
    private String descCn;

    private Class<? extends IShiftFunction> shiftFunctionClass;


    ShiftFunctionType(String desc, String descCn, Class<? extends IShiftFunction> shiftFunctionClass) {
        this.desc = desc;
        this.descCn = descCn;
        this.shiftFunctionClass = shiftFunctionClass;
    }

    Class<? extends IShiftFunction> shiftFunctionClass() {
        return shiftFunctionClass;
    }

    public String getDesc() {
        return desc;
    }

    public String getDescCn() {
        return descCn;
    }
}
