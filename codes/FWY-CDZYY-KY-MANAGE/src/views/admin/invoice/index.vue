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
            v-model="state.formQuery.phone"
            placeholder="用户手机号"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>
        <ext-d-select
            v-model="state.formQuery.status"
            placeholder="发票状态"
            type="Invoice.status"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </ext-d-select>
        <ext-date-picker
            v-model="state.formQuery.startTime"
            placeholder="开票时间（起）"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </ext-date-picker>
        <ext-date-picker
            v-model="state.formQuery.endTime"
            placeholder="开票时间（止）"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </ext-date-picker>
        <ext-d-select
            v-model="state.formQuery.invoiceType"
            placeholder="发票类型"
            type="Invoice.type"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </ext-d-select>
        <el-input
            v-model="state.formQuery.email"
            placeholder="接收发票邮箱"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>
        <el-input
            v-model="state.formQuery.invoiceTitle"
            placeholder="发票抬头名称"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>
        <el-input
            v-model="state.formQuery.taxId"
            placeholder="公司税号"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>
        <el-input
            v-model="state.formQuery.biller"
            placeholder="开票人"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>


        <el-button class="ml10" plain size="default" type="success" @click="loadData(true)">
          <SvgIcon name="ele-Search"/>
          查询
        </el-button>

        <el-button class="ml10" plain size="default" type="warning" @click="handleDownloadExcel">
          <SvgIcon name="ele-Download"/>
          下载
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
            :type="field.type"
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
              <el-row class="pd10">
                <el-col :span="12">
                  <h4 class="pd5">关联订单明细</h4>
                  <el-table :data="row.orderDetails" width="400" border>
                    <el-table-column label="充电订单" prop="startChargeSeq"/>
                    <el-table-column label="订单总金额" prop="totalMoney">
                      <template #default="{row}">{{ u.fmt.fmtMoney(row.totalMoney) }}</template>
                    </el-table-column>
                    <el-table-column label="总电量/度" prop="totalPower"/>
                    <el-table-column label="电费" prop="elecMoney">
                      <template #default="{row}">{{ u.fmt.fmtMoney(row.elecMoney) }}</template>
                    </el-table-column>
                    <el-table-column label="服务费" prop="serviceMoney">
                      <template #default="{row}">{{ u.fmt.fmtMoney(row.serviceMoney) }}</template>
                    </el-table-column>
                    <el-table-column label="服务费优惠" prop="serviceMoneyDiscount">
                      <template #default="{row}">{{ u.fmt.fmtMoney(row.serviceMoneyDiscount) }}</template>
                    </el-table-column>
                  </el-table>
                </el-col>
              </el-row>

            </template>
            <template v-else-if="field.prop==='status'">
              <ext-d-label type="Invoice.status" :model-value="row[field.prop]"></ext-d-label>
              <!--              <p style="padding-left: 2em;" v-html="row[field.prop]"></p>-->
            </template>
            <template v-else-if="field.prop==='invoiceType'">
              <ext-d-label type="Invoice.type" :model-value="row[field.prop]"></ext-d-label>
              <!--              <p style="padding-left: 2em;" v-html="row[field.prop]"></p>-->
            </template>
            <template v-else-if="field.prop==='totalMoney'">
              {{ u.fmt.fmtMoney(row[field.prop]) }}
            </template>
            <template v-else-if="field.prop==='invoiceAmount'">
              {{ u.fmt.fmtMoney(row[field.prop]) }}
            </template>
            <template v-else-if="field.prop==='elecMoney'">
              {{ u.fmt.fmtMoney(row[field.prop]) }}
            </template>
            <template v-else-if="field.prop==='serviceMoney'">
              {{ u.fmt.fmtMoney(row[field.prop]) }}
            </template>
            <template v-else-if="field.prop==='serviceMoneyDiscount'">
              {{ u.fmt.fmtMoney(row[field.prop]) }}
            </template>
            <template v-else-if="field.prop==='action'">
              <el-button v-if="row.status===0" v-auth="'invoice.modify'" size="small" plain type="warning" @click="handleInvice(row)">开票</el-button>
              <el-button v-if="row.status===0" v-auth="'invoice.modify'" size="small" plain type="danger" @click="handleCancelInvoice(row)">取消开票</el-button>
              <el-button v-if="row.status===1" v-auth="'invoice.modify'" size="small" plain type="success" @click="previewInvoice(row)">下载</el-button>
              <el-button   size="small" plain type="success" @click="handleInfo(row)">详情</el-button>
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
  <InvoiceDialog ref="invoiceDialogRef" @refresh="loadData(true)"/>

  <canvas ref="pdfViewer"></canvas>
</template>

<script setup lang="ts" name="InvoiceList">
import {defineAsyncComponent, reactive, onMounted, onBeforeMount, ref, getCurrentInstance, nextTick, onBeforeUnmount} from 'vue';
import {$body, $get, $post} from "/@/utils/request";
import {Msg} from "/@/utils/message";
import u from "/@/utils/u"


import ExtPage from '/@/components/form/ExtPage.vue'

import mittBus from '/@/utils/mitt';
import ExtDSelect from "/@/components/form/ExtDSelect.vue";
import ExtDLabel from "/@/components/form/ExtDLabel.vue";
import ExtDatePicker from "/@/components/form/ExtDatePicker.vue";

const InvoiceDialog = defineAsyncComponent(() => import("/@/views/admin/invoice/dialog.vue"));

// import pdfjsLib from 'pdfjs-dist'

//定义引用
const queryRef = ref();
const invoiceDialogRef = ref();

