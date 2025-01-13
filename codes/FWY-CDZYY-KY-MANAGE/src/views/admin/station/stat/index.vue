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
<!--        <el-input-->
<!--            v-model="state.formQuery.statMonth"-->
<!--            placeholder="统计时间"-->
<!--            clearable-->
<!--            @blur="loadData(true)"-->
<!--            class="wd150 mr10">-->
<!--        </el-input>-->

        <el-date-picker
            placeholder="统计时间（月）"
            @change="loadData(true)"
            value-format="YYYY-MM"
            v-model="state.formQuery.statMonth"
            type="month"
            class="wd150 mr10"
        />

        <ext-select
            v-model="state.formQuery.stationId"
            placeholder="站点"
            clearable
            url="station/listStation"
            urlMethod="get"
            data-key=""
            label-key="stationName"
            value-key="stationId"
            @on-change="loadData(true)"
            class="wd150 mr10">
        </ext-select>

        <el-button class="ml10"  plain size="default" type="success" @click="loadData(true)">
          <SvgIcon name="ele-Search"/>
          查询
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
            <template v-if="field.prop==='action'">
              <el-button v-auth="'stationStatMonth.modify'" size="small" plain type="warning" @click="onRowClick('edit',row)">编辑</el-button>
              <el-button v-auth="'stationStatMonth.list'" size="small" plain type="primary" @click="onRowClick('view',row)">查看</el-button>
              <el-button v-auth="'statement.add'" size="small" plain type="success" @click="handleCreateStatements(row)">生成对账单</el-button>
            </template>
            <template v-else-if="field.prop==='stationId'">
              <div class="text-align-center">
                {{row.stationId}}
                <hr>
                {{row.stationName}}
              </div>
            </template>
            <template v-else-if="['totalMoney','elecMoney','actualElecMoney','serviceMoney','serviceMoneyDiscount','discountAmount','avgOrderMoney','actualPower'].includes(field.prop)">
              {{ u.fmt.fmtMoney(row[field.prop]) }}
            </template>
            <template v-else>
              <div>{{row[field.prop]}}</div>
            </template>

          </template>
        </el-table-column>
      </el-table>

      <ext-page class="page-pager" v-model:value="state.pageQuery" @change="loadData(false)"/>
    </el-card>
  </div>
  <StationStatMonthDialog ref="stationStatMonthDialogRef" @refresh="loadData(true)"/>
</template>

<script setup lang="ts" name="StationStatMonthList">
import {defineAsyncComponent, reactive, onMounted, onBeforeMount, ref, getCurrentInstance, nextTick, onBeforeUnmount} from 'vue';
import {$body,$get} from "/@/utils/request";
import {Msg} from "/@/utils/message";
import u from "/@/utils/u";

import ExtPage from '/@/components/form/ExtPage.vue'

import mittBus from '/@/utils/mitt';
import ExtSelect from "/@/components/form/ExtSelect.vue";

const StationStatMonthDialog = defineAsyncComponent(() => import("/@/views/admin/station/stat/dialog.vue"));

//定义引用
const queryRef = ref();
const stationStatMonthDialogRef = ref();

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
    data: [] as Array < any >,
    loading: false,
    columns: [
      {label: '站点', prop: 'stationId', resizable: true,width:140,fixed:'left'},
      {label: '统计时间', prop: 'statMonth', resizable: true,width:90},
      {label: '订单电量', prop: 'totalPower', resizable: true,width:100},
      {label: '订单金额', prop: 'totalMoney', resizable: true,width:100},
      {label: '订单电费', prop: 'elecMoney', resizable: true,width:100},
      {label: '实际抄表电费', prop: 'actualElecMoney', resizable: true,width:120},
      {label: '实际抄表电量', prop: 'actualPower', resizable: true,width:120},
      {label: '订单服务费', prop: 'serviceMoney', resizable: true,width:110},
      {label: '服务费优惠金额', prop: 'serviceMoneyDiscount', resizable: true,width:135},
      {label: '总优惠金额', prop: 'discountAmount', resizable: true,width:110},
      {label: '充电人数', prop: 'chargeUsers', resizable: true,width:100},
      {label: '充电有效订单数', prop: 'validOrders', resizable: true,width:135},
      {label: '订单平均充电量', prop: 'avgOrderElec', resizable: true,width:145},
      {label: '订单平均充电费用', prop: 'avgOrderMoney', resizable: true,width:145},
      {label: '单枪平均日充电量', prop: 'avgConnectorElec', resizable: true,width:145},
      // {label: '创建时间', prop: 'createTime', sortable: 'custom', resizable: true,width:200,fixed: 'right',},
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

  mittBus.on("stationStatMonth.refresh", () => {
    loadData();
  })
});

onBeforeUnmount(() => {
  mittBus.off("stationStatMonth.refresh")
})


//region 方法区
// 初始化表格数据
const handleCreateStatements = (statMonth) => {
  $get(`statements/create/${statMonth.id}`).then(()=>{
    Msg.message(`生成对账单成功`)
  })
}

const loadData = (refresh: boolean = false) => {
  if (refresh) {
    state.pageQuery.pageNum = 1;
  }
  state.tableData.loading = true;
  $get(`/stat/listStatMonth`, {...state.formQuery, ...state.pageQuery}).then((res: any) => {
    let {list, total} = res;
    state.tableData.data = list;
    state.pageQuery.total = total;
    state.tableData.loading = false;
  }).catch(e => {
    console.error(e)
    state.tableData.loading = false;
  })
};

// 打开修改站点统计表-月弹窗
const onRowClick = (type: string, row: any) => {
  stationStatMonthDialogRef.value.open(type, row);
};

// 删除站点统计表-月
const onRowDel = (row: any) => {
  Msg.confirm(`此操作将永久删除：『${row.name}』，是否继续?`).then(() => {
    $get(`/stationStatMonth/delete/${row.id}`).then(() => {
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