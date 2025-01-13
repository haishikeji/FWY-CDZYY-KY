<template>
  <div class="system-role-dialog-container">
    <el-dialog :title="state.dialog.title" v-model="state.dialog.isShowDialog" width="769px">
      <el-form ref="roleDialogFormRef" :model="state.ruleForm" :rules="state.rules" size="default" label-width="90px">
        <el-row :gutter="35">
          <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12" class="mb20">
            <el-form-item label="角色名称" prop="roleName">
              <template #label>
                <el-tooltip effect="dark" content="用于标识日常使用的角色名称" placement="top-start">
                  <span>角色名称</span>
                </el-tooltip>
              </template>
              <el-input v-model="state.ruleForm.roleName" placeholder="请输入角色标识" clearable></el-input>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12" class="mb20">
            <el-form-item label="角色别名" prop="roleDesc">
              <el-input v-model="state.ruleForm.roleDesc" placeholder="请输入角色别名" clearable></el-input>
            </el-form-item>
          </el-col>
<!--
          <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12" class="mb20">
            <el-form-item label="排序">
              <el-input-number v-model="state.ruleForm.weight" :min="0" :max="999" controls-position="right" placeholder="请输入排序" class="w100"/>
            </el-form-item>
          </el-col>-->
          <!--          <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12" class="mb20">
                      <el-form-item label="角色状态">
                        <el-switch v-model="state.ruleForm.status" inline-prompt active-text="启" inactive-text="禁"></el-switch>
                      </el-form-item>
                    </el-col>-->
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24" class="mb20">
            <el-form-item label="权限组">
              <el-input readonly  v-model="state.ruleForm.permissions" type="textarea" placeholder="请输入角色描述" ></el-input>
            </el-form-item>
          </el-col>
          <!--          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24" class="mb20">
                      <el-form-item label="菜单权限">
                        <el-tree :data="state.menuData" :props="state.menuProps" show-checkbox class="menu-data-tree"/>
                      </el-form-item>
                    </el-col>-->
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24" class="mb20">
            <span style="margin-left: 5px">
              <el-button v-auth="'role.delete'"   type="danger" @click="handleDelete" size="default">删除角色</el-button>
            </span>
          </el-col>

        </el-row>
      </el-form>
      <template #footer>

				<span class="dialog-footer">
					<el-button @click="onCancel" size="default">取 消</el-button>
					<el-button type="primary" @click="onSubmit(roleDialogFormRef)" size="default">{{ state.dialog.submitTxt }}</el-button>
				</span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="systemRoleDialog">
import {reactive, ref} from 'vue';
import {Msg} from "/@/utils/message";
import {$body, $get} from "/@/utils/request";
import u from "/@/utils/u";
import type {FormInstance, FormRules} from 'element-plus'

// 定义子组件向父组件传值/事件
const emit = defineEmits(['refresh']);

// 定义变量内容
const roleDialogFormRef = ref();
const state = reactive({
  readonly:false,
  action:'add',
  ruleForm: {
    id:0,
    name: '', // 角色名称
    roleSign: '', // 角色标识
    sort: 0, // 排序
    status: true, // 角色状态
    describe: '', // 角色描述
  },
  rules: {
    roleName: [u.validator.required, u.validator.length32],
  },
  menuData: [] as TreeType[],
  menuProps: {
    children: 'children',
    label: 'label',
  },
  dialog: {
    isShowDialog: false,
    type: '',
    title: '',
    submitTxt: '',
  },
});

// 打开弹窗
const openDialog = (id: number, readonly: boolean) => {
  state.readonly = readonly;
  if (readonly) {
    if (id > 0) {
      state.dialog.title = '角色详情';
      state.dialog.submitTxt = '确定';
      loadData(id);
    } else {
      Msg.message('角色数据错误', 'error')
      return;
    }
  } else {
    if (id > 0) {
      state.dialog.title = '修改角色';
      state.dialog.submitTxt = '修 改';
      loadData(id)
    } else {
      state.dialog.title = '新增角色';
      state.dialog.submitTxt = '新 增';
    }
  }
  state.dialog.isShowDialog = true;
};
// 关闭弹窗
const closeDialog = () => {
  state.dialog.isShowDialog = false;
};
// 取消
const onCancel = () => {
  closeDialog();
};

const handleDelete=()=>{
  Msg.confirm(`确认删除该角色:${state.ruleForm.name}吗？该操作无法撤回`).then(()=>{
    $get(`/role/remove/${state.ruleForm.id}`).then(()=>{
      Msg.message("操作成功","success")
    })
  })
}
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
      const url = state.ruleForm.id > 0 ? "role/modify" : "role/add"
      $body(url, state.ruleForm).then(() => {
        Msg.message("操作成功","success")
        console.log('submit!')
        closeDialog();
        emit('refresh');
      })

    } else {
      console.log('error submit!', fields)
    }
  })
};
// 获取菜单结构数据
const loadData = (id: number) => {
  $get(`role/detail/${id}`).then((res: any) => {
    state.ruleForm = res;
  })
}


// 暴露变量
defineExpose({
  openDialog,
});
</script>

<style scoped lang="scss">
.system-role-dialog-container {
  .menu-data-tree {
    width: 100%;
    border: 1px solid var(--el-border-color);
    border-radius: var(--el-input-border-radius, var(--el-border-radius-base));
    padding: 5px;
  }
}
</style>
