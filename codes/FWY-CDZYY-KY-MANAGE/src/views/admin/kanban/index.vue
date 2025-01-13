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
  <div class="system-container1 layout-padding">

    <el-form
        inline
        :model="state.formQuery"
        ref="queryRef"
        size="default" label-width="0px" class="mt5 mb5">
      <el-form-item>
        <ext-select
            v-model="state.formQuery.stationIdList"
            placeholder="站点"
            value-key="stationId"
            label-key="stationName"
            clearable
            multiple
            @on-change="loadData"
            :data-list="state.stationList"
            style="min-width: 150px;"
            class=" mr10">
        </ext-select>
      </el-form-item>

      <el-form-item>
        <el-radio-group v-model="state.formQuery.type" size="default" @change="handleQueryTypeChange">
          <el-radio-button label="day">天</el-radio-button>
          <el-radio-button label="month">月</el-radio-button>
        </el-radio-group>
        <el-date-picker
            :value-format="state.dateFormat"
            v-model="state.formQuery.dateRange"
            :type="state.dateType"
            @change="loadData"
            unlink-panels
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :shortcuts="shortcuts"
        />
      </el-form-item>

      <el-form-item>
        <el-button class="ml10" plain size="default" type="success" @click="loadData()">
          <SvgIcon name="ele-Search"/>
          查询
        </el-button>
      </el-form-item>
    </el-form>

    <el-scrollbar>
      <el-card>
        <el-card shadow="hover" class="layout-padding-auto">
          <template #header>充电用户及有效订单数</template>
          <template #default>
            <div style="min-height: 500px;padding: 20px;" ref="chartOneRef"></div>
          </template>
        </el-card>

        <el-card shadow="hover" class="layout-padding-auto mt10">
          <template #header>电费/服务费</template>
          <template #default>
            <div style="min-height: 500px;padding: 20px;" ref="chartTwoRef"></div>
          </template>
        </el-card>

        <el-card shadow="hover" class="layout-padding-auto mt10">
          <template #header>电量/平均电量</template>
          <template #default>
            <div style="min-height: 500px;padding: 20px;" ref="chartThreeRef"></div>
          </template>
        </el-card>


      </el-card>

    </el-scrollbar>
  </div>
</template>

<script setup lang="ts" name="KanbanList">
import {defineAsyncComponent, reactive, onMounted, onBeforeMount, ref, getCurrentInstance, nextTick, onUnmounted, markRaw} from 'vue';
import {$body, $get} from "/@/utils/request";
import {Msg} from "/@/utils/message";
import * as echarts from 'echarts';

import mittBus from '/@/utils/mitt';
import ExtSelect from "/@/components/form/ExtSelect.vue";
import u from "/@/utils/u";

//定义引用
const queryRef = ref();
const chartOneRef = ref();
const chartTwoRef = ref();
const chartThreeRef = ref();

const end = new Date()
const start = new Date()
start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)


//定义变量
const state = reactive({
  formQuery: {
    stationIdList: [],
    dateRange: [u.date.format(start, "YYYY-MM-DD"), u.date.format(end, "YYYY-MM-DD")],
    type: 'day'
  },
  stationList: [],
  chartOne: null as any,
  chartTwo: null as any,
  chartThree: null as any,
  chartData: {},
  theme: '',
  dateType: 'daterange',
  dateFormat: 'YYYY-MM-DD'
})


const shortcuts = [
  {
    text: '近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    },
  },
  {
    text: '近90天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    },
  },
]


// 监听双向绑定 modelValue 的变化
// watch(
//         () => state.pageIndex,
//         () => {
//
//         }
// );

//生命周期钩子
onBeforeMount(() => {
  loadStationList()
})

onMounted(() => {
  loadData();
});

onUnmounted(() => {
  state.chartOne?.dispose();
  state.chartTwo?.dispose();
  state.chartThree?.dispose();
})

window.onresize = function () {
  //自适应大小
  state.chartOne.resize();
  state.chartTwo.resize();
  state.chartThree.resize();
};

//region 方法区

const handleQueryTypeChange = (type: string) => {
  console.log(type)
  if (type === 'day') {
    state.dateType = 'daterange'
    state.dateFormat = 'YYYY-MM-DD'
  } else {
    state.dateType = 'monthrange'
    state.dateFormat = 'YYYY-MM-DD'
  }
}

