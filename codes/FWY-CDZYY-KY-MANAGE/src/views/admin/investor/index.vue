<style scoped lang="scss">
.system-container {

  :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
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
<template>
  <div class="system-container layout-padding">
    <el-card shadow="hover" class="layout-padding-auto">


      <el-form
          :model="state.formQuery"
          ref="queryRef"
          size="default" label-width="0px" class="mt5 mb5">
        <el-input
            v-model="state.formQuery.username"
            placeholder="客户姓名"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>
        <el-input
            v-model="state.formQuery.stationId"
            placeholder="站点id"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>
        <ext-d-select
            v-model="state.formQuery.status"
            placeholder="状态"
            type="Investor.status"
            clearable
            @on-change="loadData(true)"
            class="wd150 mr10"/>

        <!--        <el-input
                    v-model="state.formQuery.status"
                    placeholder="状态：0-无效，1-有效"
                    clearable
                    @blur="loadData(true)"
                    class="wd150 mr10">
                </el-input>-->
        <el-input
            v-model="state.formQuery.mobilePhone"
            placeholder="电话号码"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>


        <el-button class="ml10" plain size="default" type="success" @click="loadData(true)">
          <SvgIcon name="ele-Search"/>
          查询
        </el-button>

        <el-button  v-auth="'investor.add'"   size="default" plain  type="success" class="ml10" @click="onRowClick('add',null)">
          <SvgIcon name="ele-FolderAdd"/>
          新增
        </el-button>
      </el-form>

      <el-table
          border
          stripe="stripe"
          :height="state.tableData.height"
          highlight-current-row
          current-row-key="id"
          row-key="id"
          :data="state.tableData.data"
          v-loading="state.tableData.loading"
          @selection-change="handleTableSelectionChange"
          @sort-change="handleTableSortChange">
        <template #empty>
          <el-empty></el-empty>
        </template>
        <el-table-column
            v-for="field in state.tableData.columns"
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
            <template v-if="field.prop==='stationId'">
              <div class="text-align-center">
                {{ row.stationId }}
                <hr>
                {{ row.stationName }}
              </div>

            </template>
            <template v-else-if="field.prop==='status'">
              <ext-d-label type="Investor.status" v-model="row.status"></ext-d-label>
            </template>
            <template v-else-if="field.prop==='action'">
              <el-button  v-auth="'investor.modify'"  size="small" plain  type="warning" @click="onRowClick('edit',row)">编辑</el-button>
              <el-button  v-auth="'investor.list'"  size="small" plain  type="primary" @click="onRowClick('view',row)">查看</el-button>
            </template>
            <template v-else>
              <div>{{ row[field.prop] }}</div>
            </template>

          </template>
        </el-table-column>
      </el-table>

      <ext-page class="page-pager" v-model:value="state.pageQuery" @change="loadData(false)"/>
    </el-card>
  </div>
  <InvestorInfoDialog ref="investorInfoDialogRef" @refresh="loadData(true)"/>
</template>

<script setup lang="ts" name="InvestorInfoList">
import {defineAsyncComponent, reactive, onMounted, onBeforeMount, ref, getCurrentInstance, nextTick, onBeforeUnmount} from 'vue';
import {$body, $get} from "/@/utils/request";
import {Msg} from "/@/utils/message";


import ExtPage from '/@/components/form/ExtPage.vue'

import mittBus from '/@/utils/mitt';
import ExtDSelect from "/@/components/form/ExtDSelect.vue";
import ExtDLabel from "/@/components/form/ExtDLabel.vue";

const InvestorInfoDialog = defineAsyncComponent(() => import("/@/views/admin/investor/dialog.vue"));

//定义引用
const queryRef = ref();
const investorInfoDialogRef = ref();

//定义变量
const state = reactive({
  formQuery: {},
  pageQuery: {
    pageNum: 1,
    pageSize: 10,
    total: 0
  },
  tableData: {
    height: 500,
    data: [] as Array<any>,
    loading: false,
    columns: [
      {label: '客户姓名', prop: 'adminUserName', resizable: true},
      {label: '电话号码', prop: 'telephone', resizable: true},
      {label: '站点', prop: 'stationId', resizable: true},
      {label: '分成比例', prop: 'splittingProportion', resizable: true, width: 90},
      {label: '电损承担比例', prop: 'elecLossProportion', resizable: true, width: 120},
      {label: '增值税率', prop: 'vatRate', resizable: true, width: 90},
      {label: '账户名', prop: 'accountName', resizable: true},
      {label: '银行卡号', prop: 'bankCardNo', resizable: true, width: 180},
      {label: '开户行名称', prop: 'bankName', resizable: true, width: 180},
      {label: '税号', prop: 'taxNo', resizable: true, width: 180},
      // {label: '备注', prop: 'remark', resizable: true},
      // {label: '状态', prop: 'status', sortable: 'custom', align: 'center'},
      // {label: '创建时间', prop: 'createTime', sortable: 'custom', resizable: true, width: 180},
      // {label: '更新时间', prop: 'updateTime', sortable: 'custom', resizable: true, width: 180},
      {
        label: '操作', prop: 'action', width: 180, align: 'center', fixed: 'right',
      }
    ],
  },
})


// 监听双向绑定 modelValue 的变化
// watch(
//         () => state.pageIndex,
//         () => {
//
//         }
// );

//生命周期钩子
onBeforeMount(() => {
})

onMounted(() => {
  loadData();

  nextTick(() => {
    let bodyHeight = document.body.clientHeight;
    let queryHeight = queryRef.value.$el.clientHeight;
    state.tableData.height = bodyHeight - queryHeight - 320
  })

  mittBus.on("investorInfo.refresh", () => {
    loadData();
  })
});

onBeforeUnmount(() => {
  mittBus.off("investorInfo.refresh")
})


//region 方法区
// 初始化表格数据
const loadData = (refresh: boolean = false) => {
  if (refresh) {
    state.pageQuery.pageNum = 1;
  }
  state.tableData.loading = true;
  $get(`/investorInfo/list`, {...state.formQuery, ...state.pageQuery}).then((res: any) => {
    let {list, total} = res;
    state.tableData.data = list;
    state.pageQuery.total = total;
    state.tableData.loading = false;
  }).catch(e => {
    console.error(e)
    state.tableData.loading = false;
  })
};

// 打开修改投资者弹窗
const onRowClick = (type: string, row: any) => {
  investorInfoDialogRef.value.open(type, row);
};

// 删除投资者
const onRowDel = (row: any) => {
  Msg.confirm(`此操作将永久删除：『${row.name}』，是否继续?`).then(() => {
    $get(`/investorInfo/delete/${row.id}`).then(() => {
      Msg.message("删除成功", 'success')
    }).catch(() => {
      Msg.message("删除失败", 'error')
    })
  });
};

const handleTableSelectionChange = (selection: any) => {
  console.log("handleTableSelectionChange>>", selection)
  // emit("on-check-change", selection)
}

const handleTableSortChange = (column, prop, order) => {
  console.log("handleTableSortChange>>", column, prop, order)
  // emit("on-sort-change", column)
}


//endregion


// 暴露变量
// defineExpose({
//     loadData,
// });
</script>