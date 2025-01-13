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
            type="Connector.status"
            v-model="state.formQuery.status"
            placeholder="状态"
            clearable
            @change="loadData(true)"
            class="wd150 mr10"/>

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
        <el-input
            v-model="state.formQuery.equipmentId"
            placeholder="充电桩编号/序列号"
            clearable
            @change="loadData(true)"
            class="wd150 mr10">
        </el-input>


        <el-button class="ml10" plain size="default" type="success" @click="loadData(true)">
          <SvgIcon name="ele-Search"/>
          查询
        </el-button>

<!--        <el-button class="ml10" plain size="default" type="success" @click="handleUploadVisible">-->
<!--          <SvgIcon name="ele-Upload"/>-->
<!--          上传-->
<!--        </el-button>-->
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
            <template v-else-if="'equipmentType'===field.prop">
              <ext-d-label type="Equipment.type" v-model="row[field.prop]"/>
            </template>
            <template v-else-if="'status'===field.prop">
              <ext-d-label type="Connector.status" v-model="row[field.prop]"/>
            </template>
<!--            <template v-else-if="'netStatus'===field.prop">-->
<!--              <ext-d-label type="Equipment.netStatus" v-model="row[field.prop]"/>-->
<!--            </template>-->
            <template v-else-if="'equipmentId'===field.prop">
              <el-button link type="primary" @click="handleGotoOrder(row)">{{row[field.prop]}} </el-button>
            </template>
            <template v-else-if="'shortId'===field.prop">
              <el-button link type="primary" @click="handleGotoOrder(row)">{{row[field.prop]}} </el-button>
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
  <!--  <EquipmentInfoDialog ref="equipmentInfoDialogRef" @refresh="loadData(true)"/>-->
  <endpoint-upload ref="endpoint_upload_ref"></endpoint-upload>
</template>

<script setup lang="ts" name="EquipmentInfoList">
import {defineAsyncComponent, reactive, onMounted, onBeforeMount, ref, getCurrentInstance, nextTick, onBeforeUnmount} from 'vue';
import {$body, $get} from "/@/utils/request";

import {useRoute} from "vue-router";

const route = useRoute();
import ExtPage from '/@/components/form/ExtPage.vue'

import mittBus from '/@/utils/mitt';
import ExtDLabel from "/@/components/form/ExtDLabel.vue";
import ExtDSelect from "/@/components/form/ExtDSelect.vue";
import ExtSelect from "/@/components/form/ExtSelect.vue";

import {useRouter} from "vue-router";
const router = useRouter();
const EndpointUpload = defineAsyncComponent(() => import("/@/views/admin/station/endpoint/upload.vue"));

//定义引用
const queryRef = ref();
const endpoint_upload_ref = ref();

//定义变量
const state = reactive({
  uploadVisible:false,
  formQuery: {
    stationId: ''
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
      {label: '站点ID', prop: 'stationId', resizable: true, width: 100, fixed: 'left'},
      {label: '站点编号', prop: 'stationNo', resizable: true, width: 100, fixed: 'left'},
      {label: '站点名称', prop: 'stationName', resizable: true, width: 200, fixed: 'left'},
      {label: '充电桩编号', prop: 'shortId', resizable: true, width: 110, fixed: 'left'},
      {label: '车位编号', prop: 'parkingNo', resizable: true, width: 90, fixed: 'left'},
      {label: '充电桩序列号', prop: 'equipmentId', width: 180, resizable: true},
      {label: '充电桩接口编号', prop: 'connectorId', width: 180, resizable: true},
      // {label: '设备型号', prop: 'equipmentModel', width: 150, resizable: true},
      // {label: '服务状态', prop: 'status', resizable: true, width: 130},
      // {label: '设备类型', prop: 'equipmentType', resizable: true, width: 130},
      // {label: '位置坐标', prop: 'location', resizable: true, width: 150},
      // {label: '网络状态', prop: 'netStatus', resizable: true, width: 130},
      {label: '状态', prop: 'status', resizable: true, width: 130},
      // {label: '设备生产商组织机构代码', prop: 'manufacturerId', width: 200, resizable: true},
      // {label: '设备生产商名称', prop: 'manufacturerName', width: 160, resizable: true},
      // {label: '设备生产日期', prop: 'productionDate', width: 160, resizable: true},
      {label: '更新时间', prop: 'updateTime', sortable: 'custom', resizable: true, width: 180},
      /*{
        label: '操作', prop: 'action', width: 1, align: 'center', fixed: 'right',
      }*/
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
  var params = route.params;
  console.log(route.params, route.query)
  let {id} = route.params;
  if (!isNaN(Number(id))) {
    state.formQuery.stationId = id;
  }
  loadData();

  nextTick(() => {
    let bodyHeight = document.body.clientHeight;
    let queryHeight = queryRef.value.$el.clientHeight;
    state.tableData.height = bodyHeight - queryHeight - 320
  })

  mittBus.on("equipmentInfo.refresh", () => {
    loadData();
  })
});

onBeforeUnmount(() => {
  mittBus.off("equipmentInfo.refresh")
})


//region 方法区
const handleGotoOrder =(row:any)=>{
  router.push(`/ordering?connectorId=${row.equipmentId}`)
}


// 初始化表格数据
const loadData = (refresh: boolean = false) => {
  if (refresh) {
    state.pageQuery.pageNum = 1;
  }
  state.tableData.loading = true;
  $get(`/connector/listConnectors`, {...state.formQuery, ...state.pageQuery}).then((res: any) => {
    let {list, total} = res;
    state.tableData.data = list;
    state.pageQuery.total = total;
    state.tableData.loading = false;
  }).catch(e => {
    console.error(e)
    state.tableData.loading = false;
  })
};
/*

// 打开修改用户弹窗
const onRowClick = (type: string, row: any) => {
  equipmentInfoDialogRef.value.open(type, row);
};

// 删除用户
const onRowDel = (row: any) => {
  Msg.confirm(`此操作将永久删除：『${row.name}』，是否继续?`).then(() => {
    $get(`/equipmentInfo/delete/${row.id}`).then(() => {
      Msg.message("删除成功", 'success')
    }).catch(() => {
      Msg.message("删除失败", 'error')
    })
  });
};
*/

const handleTableSelectionChange = (selection: any) => {
  console.log("handleTableSelectionChange>>", selection)
  // emit("on-check-change", selection)
}

const handleTableSortChange = (column, prop, order) => {
  console.log("handleTableSortChange>>", column, prop, order)
  // emit("on-sort-change", column)
}

const handleUploadVisible = () => {
  endpoint_upload_ref.value.open();
}


//endregion


// 暴露变量
// defineExpose({
//     loadData,
// });
</script>