const pdfViewer = ref()


//定义变量
const state = reactive({
  formQuery: {
    status:0
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
      {type: 'expand', prop: 'expand', width: 30, fixed: 'left'},
      {label: '用户手机号', prop: 'mobilePhone', align: 'center', fixed: 'left', width: 120},
      {label: '发票ID', prop: 'id', align: 'center', fixed: 'left', width: 110},
      {label: '发票申请单号', prop: 'applyId', align: 'center', fixed: 'left', width: 130},
      {label: '发票状态', prop: 'status', align: 'center', fixed: 'left', width: 100},
      {label: '发票金额', prop: 'invoiceAmount', resizable: true, width: 90},
      {label: '订单总额', prop: 'totalMoney', resizable: true, width: 90},
      {label: '总服务费', prop: 'serviceMoney', resizable: true, width: 90},
      {label: '总服务费', prop: 'elecMoney', resizable: true, width: 90},
      {label: '服务费优惠', prop: 'serviceMoneyDiscount', resizable: true, width: 105},
      {label: '公司税号', prop: 'taxId', resizable: true, width: 115},
      {label: '发票类型', prop: 'invoiceType', resizable: true, width: 90},
      {label: '发票抬头', prop: 'invoiceTitle', resizable: true, width: 110},
      {label: '开票人', prop: 'biller', resizable: true, width: 80},
      {label: '开票时间', prop: 'fapiaoTime', resizable: true, width: 170},
      {label: '接收发票邮箱', prop: 'email', resizable: true, width: 120},
      {label: '电话', prop: 'phone', resizable: true, width: 90},
      /*

            {label: '公司地址', prop: 'address', resizable: true, width: 130},
            {label: '开户银行', prop: 'bankName', resizable: true, width: 130},
            {label: '银行账户', prop: 'bankAccount', resizable: true},

            {label: '税额详情信息', prop: 'taxInfo', resizable: true, width: 150},

      */

      {label: '备注', prop: 'remark', resizable: true},
      {label: '创建时间', prop: 'createTime', sortable: 'custom', resizable: true, width: 170},
      {label: '更新时间', prop: 'updateTime', sortable: 'custom', resizable: true, width: 170},
      {
        label: '操作', prop: 'action', width: 240, align: 'center', fixed: 'right',
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

  mittBus.on("invoice.refresh", () => {
    loadData();
  })
});

onBeforeUnmount(() => {
  mittBus.off("invoice.refresh")
})


//region 方法区
// 初始化表格数据
const loadData = (refresh: boolean = false) => {
  if (refresh) {
    state.pageQuery.pageNum = 1;
  }
  state.tableData.loading = true;
  $body(`/finance/listInvoice`, {...state.formQuery, ...state.pageQuery}).then((res: any) => {
    let {list, total} = res;
    state.tableData.data = list;
    state.pageQuery.total = total;
    state.tableData.loading = false;
  }).catch(e => {
    console.error(e)
    state.tableData.loading = false;
  })
};

const handleInvice = (row: any) => {
  Msg.confirm("请确认是否开票？").then(()=>{
    $get(`/finance/handleInvoice/${row.id}`).then((res: any) => {
      Msg.message("开票成功")
      loadData(true)
    }).catch(e => {
      console.error(e)
    })
  })

}

const handleDownloadExcel = () => {
  let url = import.meta.env.VITE_API_URL;
  if (!url) {
    url = `${location.origin}/admin/`;
  }
  // $get(`/finance/export/invoiceList`,{...state.formQuery, ...state.pageQuery})
  let params =   Object.keys(state.formQuery).map(key=>`${key}=${state.formQuery[key]}`).join("&")

  window.open(`${url}/finance/export/invoiceList?t=${new Date().getTime()}&${params}`, "_blank")
}

const handleInfo = (row:any) => {
  invoiceDialogRef.value.open('view', row);
}


const previewInvoice =   (row: any) => {
  $get(`/finance/downloadInvoice/${row.id}`).then((res: any) => {
    console.log(res)
    let {downloadUrl} = res;
    if (downloadUrl) {
      window.open(downloadUrl, "_blank")

      // doPriview(downloadUrl)

    }
  }).catch(e => {
    console.error(e)
  })
}
/*
const doPriview=async  (url:string) =>{
  const canvas = pdfViewer.value;
  const loadingTask = pdfjsLib.getDocument(url)
  const pdf = await loadingTask.promise
  const page = await pdf.getPage(1)
  const viewport = page.getViewport({ scale: 1.0 })
  const context = canvas.getContext('2d')
  canvas.height = viewport.height
  canvas.width = viewport.width
  const renderTask = page.render({
    canvasContext: context,
    viewport: viewport
  })
  await renderTask.promise
}*/

// 打开修改用户弹窗
const onRowClick = (type: string, row: any) => {
  invoiceDialogRef.value.open(type, row);
};

const handleTableSelectionChange = (selection: any) => {
  console.log("handleTableSelectionChange>>", selection)
  // emit("on-check-change", selection)
}

const handleTableSortChange = (column, prop, order) => {
  console.log("handleTableSortChange>>", column, prop, order)
  // emit("on-sort-change", column)
}

const handleCancelInvoice = (row:any) => {
  Msg.confirm("请确认是否取消开票？").then(()=>{
    $get(`/finance/cancelApplyInvoice/${row.id}`).then((res: any) => {
      Msg.message("取消成功")
      loadData(true)
    }).catch(e => {
      console.error(e)
    })
  })

}


//endregion


// 暴露变量
// defineExpose({
//     loadData,
// });
</script>
