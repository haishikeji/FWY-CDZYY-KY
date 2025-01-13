<style scoped lang="scss">

</style>
<template>
  <div class="system-dialog-container">
    <el-drawer
        :title="state.dialog.title"
        v-model="state.dialog.isShowDialog"
        width="820px"
        append-to-body
        destroy-on-close
        :close-on-click-modal="false"
    >
      <el-form
          :model="state.form"
          :rules="rules"
          label-position="left"
          ref="formRef"
          size="default"
          label-width="100px"
          class="mt5">
        <el-input
            v-model="state.formQuery.avatar"
            placeholder="头像"
            clearable
            style="width: 100%">
        </el-input>
        <el-input
            v-model="state.formQuery.companyId"
            placeholder="公司id"
            clearable
            style="width: 100%">
        </el-input>
        <el-input
            v-model="state.formQuery.lastLoginTime"
            placeholder="最后登录时间"
            clearable
            style="width: 100%">
        </el-input>
        <el-input
            v-model="state.formQuery.mobilePhone"
            placeholder="手机号"
            clearable
            style="width: 100%">
        </el-input>
        <el-input
            v-model="state.formQuery.nickname"
            placeholder="昵称"
            clearable
            style="width: 100%">
        </el-input>
        <el-input
            v-model="state.formQuery.password"
            placeholder="密码"
            clearable
            style="width: 100%">
        </el-input>
        <el-input
            v-model="state.formQuery.status"
            placeholder="0:禁用 1:启用"
            clearable
            style="width: 100%">
        </el-input>
        <el-input
            v-model="state.formQuery.username"
            placeholder="用户名"
            clearable
            style="width: 100%">
        </el-input>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="onCancel" size="default">取 消</el-button>
          <el-button :loading="state.btnLoading" type="primary" @click="onSubmit" size="default">{{ state.dialog.submitTxt }}</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts" name="AdminUserDialog">
import {defineAsyncComponent, reactive, onMounted, ref} from 'vue';
import {Msg} from "/@/utils/message";
import {$body, $get} from "/@/utils/request";
import u from '/@/utils/u'
import type {FormInstance, FormRules} from 'element-plus';

// 定义子组件向父组件传值/事件
const emit = defineEmits(['refresh']);
const formRef = ref();
//定义初始变量，重置使用
const initState = () => ({
  ruleForm: {
    id: 0
  },
  btnLoading: false,
  dialog: {
    isShowDialog: false,
    type: '',
    title: '',
    submitTxt: '',
  },
  rules: {},
})

// 定义变量内容
const state = reactive(initState());


// 打开弹窗
const open = (action: string = 'add', row: any) => {
  state.dialog.title = u.dialog.actions[action].title + "『用户表』"
  state.dialog.submitTxt = u.dialog.actions[action].btn + "『用户表』"
  state.dialog.isShowDialog = true;
  if (action !== 'add') {
    loadData(row.id);
  }
};
// 关闭弹窗
const onClose = () => {
  state.dialog.isShowDialog = false;
  Object.assign(state, initState())
};
// 取消
const onCancel = () => {
  onClose();
};
// 提交
const onSubmit = () => {
  formRef.value.validate((valid, fields) => {
    // console.log('basic checkForm!', valid,fields)
    if (valid) {
      state.btnLoading = true;
      const url = state.ruleForm.id > 0 ? "adminUser/modify" : "adminUser/add"
      $body(url, state.ruleForm).then(() => {
        state.btnLoading = false;
        Msg.message('操作成功');
        console.log('submit!')
        onClose();
        emit('refresh');
      })
    } else {
      state.btnLoading = false;
      Msg.message('表单校验失败', 'error');
    }
  })

  // formRef.value.checkForm().then(() => {
  //
  // }).catch(() => {
  //     state.btnLoading = false;
  //     Msg.message('表单校验失败', 'error');
  // })
};

const handleFormChange = (formData: any) => {
  console.log(formData)
}

// 初始化表格数据
const loadData = (id: number) => {
  $get(`adminUser/detail/${id}`).then((res: any) => {
    state.ruleForm = res;
  })
}

// 暴露变量
defineExpose({
  open
});


</script>