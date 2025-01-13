<template>
  <el-select-v2
      :options="state.list"
      :disabled="disabled"
      :multiple="multiple"
      filterable
      :clearable="clearable"
      :placeholder="placeholder"
      :max-collapse-tags="max"
      @change="handleValueChange"
      @clear="handleValueClear"
      v-model="state.modelVal">
    <template #empty>
      <el-empty :image-size="40"/>
    </template>
    <template #prefix>
      <SvgIcon :name="prefix"/>
    </template>
    <!--    <el-option :key="item.id" v-for="item in state.list" :value="item.id" :label="item.name||item.title"> {{ item.name || item.title }}</el-option>-->
  </el-select-v2>
</template>
<script lang="ts" setup name="ExtSelect">
import {onMounted, ref, nextTick, watch, reactive} from 'vue';
import u from "/@/utils/u";
import {$body, $post, $get} from "/@/utils/request";

const emit = defineEmits(['update:modelValue', 'on-change']);

const props = defineProps({
  modelValue: {
    type: Number
  },
  prefix: {
    type: String
  },
  max: {
    type: Number,
    default: 3
  },
  url: {
    type: String
  },
  query: {
    type: Object
  },
  disabled: {
    type: Boolean,
    default: false
  },
  clearable: {
    type: Boolean,
    default: true
  },
  multiple: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  dataList: {
    type: Array<any>
  },
  urlMethod: {
    type: String,
    default: 'post'
  },
  labelKey: {
    type: String,
    default: 'name'
  },
  valueKey: {
    type: String,
    default: 'id'
  },
  dataKey:{
    type:String,
    default:'list'
  }
})

const state = reactive({
  list: [] as Array<any>,
  modelVal: null
})

watch(() => props.modelValue, (val) => {
  console.log("watch>>>", val)
  state.modelVal = val;
},{immediate:true})


watch(() => props.dataList, (val) => {
  console.log("watch>>>", val)
  nextTick(() => {
    if (!u.isEmptyOrNull(props.dataList)) {
      // state.list = props.dataList;
      state.list = props.dataList?.map((k: any) => {
        return {value: k[props.valueKey], label: k[props.labelKey]}
      })
    }
  })
},{deep:true})


onMounted(() => {
  loadData();
})

const loadData = () => {
  if (!u.isEmptyOrNull(props.dataList)) {
    // state.list = props.dataList;
    state.list = props.dataList?.map((k: any) => {
      return {value: k[props.valueKey], label: k[props.labelKey]}
    })
  } else {
    if (!props.url) {
      return;
    }
    var query = {
      pageIndex: 1,
      pageSize: 200,
    };
    if (props.query) {
      query = Object.assign({}, query, props.query);
    }
    if (props.urlMethod?.toLowerCase() === 'post') {
      $body(`${props.url}`, query).then((list: any) => {
        // let {list,count}  = res;
        let dataList = [];
        if(props.dataKey){
          dataList = list[props.dataKey]
        }else{
          dataList = list
        }
        state.list = dataList.map((k: any) => {
          return {value: k[props.valueKey], label: k[props.labelKey]}
        })
        console.log(state.list)
        // state.value.list = res?.list
      });
    } else {
      $get(`${props.url}`, query).then((list: any) => {
        // let {list,count}  = res;
        let dataList = [];
        if(props.dataKey){
          dataList = list[props.dataKey]
        }else{
          dataList = list
        }
        state.list = dataList.map((k: any) => {
          return {value: k[props.valueKey], label: k[props.labelKey]}
        })

/*
        if (list.list) {
          state.list = list.list.map((k: any) => {
            return {value: k[props.valueKey], label: k[props.labelKey]}
          })
        } else {
          state.list = list.map((k: any) => {
            return {value: k[props.valueKey], label: k[props.labelKey]}
          })
        }
*/

        console.log(state.list)
        // state.value.list = res?.list
      });
    }

  }

}

const handleValueChange = (val: any) => {
  nextTick(() => {
    console.log("handleValueChange--->", val)
    emit("update:modelValue", val);
    emit("on-change", val);
  })
}

const handleValueClear = () => {
  // props.query.id = undefined;
  // this.query['id'] = undefined;
  // let val: any = [];
  // if (!props.multiple) {
  //   val = undefined;
  // }
  // // emit("update:value", val);
  // // emit("change", val);
  // loadData();
}


</script>
