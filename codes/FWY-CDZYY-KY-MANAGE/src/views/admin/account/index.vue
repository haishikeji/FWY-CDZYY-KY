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
<!--            v-model="state.formQuery.username"-->
<!--            placeholder="用户名"-->
<!--            clearable-->
<!--            @blur="loadData(true)"-->
<!--            class="wd150 mr10">-->
<!--        </el-input>-->
        <el-input
            v-model="state.formQuery.mobilePhone"
            placeholder="手机号"
            clearable
            @blur="loadData(true)"
            class="wd150 mr10">
        </el-input>
<!--        <ext-d-select-->
<!--            v-model="state.formQuery.status"-->
<!--            placeholder="状态"-->
<!--            type="User.status"-->
<!--            clearable-->
<!--            @on-change="loadData(true)"-->
<!--            class="wd150 mr10"/>-->

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
            <template v-if="['rechargeAmount','totalMoney','refundAmount','balance','frozenAmount','payAmount','discountAmount','refundDiscountAmount'].includes(field.prop)">
              {{ u.fmt.fmtMoney(row[field.prop]) }}
            </template>
            <template v-else-if="'status'===field.prop">
             <ext-d-label type="User.status" v-model="row[field.prop]"/>
            </template>
            <template v-else-if="'rechargeTimes'===field.prop">
              <div class="flex w100 flex-justify-around hp">
                <span>  <el-button link type="primary" @click="handleGotoRecharge(row)">{{row[field.prop]}} <SvgIcon name="ele-Link" class="hc"></SvgIcon></el-button></span>
              </div>
            </template>
            <template v-else-if="'chargeTimes'===field.prop">
              <div class="flex w100 flex-justify-around hp">
                <span>  <el-button link type="primary" @click="handleGotoCharge(row)">{{row[field.prop]}} <SvgIcon name="ele-Link" class="hc"></SvgIcon></el-button></span>
              </div>
            </template>
            <template v-else-if="'refundTimes'===field.prop">
              <div class="flex w100 flex-justify-around hp">
                <span>  <el-button link type="primary" @click="handleGotoRefund(row)">{{row[field.prop]}} <SvgIcon name="ele-Link" class="hc"></SvgIcon></el-button></span>
              </div>
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
  <AdminUserDialog ref="adminUserDialogRef" @refresh="loadData(true)"/>
</template>

<script setup lang="ts" name="AdminUserList">
import {defineAsyncComponent, reactive, onMounted, onBeforeMount, ref, getCurrentInstance, nextTick, onBeforeUnmount} from 'vue';
import {$body, $get} from "/@/utils/request";
import {Msg} from "/@/utils/message";
import u from "/@/utils/u"

import {useRouter} from "vue-router";
const router = useRouter();
import ExtPage from '/@/components/form/ExtPage.vue'

import mittBus from '/@/utils/mitt';
import ExtDLabel from "/@/components/form/ExtDLabel.vue";
import ExtDSelect from "/@/components/form/ExtDSelect.vue";

const AdminUserDialog = defineAsyncComponent(() => import("/@/views/admin/account/detail.vue"));

//定义引用
const queryRef = ref();
const adminUserDialogRef = ref();

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
      {label: '用户ID',width: 180,  prop: 'userId', resizable: true, fixed: 'left'},
      // {label: '用户名',width: 150,  prop: 'userName', resizable: true, fixed: 'left'},
      {label: '手机号', width: 120, prop: 'mobilePhone', resizable: true, fixed: 'left'},
      {label: '余额', width: 80, prop: 'balance', resizable: true, fixed: 'left'},
      {label: '冻结余额', width: 90, prop: 'frozenAmount', resizable: true, fixed: 'left'},
      {label: '状态', width: 80, prop: 'status', align: 'center'},
      {label: '注册时间', width: 160, prop: 'registerTime', resizable: true},
      {label: '充值次数', width: 90, prop: 'rechargeTimes', resizable: true},
      {label: '充值金额', width: 90, prop: 'rechargeAmount', resizable: true},
      {label: '退款次数', width: 90, prop: 'refundTimes', resizable: true},
      {label: '退款金额', width: 90, prop: 'refundAmount', resizable: true},
      {label: '充电次数', width: 90, prop: 'chargeTimes', resizable: true},
      {label: '总电量/度', width: 100, prop: 'totalPower', resizable: true},
      {label: '订单总额', width: 100, prop: 'totalMoney', resizable: true},
      {label: '实付总额', width: 100, prop: 'payAmount', resizable: true},
      {label: '优惠总额', width: 100, prop: 'discountAmount', resizable: true},
      {label: '退款扣除优惠', width: 125, prop: 'refundDiscountAmount', resizable: true},
      // {
      //   label: '操作', prop: 'action', width: 180, align: 'center', fixed: 'right',
      // }
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

  mittBus.on("adminUser.refresh", () => {
    loadData();
  })
});

onBeforeUnmount(() => {
  mittBus.off("adminUser.refresh")
})


//region 方法区
// 初始化表格数据
const loadData = (refresh: boolean = false) => {
  if (refresh) {
    state.pageQuery.pageNum = 1;
  }
  state.tableData.loading = true;
  $get(`/custom/listUser`, {...state.formQuery, ...state.pageQuery}).then((res: any) => {
    let {list, total} = res;
    state.tableData.data = list;
    state.pageQuery.total = total;
    state.tableData.loading = false;
  }).catch(e => {
    console.error(e)
    state.tableData.loading = false;
  })
};

const handleGotoRecharge=(row:any)=>{
  let url = router.resolve(`/finance?mobilePhone=${row.mobilePhone}`);
  window.open(url.href, '_blank');
}

const handleGotoCharge=(row:any)=>{
  let url = router.resolve(`/ordering?mobilePhone=${row.mobilePhone}`);
  window.open(url.href, '_blank');
}

const handleGotoRefund=(row:any)=>{
  let url = router.resolve(`/refund?mobilePhone=${row.mobilePhone}`);
  window.open(url.href, '_blank');
}

// 打开修改用户弹窗
const onRowClick = (type: string, row: any) => {
  adminUserDialogRef.value.open(type, row);
};

// 删除用户
const onRowDel = (row: any) => {
  Msg.confirm(`此操作将永久删除：『${row.name}』，是否继续?`).then(() => {
    $get(`/adminUser/delete/${row.id}`).then(() => {
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