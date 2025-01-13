<template>
  <div class="system-dept-dialog-container">
    <el-dialog
        :title="state.dialog.title"
        v-model="state.dialog.isShowDialog"
        width="769px"
        draggable
        destroy-on-close
        :close-on-click-modal="false">
      <el-form ref="deptDialogFormRef"
               :rules="rules" status-icon
               :model="state.ruleForm" size="default" label-width="90px">
        <el-row :gutter="35">
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24" class="mb20" v-if="state.ruleForm.level!==1">
            <el-form-item label="上级部门" prop="parent">
              <ExtTreeSelect class="w100" v-model="state.ruleForm.parent" :display-list="state.ruleForm.parentName"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24" class="mb20">
            <el-form-item label="部门名称" prop="name">
              <el-input v-model="state.ruleForm.name" placeholder="请输入部门名称" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24" class="mb20">
            <el-form-item label="部门全称" prop="fullName">
              <el-input v-model="state.ruleForm.fullName" placeholder="请输入部门全称" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12" class="mb20">
            <el-form-item label="排序" prop="weight">
              <el-input-number v-model="state.ruleForm.weight" :min="0" :max="999" controls-position="right"
                               placeholder="请输入排序" class="w100"/>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12" class="mb20">
            <el-form-item label="部门状态" prop="status">
              <ExtDSelect v-model="state.ruleForm.status" type="Department.status"/>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12" class="mb20">
            <el-form-item label="负责人">
              <ExtTreeSelect mode="user" class="w100" v-model="state.ruleForm.leaderIdList" multiple :display-list="state.ruleForm.leaderList"/>
              <!--              <el-input v-model="state.ruleForm.leaderIdList" placeholder="请输入负责人" clearable></el-input>-->
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12" class="mb20">
            <el-form-item label="分管领导">
              <ExtTreeSelect mode="user" class="w100" v-model="state.ruleForm.disLeaderIdList" multiple :display-list="state.ruleForm.disLeaderList"/>
            </el-form-item>
          </el-col>


          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24" class="mb20">
            <el-form-item label="部门描述">
              <el-input v-model="state.ruleForm.remark" type="textarea" placeholder="请输入部门描述"
                        maxlength="150"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-row>
            <el-col :span="12" class="text-align-left">
              <el-button plain type="success" @click="onCancel" size="default">启 用</el-button>
              <el-button plain type="danger" @click="onCancel" size="default">禁用</el-button>
            </el-col>
            <el-col :span="12">
              <el-button @click="onCancel" size="default">取 消</el-button>
              <el-button type="primary" @click="onSubmit(deptDialogFormRef)" size="default">{{ state.dialog.submitTxt }}</el-button>
            </el-col>
          </el-row>

        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="systemDeptDialog">
import {reactive, ref, defineAsyncComponent, onBeforeUnmount, onDeactivated, nextTick} from 'vue';
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import u from "/@/utils/u";
import {$get, $body} from "/@/utils/request";

//组件
const ExtTreeSelect = defineAsyncComponent(() => import("/@/components/form/ExtTreeSelect.vue"))
const ExtDSelect = defineAsyncComponent(() => import("/@/components/form/ExtDSelect.vue"))


// 定义子组件向父组件传值/事件
const emit = defineEmits(['refresh']);

// 定义变量内容
// const deptDialogFormRef = ref();
const deptDialogFormRef = ref<FormInstance>()

const rules = reactive<FormRules>({
  name: [u.validator.required, u.validator.lengthShort],
  parent: [u.validator.required]
})

const initState = () => ({
  ruleForm: {
    id: 0,
    parentName: null, // 上级部门
    parent: null, // 上级部门
    name: '', // 部门名称
    leaderIdList: [], // 负责人
    level: 0, // 排序
    weight: 100, // 排序
    status: 1, // 部门状态
    remark: '', // 部门描述
  },
  deptData: [] as DeptTreeType[], // 部门数据
  dialog: {
    isShowDialog: false,
    type: '',
    title: '',
    submitTxt: '',
  },
  readonly: false
});


// 定义变量内容
const state = reactive(initState());

onDeactivated(() => {
  console.log("onDeactivated")
  deptDialogFormRef.value?.resetFields();
})
onBeforeUnmount(() => {
  console.log("onBeforeUnmount")
  deptDialogFormRef.value?.resetFields();
})
// 打开弹窗
const openDialog = (id: number, readonly: boolean, row: any) => {
  // deptDialogFormRef.value?.resetFields(['name', "weight"]);
  state.readonly = readonly;
  if (id === 0) {
    if (row && row.id > 0) {
      state.ruleForm.parent = row.id;
    }
    state.dialog.title = '新增部门';
    state.dialog.submitTxt = '新 增';
    // 清空表单，此项需加表单验证才能使用
    let {id,name}  = row;
    state.ruleForm.parent = id;
    state.ruleForm.parentName = name;

  } else if (id > 0) {

    if (!readonly) {
      state.dialog.title = '修改部门';
      state.dialog.submitTxt = '修 改';
      loadData(id);
    } else {
      state.dialog.title = '部门详情';
      state.dialog.submitTxt = '确认';
      loadData(id);
    }

  }
  state.dialog.isShowDialog = true;

  // getMenuData();
};
// 关闭弹窗
const closeDialog = () => {
  state.dialog.isShowDialog = false;
  console.log("closeDialog")

  nextTick(() => {

    deptDialogFormRef.value?.resetFields();
    state.ruleForm = Object.assign({});
  })
};
const loadData = (id: number) => {
  $get(`/department/detail/${id}`).then((res: any) => {
    nextTick(() => {
      state.ruleForm = res;
    })
  })
}
// 取消
const onCancel = () => {
  closeDialog();
};
// 提交
const onSubmit = (formEl: FormInstance | undefined) => {
  if (state.readonly) {
    closeDialog();
    return;
  }
  console.log(state.ruleForm)
  if (!formEl) return
  formEl.validate((valid, fields) => {
    console.log(fields)
    if (valid) {
      const url = state.ruleForm.id > 0 ? "department/modify" : "department/add"
      $body(url, state.ruleForm).then(() => {
        ElMessage.success('修改成功');
        console.log('submit!')
        closeDialog();
        emit('refresh');
      })

    } else {
      console.log('error submit!', fields)
    }
  })
}

// 暴露变量
defineExpose({
  openDialog,
});
</script>
