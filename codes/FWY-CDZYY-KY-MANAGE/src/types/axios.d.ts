/* eslint-disable */
import * as axios from '/@/types/axios';

// 扩展 axios 数据返回类型，可自行扩展
declare module '/@/types/axios' {
    export interface AxiosResponse<T = any> {
        code: number;
        data: T;
        message: string;
        msg: string;
        type?: string;

        [key: string]: T;
    }
}


/**
 * 接口响应类型
 */
type    Resp = {
    code: number;
    data: T;
    msg: string;
}
