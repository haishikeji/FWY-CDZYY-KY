type QueryField = {
    key: any;
    name: any;
    title: any;
    type: any;
    dict: any;
    url: any;
    query: any;
    boolean?: any;
    show: boolean;
    stable: boolean;
}

declare type QueryFields<T extends QueryField = any> = T[];


type IPage = {
    pageNum: number,
    pageSize: number,
    total?: number
}

type IBaseField = {
    label: string,
    prop: string,
    width?:number,
    minWidth?:number,
    fixed?:string,
}

/**
 *
 *type  字段类型，不同类型字段在查询、列表、编辑页表现形式不同，取值范围
 * render|text|textarea|number|dict|radio|checkbox|rich|image|attach|
 * dept|user|calendar|datetime|date|time|daterange|table|map|rate|
 * progress|color|search|cascade|title|split|select|treeselect|switch|
 * slide|button|tip|bool|divider|cron
 * <br>
 * label  字段名称
 * <br>
 *  prop  字段key
 * <br>
 * rules 校验规则
 * <br>
 * placeholder 提示语
 * <br>
 * readonly 只读
 *  <br>
 * rules 校验规则
 *  <br>
 * conf 高级配置 （长度、可选值等）
 *  <br>
 * tips 浮层提示
 *
 */
type IField = {
    fixed?:string,
    query?:boolean,
    xs?: number,
    sm?: number,
    md?: number,
    lg?: number,
    xl?: number,
    width?:number,
    autoWidth?:boolean,
    label: string,
    prop: string,
    type: string,
    queryType?: string, //查询渲染，优先级比type高
    rules?: [],
    placeholder?: string,
    readonly?: boolean,
    hide?: boolean,
    // config?: IFieldConfig,
    conf?: IFieldConfig,
    tips?: string, //提示语句，在表单中使用
    render?: Function,  //组件渲染  列表中使用
    renderHeader?: Function,  //表头渲染  列表中使用
}

type IFieldConfig = {
    dict?:string, //数据字典key ， 同range
    format?: Function,  //显示的格式化方法
    range?: Array<IFieldRange>,
    min?: number, //最小值 长度
    max?: number, //最大值 长度
    require?: boolean, // 是否必填
    rowspan?: number, //行高
    deft?: any, //默认值
    password?: boolean, //显示密码
    clearable?: boolean, //是否可以清除
    multiple?: boolean, //多选
    direction?: string, //展示方向
    url?:string,   //搜索
    query?:object,   //搜索参数
    op?:string, //操作符，控制查询条件的展示方式
    fmt?:string, //组件中展示的格式     set
    vfmt?:string , //v-model返回值的格式 get

}

/**
 * 字段取值范围定义
 */
type IFieldRange = {
    deft?: boolean,
    label: string,
    color?: string,
    value: number
}
/*

<!--                public static final int FIELD_文本 = 1;-->
<!--                public static final int FIELD_文本域 = 2;-->
<!--                public static final int FIELD_数字 = 3;-->
<!--                public static final int FIELD_单选 = 4;-->
<!--                public static final int FIELD_复选 = 5;-->
<!--                public static final int FIELD_富文本 = 6;-->
<!--                public static final int FIELD_图片 = 7;-->
<!--                public static final int FIELD_附件 = 8;-->
<!--                public static final int FIELD_部门选择 = 9;-->
<!--                public static final int FIELD_成员选择 = 10;-->
<!--                    public static final int FIELD_日期选择 = 11;-->
<!--                    public static final int FIELD_区间日期选择 = 12;-->
<!--                    public static final int FIELD_表格 = 13;-->
<!--                    public static final int FIELD_地图 = 14;-->
<!--                    public static final int FIELD_评分 = 15;-->
<!--                    public static final int FIELD_取色 = 16;-->
<!--                    public static final int FIELD_下拉选择 = 17;-->
<!--                    public static final int FIELD_级联城市 = 18;-->
<!--                    public static final int FIELD_模块标题= 19;-->
<!--                    public static final int FIELD_分割线= 20;-->*/
