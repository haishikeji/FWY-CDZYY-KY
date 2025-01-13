<template>
  <div class="system-container layout-padding">
    <el-card shadow="hover" class="layout-padding-auto">
      <el-form
          :model="state.formQuery"
          ref="queryRef"
          size="default" label-width="0px" class="mt5 mb5">
        <!--        <el-input
                    v-model="state.formQuery.name"
                    placeholder="字典名称"
                    clearable
                    @blur="loadData(true)"
                    class="wd150 mr10">
                </el-input>-->

        <el-input
            v-model="state.formQuery.code"
            placeholder="字典编码"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>

        <el-button class="ml10" plain size="default" type="success" @click="loadData(true)">
          <SvgIcon name="ele-Search"/>
          查询
        </el-button>

        <el-button class="ml10" plain size="default" type="primary" @click="handleAddDict" v-auth="'dict.add'">
          <SvgIcon name="ele-FolderAdd"/>
          创建
        </el-button>
      </el-form>

      <div class="flex">
        <el-table
            style="width: 400px;"
            width="400"
            border
            stripe="stripe"
            :height="state.tableData.height"
            highlight-current-row
            current-row-key="id"
            row-key="id"
            :data="state.tableData.data"
            @row-click="handleDictRowClick"
            v-loading="state.tableData.loading">
          <template #empty>
            <el-empty></el-empty>
          </template>
          <el-table-column
              v-for="field in state.columns"
              :key="field.prop"
              :label="field.label"
              :column-key="field.prop"
              :width="field.width"
              :min-width="field.minWidth"
              :fixed="field.fixed"
              :sortable="field.sortable"
              :show-overflow-tooltip="!field.fixed&&field.width>150"
          >
            <template #default="{row}">
              <template v-if="field.prop==='expand'">
                <p style="padding-left: 2em;" v-html="row[field.prop]"></p>
              </template>
              <template v-else>
                <div>{{ row[field.prop] }}</div>
              </template>

            </template>
          </el-table-column>
        </el-table>

        <div class="dict-content__box flex-auto ml10 pr5 pl5" v-if="state.visible">
          <el-input
              :readonly="state.dictForm.id"
              v-model="state.dictForm.code"
              placeholder="字典编码"
              clearable
              size="default"
              class="w100 mr10">
          </el-input>

          <el-input
              v-model="state.dictForm.remark"
              placeholder="备注"
              clearable
              type="textarea"
              size="default"
              :rows="2"
              class="w100  mt10">
          </el-input>

          <el-row :gutter="10" class="mt20">
            <el-col :span="8">字典项名称</el-col>
            <el-col :span="8">字典项码值</el-col>
            <el-col :span="4">排序权重</el-col>
          </el-row>
          <div v-for="(item,idx) in state.dictForm.list" :key="idx" class="mt10">
            <el-row :gutter="10">
              <el-col :span="8">
                <el-input size="default" v-model="item.name" placeholder="字典名称"></el-input>
              </el-col>
              <el-col :span="8">
                <el-input size="default" :readonly="item.id" v-model="item.value" placeholder="字典码值"></el-input>
              </el-col>
              <el-col :span="8">
                <el-input-number :min="1"  size="default" controls-position="right" v-model="item.weight" placeholder="排序权重"></el-input-number>
                <SvgIcon name="ele-RemoveFilled" color="var(--el-color-danger)" class="ml3 cursor-pointer" @click="handleDeleteDictItem(idx)"></SvgIcon>
              </el-col>
            </el-row>
          </div>

          <el-button  v-auth="'dict.add'" size="small" class="mt10 mr5" type="primary" @click="handleAddDictItem">新增</el-button>
          <el-button v-auth="'dict.modify'" size="small" class="mt10" type="success" v-if="state.saveVisible" @click="handleSaveDict">保存</el-button>

        </div>
      </div>


      <el-affix position="bottom" :offset="49">
        <ext-page class="page-pager" v-model:value="state.pageQuery" @change="loadData(false)"/>
      </el-affix>

    </el-card>
  </div>
</template>

<script setup lang="ts" name="Dict">
import {defineAsyncComponent, reactive, onMounted, ref, getCurrentInstance, nextTick} from 'vue';
import ExtPage from "/@/components/form/ExtPage.vue"
import {$body} from "/@/utils/request";
import u from "/@/utils/u";
import {Msg} from "/@/utils/message";
// 引入组件

