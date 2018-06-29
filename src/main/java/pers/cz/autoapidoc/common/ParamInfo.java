package pers.cz.autoapidoc.common;

public class ParamInfo {
    private String clazz;
    private String name;

    private String transformName(String origin) {
        String transform = origin;
        transform = transform.replaceAll("(?i)Code", "代码");
        transform = transform.replaceAll("(?i)Msg", "信息");
        transform = transform.replaceAll("(?i)Data", "数据");
        transform = transform.replaceAll("(?i)Task", "工作项");
        transform = transform.replaceAll("(?i)Size", "大小");
        transform = transform.replaceAll("(?i)Week", "周");
        transform = transform.replaceAll("(?i)Period", "时间段");
        transform = transform.replaceAll("(?i)Project", "项目");
        transform = transform.replaceAll("(?i)Group", "组");
        transform = transform.replaceAll("(?i)Name", "名称");
        transform = transform.replaceAll("(?i)Sex", "性别");
        transform = transform.replaceAll("(?i)Page", "页面");
        transform = transform.replaceAll("(?i)Age", "年龄");
        transform = transform.replaceAll("(?i)Type", "类型");
        transform = transform.replaceAll("(?i)Status", "状态");
        transform = transform.replaceAll("(?i)Order", "序号");
        transform = transform.replaceAll("(?i)Priority", "优先级");
        transform = transform.replaceAll("(?i)block", "阻塞");
        transform = transform.replaceAll("(?i)Time", "时间");
        transform = transform.replaceAll("(?i)Reason", "原因");
        transform = transform.replaceAll("(?i)Deploy", "部署");
        transform = transform.replaceAll("(?i)Product", "产品");
        transform = transform.replaceAll("(?i)Role", "角色");
        transform = transform.replaceAll("(?i)Area", "地区");
        transform = transform.replaceAll("(?i)Start", "开始");
        transform = transform.replaceAll("(?i)End", "结束");
        transform = transform.replaceAll("(?i)Create", "创建");
        transform = transform.replaceAll("(?i)User", "用户");
        transform = transform.replaceAll("(?i)Date", "日期");
        transform = transform.replaceAll("(?i)Update", "更新");
        transform = transform.replaceAll("(?i)Update", "更新");
        transform = transform.replaceAll("(?i)Check", "检查");
        transform = transform.replaceAll("(?i)List", "列表");
        transform = transform.replaceAll("(?i)Remark", "备注");
        transform = transform.replaceAll("(?i)Spare", "备用");
        transform = transform.replaceAll("(?i)Field", "域");
        return transform;
    }

    public ParamInfo(String clazz, String name) {
        clazz = clazz.substring(clazz.lastIndexOf('.') + 1);
        this.clazz = clazz.substring(clazz.lastIndexOf('$') + 1);
        this.name = name;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getName(boolean autoTransform) {
        String transform = name;
        if (autoTransform)
            transform += " " + transformName(name);
        return transform;
    }

    public String getName() {
        return name + " " + transformName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

}
