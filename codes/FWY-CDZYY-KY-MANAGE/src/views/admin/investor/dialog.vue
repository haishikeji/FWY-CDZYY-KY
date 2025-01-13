<style scoped lang="scss">

</style>
<template>
  <div class="system-dialog-container">
    <el-drawer
        :title="state.dialog.title"
        v-model="state.dialog.isShowDialog"
        size="600"
        class="pd10"
        append-to-body
        destroy-on-close
        :close-on-click-modal="false"
    >
      <el-form
          inline
          :model="state.ruleForm"
          :rules="state.rules"
          label-position="top"
          ref="formRef"
          size="default"
          label-width="100px"
          class="mt5">
        <el-form-item label="账户名" prop="accountName">
          <el-input
              v-model="state.ruleForm.accountName"
              placeholder="账户名"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>
        <el-form-item label="客户名称" prop="adminUserName">
          <el-input
              v-model="state.ruleForm.adminUserName"
              placeholder="客户名称"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>
        <el-form-item label="关联客户" prop="adminUserId">
          <ext-select
              v-model="state.ruleForm.adminUserId"
              placeholder="关联用户"
              url="admin-user/list"
              url-method="get"
              label-key="nickname"
              clearable
              class="wd200 ">
          </ext-select>

<!--          <el-input
              v-model="state.ruleForm.adminUserId"
              placeholder="客户用户id"
              clearable
              class="wd200">
          </el-input>-->
        </el-form-item>

        <el-form-item label="管理站点" prop="stationId">
          <ext-select
              v-model="state.ruleForm.stationId"
              placeholder="关联站点"
              url="station/listStation"
              url-method="get"
              label-key="stationName"
              value-key="stationId"
              data-key=""
              clearable
              class="wd200 ">
          </ext-select>
          <!--          <el-input
                        v-model="state.ruleForm.stationId"
                        placeholder="站点id"
                        clearable
                        class="wd200">
                    </el-input>-->
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <ext-d-select
              v-model="state.ruleForm.status"
              placeholder="状态"
              type="Investor.status"
              clearable
              class="wd200 "/>
        </el-form-item>
        <el-form-item label="电话号码" prop="telephone">
          <el-input
              v-model="state.ruleForm.telephone"
              placeholder="电话号码"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>

        <el-form-item label="银行卡号" prop="bankCardNo">
          <el-input
              v-model="state.ruleForm.bankCardNo"
              placeholder="银行卡号"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>
        <el-form-item label="开户行名称" prop="bankName" >
          <el-input
              v-model="state.ruleForm.bankName"
              placeholder="开户行名称"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>

        <el-form-item label="税号" prop="taxNo">
          <el-input
              v-model="state.ruleForm.taxNo"
              placeholder="税号"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>
        <el-form-item label="增值税率" prop="vatRate">
          <el-input
              v-model="state.ruleForm.vatRate"
              placeholder="增值税率 0.06表示6%"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>

        <el-form-item label="电损承担比例 (0.30表示30%)" prop="elecLossProportion">
          <el-input
              v-model="state.ruleForm.elecLossProportion"
              placeholder="电损承担比例 0.30表示30%"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>

        <el-form-item label="分成比例 （0.45表示45%）" prop="splittingProportion">
          <el-input
              v-model="state.ruleForm.splittingProportion"
              placeholder="分成比例 0.45表示45%"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>

<!--        <el-form-item label="站点名称" prop="stationName">
          <el-input
              v-model="state.ruleForm.stationName"
              placeholder="站点名称"
              clearable
              class="wd200">
          </el-input>
        </el-form-item>-->



        <el-form-item label="备注" prop="remark" class="w100">
          <el-input
              v-model="state.ruleForm.remark"
              placeholder="备注"
              clearable
              :rows="3"
              type="textarea"
              class="w100">
          </el-input>
        </el-form-item>
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

<script setup lang="ts" name="InvestorInfoDialog">
import {defineAsyncComponent, reactive, onMounted, ref} from 'vue';
import {Msg} from "/@/utils/message";
import {$body, $get} from "/@/utils/request";
import u from '/@/utils/u'
import ExtSelect from "/@/components/form/ExtSelect.vue";
import ExtDSelect from "/@/components/form/ExtDSelect.vue";

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
  rules: {
    telephone:[u.validator.mobile]
  },
})

// 定义变量内容
const state = reactive(initState());


// 打开弹窗
const open = (action: string = 'add', row: any) => {
  state.dialog.title = u.dialog.actions[action].title + "『投资者/物业』"
  state.dialog.submitTxt = u.dialog.actions[action].btn + "『投资者/物业』"
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
      const url = state.ruleForm.id ? "investorInfo/update" : "investorInfo/create"
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

};

const handleFormChange = (formData: any) => {
  console.log(formData)
}

// 初始化表格数据
const loadData = (id: any) => {
  $get(`investorInfo/${id}`).then((res: any) => {
    state.ruleForm = res;
  })
}

// 暴露变量
defineExpose({
  open
});


</script>