const {proxy}: any = getCurrentInstance();
import {ElButton} from 'element-plus'

// 定义变量内容
const queryRef = ref();
const dicDialogRef = ref();

const state = reactive({
  tableData: {
    data: [] as Array<any>,
    total: 0,
    loading: false,
    height: 500
  },
  pageQuery: {
    pageNum: 1,
    pageSize: 20,
    total: 0
  },
  formQuery: {},
  columns: [
    {label: '编码', prop: 'code', query: true, type: 'text', resizable: true, width: 250, fixed: 'left'},
    {label: '备注', prop: 'remark', query: true, type: 'text', resizable: true, width: 160},
    {
      label: '操作时间', prop: 'updateTime', width: 180, query: true, type: 'datetime', resizable: true,
    }
  ],
  dictForm: {
    visible:false,
    id: 0,
    code: '',
    remark: '',
    list: [] as Array<any>
  },
  dictLoading: false,
  visible:false,
  saveVisible:false
});


// 初始化表格数据
const loadData = (refresh: boolean = false) => {
  if (refresh) {
    state.pageQuery.pageNum = 1;
  }
  state.tableData.loading = true;
  $body('dataDict/listV2', {query:state.formQuery, ...state.pageQuery}).then((res: any) => {
    let {list, total} = res;
    state.tableData.data = list;
    state.pageQuery.total = total;
    state.tableData.loading = false;
  })
};
/*// 打开新增字典弹窗
const onOpenAddDic = (type: string) => {
  console.log(type)
  dicDialogRef.value.openDialog(type);
};
// 打开修改字典弹窗
const onOpenEditDic = (type: string, row: RowDicType) => {
  console.log(row)
  dicDialogRef.value.openDialog(type, row);
};*/
// 删除字典
const onRowDel = (row: RowDicType) => {
  console.log(row)
  Msg.confirm(`此操作将永久删除字典名称：“${row.type}”，是否继续?`).then(() => {
    loadData();
    Msg.message("删除成功")
  })
}

// 页面加载时
onMounted(() => {
  loadData();

  nextTick(() => {
    let bodyHeight = document.body.clientHeight;
    let queryHeight = queryRef.value.$el.clientHeight;
    state.tableData.height = bodyHeight - queryHeight - 320
  })
});

const handleDictRowClick = (row: any) => {
  state.dictLoading = true;
  Msg.showLoading()
  $body(`dataDict/list`, {query: {code: row.code}}).then(res => {
    state.visible = true;
    Msg.hideLoading()
    state.dictLoading = false;
    let {list} = res;
    if (u.isEmptyOrNull(list)) {
      state.dictForm = {
        code: '',
        remark: '',
        list: []
      }
      return;
    }

    u.sort(list, 'weight');
    state.dictForm = {
      id: list[0].id,
      code: list[0].code,
      remark: list[0].remark,
      list: list
    }

  })
}

const handleDeleteDictItem = (idx) => {
  state.saveVisible = true;
  state.dictForm.list.splice(idx,1)
}

const handleAddDict = () => {
  state.saveVisible = true;
  state.visible = true;
  state.dictForm = {
    code: '字典码值',
    remark: '备注',
    list: [
      {name: '', value: '', weight: 0}
    ]
  }
}

const handleAddDictItem = () => {
  state.saveVisible = true;
  state.dictForm.list.push({
    code: '',
    name: '',
    value: '',
    remark: '',
    weight: 0
  })
}

const handleSaveDict = () => {
  let params = state.dictForm.list.map(k => {
    let {id, name, value, weight} = k;
    return {
      id, name, value, weight,
      code: state.dictForm.code,
      remark: state.dictForm.remark
    }
  })
  $body(`dataDict/saveOrUpdate`, params).then(() => {
    Msg.message("保存成功")
    state.saveVisible = false;
  })
}
</script>

<style scoped lang="scss">
.system-container {
  :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    flex: 1;
    overflow: auto;

    .el-table {
      flex: 1;
    }
  }
}

.page-content {
  margin-bottom: 20px;
}

.page-pager {
  background-color: #fff;
  height: 24px;
}
</style>
