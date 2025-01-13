import { defineStore } from 'pinia';
import { Session } from '/@/utils/storage';

/**
 * TagsView 路由列表
 * @methods setTagsViewRoutes 设置 TagsView 路由列表
 * @methods setCurrenFullscreen 设置开启/关闭全屏时的 boolean 状态
 */
export const useTagsViewRoutes = defineStore('tagsViewRoutes', {
	state: (): TagsViewRoutesState => ({
		tagsViewRoutes: [],
		isTagsViewCurrenFull: false,
	}),
	actions: {
		async setTagsViewRoutes(data: Array<string>) {
			// console.log("setTagsViewRoutes>>>",data)
			this.tagsViewRoutes = data;
		},
		async addTagsViewRoute(route: any) {
			// console.log("addTagsViewRoute>>>",route)
			this.tagsViewRoutes.push(route)
		},
		setCurrenFullscreen(bool: Boolean) {
			// console.log("setCurrenFullscreen>>>",bool)
			Session.set('isTagsViewCurrenFull', bool);
			this.isTagsViewCurrenFull = bool;
		},
	},
});
