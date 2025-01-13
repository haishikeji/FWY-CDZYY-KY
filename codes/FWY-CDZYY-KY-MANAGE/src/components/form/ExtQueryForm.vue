<template>
  <div class="dynamic-state.form-container">
    <el-card shadow="hover">
      <el-form
          :model="state.form"
          :rules="rules"
          ref="formRulesOneRef"
          size="default" label-width="0px" class="mt5">
        <el-row :gutter="15">
          <el-col
              :xs="field.xs"
              :sm="field.sm"
              :md="field.md"
              :lg="field.md"
              :xl="field.xl"
              class="mb20"
              v-for="(field, key) in state.cols"
              :key="key">
            <el-input
                v-if="field.type === 'text'"
                v-model="state.form[field.prop]"
                :placeholder="field.placeholder"
                clearable
                @blur="onQueryChange"
                style="width: 100%">
            </el-input>
            <el-input
                v-if="field.type === 'number'"
                type="number"
                v-model="state.form[field.prop]"
                :placeholder="field.placeholder"
                clearable
                @blur="onQueryChange"
                style="width: 100%">
            </el-input>
            <el-date-picker
                v-else-if="field.type === 'datetime'"
                v-model="state.form[field.prop]"
                type="datetime"
                :placeholder="field.placeholder"
                style="width: 100%"
                @change="onQueryChange">
            </el-date-picker>
            <ext-date-picker
                v-else-if="field.type === 'year'"
                v-model="state.form[field.prop]"
                type="year"
                format="YYYY"
                :placeholder="field.placeholder"
                style="width: 100%"
                @on-change="onQueryChange">
            </ext-date-picker>
            <el-date-picker
                v-else-if="field.type === 'month'"
                v-model="state.form[field.prop]"
                type="month"
                format="YYYY-MM"
                value-format="YYYY-MM"
                :placeholder="field.placeholder"
                style="width: 100%"
                @change="onQueryChange">
            </el-date-picker>
            <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="state.form[field.prop]"
                type="date"
                :value-format="field?.conf?.vfmt"
                :placeholder="field.placeholder"
                style="width: 100%"
                @change="onQueryChange">
            </el-date-picker>
            <el-time-picker
                v-else-if="field.type === 'time'"
                v-model="state.form[field.prop]"
                type="time"
                :placeholder="field.placeholder"
                style="width: 100%"
                @change="onQueryChange">
            </el-time-picker>
            <ext-d-select
                v-else-if="field.type === 'dict'"
                v-model="state.form[field.prop]"
                :type="field.conf?.dict"
                :data-range="field.conf?.dataRange"
                :placeholder="field.placeholder"
                :multiple="field.conf?.multiple"
                style="width: 100%"
                @on-change="onQueryChange">
            </ext-d-select>
            <ext-select
                v-else-if="field.type === 'select'"
                v-model="state.form[field.prop]"
                :url="field.conf?.url"
                :query="field.conf?.query"
                :multiple="field.conf?.multiple"
                :placeholder="field.placeholder"
                style="width: 100%"
                @on-change="onQueryChange">
            </ext-select>
            <ext-search
                v-else-if="field.type === 'search'"
                v-model="state.form[field.prop]"
                :multiple="field.conf?.multiple"
                :placeholder="field.placeholder"
                :config="field.conf"
                style="width: 100%"
                @on-change="onQueryChange">
            </ext-search>

            <ext-tree-select
                v-else-if="field.type === 'dept'"
                v-model="state.form[field.prop]"
                :multiple="field.conf?.multiple"
                :placeholder="field.placeholder"
                style="width: 100%"
                @on-change="onQueryChange">
            </ext-tree-select>

            <ext-tree-select
                v-else-if="field.type === 'user'"
                v-model="state.form[field.prop]"
                mode="user"
                :multiple="field.conf?.multiple"
                :placeholder="field.placeholder"
                style="width: 100%"
                @on-change="onQueryChange">
            </ext-tree-select>

            <ext-boolean
                v-else-if="field.type === 'bool'"
                v-model="state.form[field.prop]"
                :placeholder="field.placeholder"
                style="width: 100%"
                :disabled="field.disabled"
                @on-change="onQueryChange">
            </ext-boolean>

          </el-col>
          <slot name="extQuery"></slot>
        </el-row>
      </el-form>
      <el-row class="flex-warp mt5">
        <div>
          <el-button-group>
            <el-button plain size="default" type="primary" @click="onResetForm(formRulesOneRef)">
              <SvgIcon name="ele-RefreshRight"/>
              重置
            </el-button>
            <el-button plain size="default" type="primary" @click="onQueryChange">
              <SvgIcon name="ele-Search"/>
              查询
            </el-button>

            <slot name="extraLeft"></slot>
          </el-button-group>
        </div>
        <div class="flex flex-auto" style="justify-content: flex-end">
          <slot name="extraRight"></slot>
        </div>
      </el-row>

      <ext-import
          @confirm="handleImport"
          ref="import_ref"
          v-if="state.importEnable"
          :importUrl="importConfig.url"
          :templateUrl="importConfig.template"
          v-auths="importConfig.auths">
      </ext-import>
    </el-card>
  </div>
