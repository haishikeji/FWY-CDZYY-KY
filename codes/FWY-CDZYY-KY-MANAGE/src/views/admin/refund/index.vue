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
        <ext-d-select
            v-model="state.formQuery.status"
            placeholder="退款状态"
            clearable
            type="Refund.status"
            @on-change="loadData(true)"
            class="wd150 mr10">
        </ext-d-select>
<!--       <ext-d-select
            v-model="state.formQuery.fundsAccount"
            placeholder="资金账户"
            clearable
            type="Refund.account"
            @on-change="loadData(true)"
            class="wd150 mr10">
        </ext-d-select>-->
        <el-input
            v-model="state.formQuery.mobilePhone"
            placeholder="手机号"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>
<!--        <el-input
            v-model="state.formQuery.outRefundNo"
            placeholder="商户退款单号"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>-->
        <el-input
            v-model="state.formQuery.outTradeNo"
            placeholder="商户订单号"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>
<!--        <el-input
            v-model="state.formQuery.refundId"
            placeholder="微信支付退款单号"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>-->
        <el-input
            v-model="state.formQuery.transactionId"
            placeholder="微信支付订单号"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>

        <el-button class="ml10" plain size="default" type="success" @click="loadData(true)">
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
            <template v-if="field.prop==='expand'">
              <p style="padding-left: 2em;" v-html="row[field.prop]"></p>
            </template>
            <template v-else-if="'total'===field.prop">
              {{u.fmt.fmtMoney(row[field.prop])}}
            </template>
            <template v-else-if="'refund'===field.prop">
              {{u.fmt.fmtMoney(row[field.prop])}}
            </template>
            <template v-else-if="'discountAmount'===field.prop">
              {{u.fmt.fmtMoney(row[field.prop])}}
            </template>
            <template v-else-if="'status'===field.prop">
              <ext-d-label type="Refund.status" v-model="row[field.prop]"/>
            </template>
            <template v-else-if="'action'===field.prop">
              <el-button v-if="row.status==='NEW'"  size="small" plain  type="warning" @click="onRefundClick(row)">退款</el-button>
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
</template>

<script setup lang="ts" name="RefundLogList">
import {defineAsyncComponent, reactive, onMounted, onBeforeMount, ref, getCurrentInstance, nextTick, onBeforeUnmount} from 'vue';
import {$body, $get} from "/@/utils/request";
import {Msg} from "/@/utils/message";
import u from "/@/utils/u"


import {useRoute} from "vue-router";

const route = useRoute();
import ExtPage from '/@/components/form/ExtPage.vue'

import mittBus from '/@/utils/mitt';
import ExtDSelect from "/@/components/form/ExtDSelect.vue";
import ExtDLabel from "/@/components/form/ExtDLabel.vue";


//定义引用
const queryRef = ref();

//定义变量
const state = reactive({
  formQuery: {
    status:'NEW'
  },
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
      {label: '退款记录ID', prop: 'refundLogId', width: 105, resizable: true,fixed:'left'},
      {label: '商户退款单号', prop: 'outRefundNo', width: 130, resizable: true,fixed:'left'},
      {label: '商户订单号', prop: 'outTradeNo', width: 130, resizable: true,fixed:'left'},
      {label: '手机号', prop: 'mobilePhone', width: 120, resizable: true,fixed:'left'},
      // {label: '退款渠道', prop: 'channel', width: 150,resizable: true},
      {label: '退款状态', prop: 'status', resizable: true, width: 120},
      {label: '申请时间', prop: 'createTime', sortable: 'custom', resizable: true, width: 160},
      {label: '退款成功时间', prop: 'successTime', sortable: 'custom', resizable: true, width: 160},
      {label: '原充值订单金额', prop: 'total', resizable: true, width: 140},
      {label: '退款申请金额', prop: 'refund', resizable: true, width: 120},
      {label: '优惠金额', prop: 'discountAmount', resizable: true, width: 90},
      // {label: '用户支付币种', prop: 'currency', resizable: true, width: 120},
      // {label: '资金账户', prop: 'fundsAccount', resizable: true, width: 120},
      {label: '退款原因', prop: 'reason', resizable: true, width: 120},
      // {label: '微信支付退款单号', prop: 'outRefundNo', resizable: true, width: 150},
      // {label: '微信支付订单号', prop: 'outTradeNo', resizable: true, width: 150},
      {label: '退款入账账户', prop: 'userReceivedAccount', resizable: true, width: 120},
      {label: '操作人', prop: 'adminUsername', resizable: true, width: 120},
      {
        label: '操作', prop: 'action', width: 110, align: 'center', fixed: 'right',
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
  let query = route.query;
  console.log(route.params, route.query)
  if (query.mobilePhone) {
    state.formQuery.mobilePhone = query.mobilePhone;
  }


  loadData();

  nextTick(() => {
    let bodyHeight = document.body.clientHeight;
    let queryHeight = queryRef.value.$el.clientHeight;
    state.tableData.height = bodyHeight - queryHeight - 320
  })

  mittBus.on("refundLog.refresh", () => {
    loadData();
  })
});

onBeforeUnmount(() => {
  mittBus.off("refundLog.refresh")
})


//region 方法区
// 初始化表格数据
const loadData = (refresh: boolean = false) => {
  if (refresh) {
    state.pageQuery.pageNum = 1;
  }
  state.tableData.loading = true;
  $get(`/finance/listRefundLog`, {...state.formQuery, ...state.pageQuery}).then((res: any) => {
    let {list, total} = res;
    state.tableData.data = list;
    state.pageQuery.total = total;
    state.tableData.loading = false;
  }).catch(e => {
    console.error(e)
    state.tableData.loading = false;
  })
};

// 打开修改用户弹窗
const onRefundClick = ( row: any) => {
  // if(row.status){
  //   return;
  // }
  Msg.confirm("请确认是否执行退款操作？").then(()=>{
    $get(`/finance/customWxRefund/${row.refundLogId}`).then((res: any) => {
      Msg.message(`操作已提交`)
      loadData(true)
    }).catch(e => {
      console.error(e)
    })
  })
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