const loadStationList = () => {
  $get(`/station/listStation`, {pageNum: 1024}).then((res: any) => {
    state.stationList = res;
    state.formQuery.stationIdList = res.map(k=>k.stationId);
    console.log(res)
    setTimeout(()=>{
      loadData();
    },200)
  }).catch(e => {
    console.error(e)
  })
}

// 初始化表格数据
const loadData = () => {
  console.log(state.formQuery)
  let params = {
    stationIds: state.formQuery.stationIdList,
    startTime: state.formQuery.dateRange[0],
    endTime: state.formQuery.dateRange[1],
    type: state.formQuery.type
  }
  for (let i = 0; i < Object.keys(params).length; i++) {
    if (u.isEmptyOrNull(params[Object.keys(params)[i]])) {
      return;
    }
  }
  $get(`/stat/stationStatDetail`, params).then(res => {
    console.log(res)
    state.chartData = res;
    buildOrderChart()
    buildMoneyChart()
    buildElectricChart()
  })
};

/**
 * 充电人数、有限订单数散点图
 */
const buildOrderChart = () => {

  if (state.chartOne) {
    state.chartOne.dispose();
  }
  state.chartOne = markRaw(echarts.init(chartOneRef.value, state.theme));
  let {type} = state.formQuery
  let stationIdList = Object.keys(state.chartData);
  if (u.isEmptyOrNull(stationIdList)) {
    Msg.message('未查询到统计数据', 'error')
    return;
  }
  let xAxis = state.chartData[`${stationIdList[0]}`].map((k: any) => type==='day'?k.statDay:k.statMonth);
  let legends:Array<string> =[];
  state.stationList.filter((k: any) => stationIdList.includes(k.stationId)).forEach(item=>{
    legends.push(item.stationName)
    // legends.push(item.stationName+'-平均订单电量')
    // legends.push(item.stationName+'-平均充电电量')
  })

  const itemStyle = {
    opacity: 0.8,
    shadowBlur: 10,
    shadowOffsetX: 0,
    shadowOffsetY: 0,
    shadowColor: 'rgba(0,0,0,0.3)'
  };


  let y1Max = 0,y2Max=0;
  let series: Array<any> = [];
  stationIdList.forEach((stationId: string) => {
    let tmpUserMax = Math.max(...state.chartData[`${stationId}`].map((k: any) => k.chargeUsers))
    y1Max = Math.max(y1Max, tmpUserMax)

    let tmpOrderMax = Math.max(...state.chartData[`${stationId}`].map((k: any) => k.validOrders))
    y2Max = Math.max(y2Max, tmpOrderMax)

    let station = state.stationList.find((k: any) => k.stationId == stationId);
    let {stationName} = station;

    series.push({
      name: stationName,
      type: 'scatter',
      itemStyle:itemStyle,
      data: state.chartData[stationId].map((k: any) =>  {
        return [type==='day'?k.statDay:k.statMonth,k.chargeUsers,k.validOrders]
      })
    })
  });




  const schema = [
    { name: 'date', index: 0, text: '日' },
    { name: '充电人次', index: 1, text: '充电人次' },
    { name: '有效订单', index: 2, text: '有效订单' },
  ];

  let option = {
    color: ['#dd4444', '#fec42c', '#80F1BE','#353EE9'],
    legend: {
      top: 10,
      data: legends,
      textStyle: {
        fontSize: 16
      }
    },
    grid: {
      left: '10%',
      right: 150,
      top: '18%',
      bottom: '10%'
    },
    tooltip: {
      backgroundColor: 'rgba(255,255,255,0.7)',
      formatter: function (param) {
        var value = param.value;
        // prettier-ignore
        return '<div style="border-bottom: 1px solid rgba(255,255,255,.3); font-size: 18px;padding-bottom: 7px;margin-bottom: 7px">'
            + value[0] + '</br> ' +  param.seriesName + ''
            + '</div>'
            + schema[1].text + '：' + value[1] + '人<br>'
            + schema[2].text + '：' + value[2] + '笔<br>'
      }
    },
    xAxis: {
      type: 'category',
      data: xAxis,
    },
    yAxis: {
      type: 'value',
      name: '数量',
      nameLocation: 'end',
      nameGap: 20,
      nameTextStyle: {
        fontSize: 16
      },
    },
    visualMap: [
      {
        left: 'right',
        top: '10%',
        dimension: 1,
        min: 0,
        max: y1Max,
        itemWidth: 30,
        itemHeight: 120,
        calculable: true,
        precision: 0.1,
        text: ['圆形大小：充电人次'],
        textGap: 30,
        inRange: {
          symbolSize: [10, 70]
        },
        outOfRange: {
          symbolSize: [10, 70],
          color: ['rgba(255,255,255,0.4)']
        },
        controller: {
          inRange: {
            color: ['#c23531']
          },
          outOfRange: {
            color: ['#999']
          }
        }
      },
      {
        left: 'right',
        bottom: '5%',
        dimension: 2,
        min: 0,
        max: y2Max,
        itemHeight: 120,
        text: ['明暗：有效订单数'],
        textGap: 30,
        inRange: {
          colorLightness: [0.9, 0.5]
        },
        outOfRange: {
          color: ['rgba(255,255,255,0.4)']
        },
        controller: {
          inRange: {
            color: ['#c23531']
          },
          outOfRange: {
            color: ['#999']
          }
        }
      }
    ],
    series: series
  };

  console.log(option)
  state.chartOne.setOption(option)
}