</template>

<script setup lang="ts" name="ExtForm">
import {computed, defineAsyncComponent, onMounted, ref} from 'vue';
import type {FormInstance} from 'element-plus';
import fieldUtil from "/@/utils/field";
import ExtDSelect from "/@/components/form/ExtDSelect.vue";
import ExtSelect from "/@/components/form/ExtSelect.vue";
import ExtTreeSelect from "/@/components/form/ExtTreeSelect.vue";
import ExtSearch from "/@/components/form/ExtSearch.vue";
import {Msg} from "/@/utils/message";
import u from "/@/utils/u";
import ExtDatePicker from "/@/components/form/ExtDatePicker.vue";

const ExtBoolean = defineAsyncComponent(() => import('/@/components/form/ExtBoolean.vue'))
// import ExtImport from "/@/components/form/ExtImport.vue";

const emit = defineEmits(['on-change', 'update:modelValue']);
const props = defineProps({
  columns: {
    type: Array < IField >,
    require: true,
    default: () => []
  },
  modelValue: {
    type: Object,
    require: true
  },
  importConfig: {
    type: Object,
    default: () => {
      return {url: '', auths: [], template: ''}
    }
  },
  exportConfig: {
    type: Object
  },
  pageQuery: {
    type: Object
  }
})


const rules = computed(() => {
  return [];
})

// 定义变量内容
const import_ref = ref();
const formRulesOneRef = ref<FormInstance>();
const state = ref({
  cols: [] as Array<any>,
  form: {},
  importEnable: false,
  exportEnable: false,
});

onMounted(() => {
  if (props.importConfig && props.importConfig.url) {
    state.value.importEnable = true;
  }
  if (props.exportConfig && props.exportConfig.url) {
    state.value.exportEnable = true;
  }

  console.log(props.columns)
  state.value.cols = fieldUtil.toFormQueryField(props.columns);
  console.log(state.value.cols)
  let val = {...props.modelValue}
  state.value.cols.forEach((col: any) => {
    val[col.prop] = null;
  })
  console.log(val)
  state.value.form = val;
})

const onQueryChange = () => {
  console.log("onQueryChange>>>", JSON.stringify(state.value.form))
  emit("update:modelValue", state.value.form);
  emit("on-change")
}

// 重置表单
const onResetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.resetFields();
  props.columns.forEach(key => {
    if (key.type == 'datetime' || key.type == 'date' || key.type == 'daterange') {
      state.value.form[key.prop + 'Start'] = null
      state.value.form[key.prop + 'End'] = null
    }
    state.value.form[key.prop] = null
  })
  // console.log(state.value.form,props.columns)
  emit("update:modelValue", {})
  emit("on-change")
};
</script>
