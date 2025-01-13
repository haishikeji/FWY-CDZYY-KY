<style scoped></style>

<template>
	<el-drawer
		ref="modal"
		v-model="state.show"
		show-close
		append-to-body
		:close-on-click-modal="false"
		:close-on-press-escape="false"
		title="选择"
		style="padding: 10px"
		size="45%"
	>
		<el-row>
			<el-col :span="24">
				<ExtQueryForm ref="queryRef" v-model="state.queryForm" :columns="state.columns" @on-change="loadData(true)" />
			</el-col>
		</el-row>

		<el-row>
			<el-col :span="24">
				<el-scrollbar>
					<ExtTable
						:height="state.height"
						:data-list="state.dataList"
						:columns="state.columns"
						:show-check-box="state.multiple"
						:loading="state.loading"
						:border="true"
						:selectable="true"
						@on-check-change="handleCheckChange"
						@on-sort-change="handleSortChange"
					/>
				</el-scrollbar>
			</el-col>
		</el-row>

		<el-row>
			<el-col :span="24">
				<ExtPage v-model:value="state.pageQuery" @change="loadData(true)" />
			</el-col>
		</el-row>

		<template #footer>
			<span class="dialog-footer">
				<el-button @click="state.show = false" size="default">取消</el-button>
				<el-button type="primary" @click="confirm" size="default">确定</el-button>
			</span>
		</template>
	</el-drawer>
</template>

<script setup lang="ts" name="ExtSContainer">
import { defineAsyncComponent, nextTick, onMounted, reactive, ref } from 'vue';

import { $body, $get } from '/@/utils/request';
import u from '/@/utils/u';
import ExtQueryForm from '/@/components/form/ExtQueryForm.vue';

const ExtPage = defineAsyncComponent(() => import('/@/components/form/ExtPage.vue'));
const ExtTable = defineAsyncComponent(() => import('/@/components/form/ExtTable.vue'));

const queryRef = ref();

const props = defineProps({
	listKey: {
		type: String,
		default: 'list',
	},
	countKey: {
		type: String,
		default: 'total',
	},
	idKey: {
		type: String,
		default: 'id',
	},
});

const state = reactive({
	queryItem: {
		id: undefined,
	},
	init: false,
	columns: [],
	initColumns: [
		{ label: 'ID', prop: 'userId', width: 170, query: false, type: 'number', conf: { op: 'eq' }, resizable: true },
		{ label: '名称', prop: 'userName', width: 120, query: false, type: 'text', resizable: true },
	],
	datas: [],
	checkIds: [],
	chosenIdList: [],
	config: {} as any,
	show: false,
	queryForm: {},
	pageQuery: {
		pageIndex: 1,
		pageSize: 20,
		total: 0,
	},
	callback: () => {},
	dataList: [],
	checkDataList: [],
	multiple: false,
	height: 500,
	loading: false,
});

onMounted(() => {
	// initQuery();

	console.log('ExtSContainer>>>>>>>>>>onMounted');
});

const open = (callback: Function, multiple: boolean = false, config: any, chosenIdList: Array<number>) => {
	state.config = config;
	state.show = true;
	state.multiple = multiple;
	state.callback = callback;
	state.chosenIdList = chosenIdList;
	initQuery();
	loadData(true);
};

const handleSortChange = (sort: any) => {
	console.log('handleSortChange>>>>', sort);
	// 合并查询条件
};

const handleCheckChange = (list: any) => {
	console.log('xxxx choosexxxx', list);
	state.checkDataList = list;
};

const loadData = (refresh: boolean = false) => {
	if (refresh) {
		state.pageQuery.pageIndex = 1;
	}
	let { url, domain, cols, query, method } = state.config;
	let requestQuery = { ...state.pageQuery, ...query, ...state.queryForm };

	console.log(requestQuery);
	switch (method) {
		case 'get':
			$get(url, requestQuery).then((res: any) => {
				state.loading = false;
				state.pageQuery.total = res[props.countKey];
				state.dataList = props.listKey ? res[props.listKey] : res;
			});
			break;
		case 'body':
		default:
			$body(url, requestQuery).then((res: any) => {
				state.loading = false;
				let { list, count } = res;
				state.pageQuery.total = res[props.countKey];
				state.dataList = props.listKey ? res[props.listKey] : res;
			});
			break;
	}
};

const confirm = () => {
	if (state.callback && typeof state.callback === 'function') {
		state.callback.apply(null, [state.checkDataList]);
	}
	/*  let list = [];
    for (var i = 0; i < this.dataList.length; i++) {
      if (this.containsInIds(this.dataList[i].id)) {
        list.push(this.dataList[i]);
      }
    }
    if (this.options.callback && list.length > 0) {
      this.options.callback(list);
    }*/
	state.show = false;
};

/**
 * 设置基础构造查询字段、展示字段等
 */
const initQuery = () => {
	if (state.init) {
		return;
	}
	let { columns, query } = state.config;
	if (!u.isEmptyOrNull(columns)) {
		state.columns = state.initColumns.concat(columns);
		console.log(state.columns);
	}
	console.log(state.config);
	if (query) {
		state.queryForm = { ...state.queryForm, ...query };
		// Object.keys(query).forEach(k=>state.queryForm[k] = query[k]);
	}
	console.log(state.queryForm);
	state.init = true;

	nextTick(() => {
		let bodyHeight = document.body.clientHeight;
		let queryHeight = queryRef.value?.$el.clientHeight;
		state.height = bodyHeight - queryHeight - 150;
		console.log(state.height);
	});
};

// 暴露变量
defineExpose({
	open,
});
</script>
