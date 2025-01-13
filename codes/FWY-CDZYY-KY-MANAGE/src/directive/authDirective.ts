import type { App } from 'vue';
import { useUserInfo } from '/@/stores/userInfo';
import { judementSameArr } from '/@/utils/arrayOperation';

/**
 * 用户权限指令
 * @directive 单个权限验证（v-auth="xxx"）
 * @directive 多个权限验证，满足一个则显示（v-auths="[xxx,xxx]"）
 * @directive 多个权限验证，全部满足则显示（v-auth-all="[xxx,xxx]"）
 */
export function authDirective(app: App) {
	// 单个权限验证（v-auth="xxx"）
	app.directive('auth', {
		mounted(el, binding) {
			const stores = useUserInfo();
			if(!binding.value||!stores.userInfos?.permissions){
				return;
			}

			if (!stores.userInfos.permissions.some((v: string) => v === binding.value)) el.parentNode.removeChild(el);
		},
	});
	// 多个权限验证，满足一个则显示（v-auths="[xxx,xxx]"）
	app.directive('auths', {
		mounted(el, binding) {
			if(!binding.value){
				return;
			}
			let flag = false;
			const stores = useUserInfo();
			stores.userInfos.permissions.map((val: string) => {
				binding.value.map((v: string) => {
					if (val === v) {
						flag = true;
						return;
					}
				});
			});
			console.log("v-auths")
			if (!flag) el.parentNode.removeChild(el);
		},
	});
	// 多个权限验证，全部满足则显示（v-auth-all="[xxx,xxx]"）
	app.directive('auth-all', {
		mounted(el, binding) {
			if(!binding.value){
				return;
			}
			const stores = useUserInfo();
			const flag = judementSameArr(binding.value, stores.userInfos.permissions);
			if (!flag) el.parentNode.removeChild(el);
		},
	});
}