/**
 * 充电量,柱状、折线图
 */
const buildElectricChart = () => {

  if (state.chartThree) {
    state.chartThree.dispose();
  }
  state.chartThree = markRaw(echarts.init(chartThreeRef.value, state.theme));
  let {type} = state.formQuery
  let stationIdList = Object.keys(state.chartData);
  if (u.isEmptyOrNull(stationIdList)) {
    Msg.message('未查询到统计数据', 'error')
    return;
  }
  let xAxis = state.chartData[`${stationIdList[0]}`].map((k: any) => type==='day'?k.statDay:k.statMonth);
  let legends:Array<string> =[];
  state.stationList.filter((k: any) => stationIdList.includes(k.stationId)).forEach(item=>{
    legends.push(item.stationName+'-总电量')
    legends.push(item.stationName+'-平均充电电量')
    legends.push(item.stationName+'-平均订单电量')
  })

  let y1Max = 0, y2Max = 0;
  let series: Array<any> = [];
  stationIdList.forEach((stationId: string) => {
    let tmpUserMax = Math.max(...state.chartData[`${stationId}`].map((k: any) => k.totalPower))
    y1Max = Math.max(y1Max, tmpUserMax)

    let tmpOrderMax = Math.max(...state.chartData[`${stationId}`].map((k: any) => k.avgOrderElec))
    y2Max = Math.max(y2Max, tmpOrderMax)

    let station = state.stationList.find((k: any) => k.stationId == stationId);
    let {stationName} = station;
    series.push({
      name: stationName+'-总电量',
      type: 'bar',
      barMaxWidth:20,
      symbolSize: 6,
      symbol: 'circle',
      smooth: true,
      itemStyle: { barBorderRadius: 5},
      // tooltip: {
      //   trigger:'axis',
      //   formatter: ' {a0}  {b}<br />{a} :  {c}人 '
      // },
      data: state.chartData[stationId].map((k: any) => k.totalPower)
    })


    series.push({
      name: stationName+'-平均充电电量',
      type: 'line',
      smooth: true,
      yAxisIndex: 1,
      data: state.chartData[stationId].map((k: any) => k.avgConnectorElec)
    })


    series.push({
      name: stationName+'-平均订单电量',
      type: 'line',
      smooth: true,
      yAxisIndex: 1,
      data: state.chartData[stationId].map((k: any) => k.avgOrderElec)
    })


  });



  y1Max = Math.max(y1Max, 80)
  y2Max  =Math.ceil(y2Max +10)

  let y1Interval = Math.ceil(y1Max / 5)
  let y2Interval =  Math.ceil(y2Max / 5)


  let option = {
    tooltip: {
      show:true,
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    toolbox: {
      feature: {
        // dataView: {show: true, readOnly: false},
        // magicType: {show: true, type: ['line', 'bar']},
        // restore: {show: true},
        // saveAsImage: {show: true}
      }
    },
    legend: {
      data: legends
    },
    xAxis: [
      {
        type: 'category',
        data: xAxis,
        axisPointer: {
          type: 'shadow'
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '电量',
        min: 0,
        max: y1Max,
        interval: y1Interval,
        axisLabel: {
          formatter: '{value} 度'
        }
      },
      {
        type: 'value',
        name: '平均电量',
        min: 0,
        max: y2Max,
        interval: y2Interval,
        axisLabel: {
          formatter: '{value} 度'
        }
      }
    ],
    series: series
  };
  // console.log(option)
  state.chartThree.setOption(option)



}


/**
 * 充电金额  电费+服务费=总费用
 */
const buildMoneyChart = () => {
  if (state.chartTwo) {
    state.chartTwo.dispose();
  }
  state.chartTwo = markRaw(echarts.init(chartTwoRef.value, state.theme));
  let {type} = state.formQuery
  let stationIdList = Object.keys(state.chartData);
  if (u.isEmptyOrNull(stationIdList)) {
    Msg.message('未查询到统计数据', 'error')
    return;
  }
  let xAxis = state.chartData[`${stationIdList[0]}`].map((k: any) => type==='day'?k.statDay:k.statMonth);
  let legends:Array<string> =[];
  state.stationList.filter((k: any) => stationIdList.includes(k.stationId)).forEach(item=>{
    legends.push(item.stationName+'-总费用')
    legends.push(item.stationName+'-电费')
    legends.push(item.stationName+'-服务费')
  })

  let y1Max = 0;
  let series: Array<any> = [];
  stationIdList.forEach((stationId: string) => {
    let tmpUserMax = Math.max(...state.chartData[`${stationId}`].map((k: any) => Math.ceil((k.totalMoney/100))))
    y1Max = Math.max(y1Max, tmpUserMax)

    let station = state.stationList.find((k: any) => k.stationId == stationId);
    let {stationName} = station;

    series.push({
      name: stationName+'-总费用',
      type: 'bar',
      barMaxWidth:20,
      symbolSize: 10,
      symbol: 'circle',
      smooth: true,
      itemStyle: { barBorderRadius: 5},
      emphasis: {
        focus: 'series'
      },
      tooltip: {
        formatter: ' {a0}  {b}<br />{a} :  {c}元 '
      },
      markPoint: {
        data: [
          { type: 'max', name: 'Max' },
          { type: 'min', name: 'Min' }
        ]
      },
      markLine: {
        data: [{ type: 'average', name: 'Avg' }]
      },
      data: state.chartData[stationId].map((k: any) => (k.totalMoney/100).toFixed(2))
    })


    series.push({
      name: stationName+'-电费',
      type: 'bar',
      barMaxWidth:20,
      smooth: true,
      stack: 'Fee'+stationId,
      markPoint: {
        data: [
          { type: 'max', name: 'Max' },
          { type: 'min', name: 'Min' }
        ]
      },
      data: state.chartData[stationId].map((k: any) =>  (k.elecMoney/100).toFixed(2))
    })


    series.push({
      name: stationName+'-服务费',
      type: 'bar',
      barMaxWidth:20,
      smooth: true,
      stack:'Fee'+stationId,
      markPoint: {
        data: [
          { type: 'max', name: 'Max' },
          { type: 'min', name: 'Min' }
        ]
      },
      data: state.chartData[stationId].map((k: any) => (k.serviceMoney/100).toFixed(2))
    })

  });

  y1Max = Math.max(y1Max, 1000)+100

  let y1Interval = Math.ceil(y1Max / 5)


  let option = {
    tooltip: {
      show:true,
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    legend: {
      data: legends,
      top:-5
    },
    xAxis: [
      {
        type: 'category',
        data: xAxis,
        axisPointer: {
          type: 'shadow'
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '金额（元）',
        min: 0,
        max: y1Max,
        interval: y1Interval,
        axisLabel: {
          formatter: '{value} '
        }
      },
    ],
    series: series
  };
  // console.log(option)
  state.chartTwo.setOption(option)

}

//endregion

</script>