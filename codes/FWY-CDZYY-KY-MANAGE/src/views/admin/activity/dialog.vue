<style scoped lang="scss">
.preview {
  width: 430px;
  height: 850px;
  background-image: url('/@/assets/phone.png');
  background-repeat: no-repeat;

  &_body {
    margin: 110px 40px;
    height: 600px;
    overflow: auto;
  }
}
</style>
<template>
  <div class="system-dialog-container">
    <el-dialog
        :title="state.dialog.title"
        v-model="state.dialog.isShowDialog"
        width="920px"
        draggable
        destroy-on-close
        :close-on-click-modal="false"
        @close="onClose"
        align-center>
      <el-form
          inline
          :model="state.ruleForm"
          :rules="state.rules"
          ref="formRef"
          size="default"
          label-width="150px"
          class="mt5">

        <div class="sub-group-bottom"> 基本信息</div>
        <el-form-item label="活动名称" prop="name" class="w100">
          <el-input
              v-model.trim="state.ruleForm.name"
              placeholder="活动名称"
              clearable
              class="w100">
          </el-input>
        </el-form-item>

        <el-form-item label="活动描述" prop="remark" class="w100"></el-form-item>
        <div class="w100 flex mt20 mb20 flex-justify-between">
          <ext-editor style="width:380px;flex:1;margin-right: 10px;" ref="ext_editor_ref"
                      v-model="state.ruleForm.activityDesc"></ext-editor>

          <!--          <el-divider direction="vertical"></el-divider>-->
          <div class="preview">
            <div class="preview_body" v-html="state.ruleForm.activityDesc"></div>
          </div>
        </div>

        <el-form-item label="开始时间" prop="startTime">
          <ext-date-picker
              v-model.trim="state.ruleForm.startTime"
              placeholder="开始时间"
              type="datetime"
              clearable
              class="wd200">
          </ext-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <ext-date-picker
              v-model.trim="state.ruleForm.endTime"
              placeholder="结束时间"
              type="datetime"
              clearable
              class="wd200">
          </ext-date-picker>
        </el-form-item>
        <!--        RechargeRights  "Coupon"-->
        <el-form-item label="优惠方式" prop="discountType">
          <ext-d-select
              v-model="state.ruleForm.discountType"
              placeholder="优惠方式"
              type="Activity.discountType"
              clearable
              class="wd200 ">
          </ext-d-select>
        </el-form-item>
        <!--        <el-form-item label="目标用户" prop="targetUsers" v-if="state.ruleForm.discountType==='RechargeRights'">-->
        <!--          <ext-d-select-->
        <!--              v-model="state.ruleForm.targetUsers"-->
        <!--              placeholder="目标用户"-->
        <!--              type="Activity.targetUsers"-->
        <!--              clearable-->
        <!--              class="wd200 ">-->
        <!--          </ext-d-select>-->
        <!--        </el-form-item>-->

        <!--        <el-form-item label="优惠允许叠加" prop="allowStacke">-->
        <!--          <ext-d-select-->
        <!--              v-model="state.ruleForm.allowStacke"-->
        <!--              placeholder="优惠允许叠加"-->
        <!--              type="Activity.allowStacke"-->
        <!--              clearable-->
        <!--              class="wd200 ">-->
        <!--          </ext-d-select>-->
        <!--        </el-form-item>-->
        <!--        <el-form-item label="数量限制" prop="quantity">-->
        <!--          <el-input-->
        <!--              type="number"-->
        <!--              v-model.trim="state.ruleForm.quantity"-->
        <!--              placeholder="数量限制"-->
        <!--              clearable-->
        <!--              class="wd200">-->
        <!--          </el-input>-->
        <!--        </el-form-item>-->
        <!--        <el-form-item label="活动状态" prop="status">
                  <ext-d-select
                      v-model="state.ruleForm.status"
                      placeholder="活动状态"
                      type="Activity.status"
                      clearable
                      class="wd200 ">
                  </ext-d-select>
                </el-form-item>-->
        <el-form-item label="备注" prop="remark" class="w100">
          <el-input
              maxlength="500"
              show-word-limit
              type="textarea"
              :rows="3"
              v-model.trim="state.ruleForm.remark"
              placeholder="备注信息"
              clearable
              class="w100">
          </el-input>

        </el-form-item>

        <div class="sub-group-bottom"> 关联站点</div>
        <!--        <el-divider content-position="left">关联站点</el-divider>-->
        <el-form-item label="适用站点" prop="applyStation">
          <ext-d-select
              @on-change="handleStationChange"
              v-model="state.ruleForm.applyStation"
              placeholder="适用站点"
              type="Activity.applyStation"
              clearable
              class="wd200 ">
          </ext-d-select>

          <ext-select
              v-model="state.ruleForm.stationIds"
              multiple
              placeholder="关联站点"
              url="station/listStation"
              url-method="get"
              label-key="stationName"
              value-key="stationId"
              data-key=""
              clearable
              class="w100 mt5">
          </ext-select>
        </el-form-item>

        <div class="sub-group-bottom"> 关联权益</div>
        <!--        <el-divider content-position="left">关联权益</el-divider>-->
        <el-button size="small" plain type="success" class="ml10" @click="handleAddRightsItem">
          <SvgIcon name="ele-FolderAdd"/>
          新增权益
        </el-button>

        <!--        服务费折扣权益 -->
        <template v-if="state.ruleForm.discountType==='RechargeRights'">
          <el-card v-for="(rights,idx) in state.ruleForm.rechargeRightsList" :key="idx" class="mt10" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-button class="button" plain type="danger" size="small" v-auth="'activity.modify'"
                           @click="handleRemoveRightsItem(idx)">删除
                </el-button>
              </div>
            </template>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="权益名称" prop="rights.name">
                  <el-input
                      v-model="rights.name"
                      placeholder="名称"
                      clearable
                      class="wd200 ">
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="权益描述" prop="rightsDesc" class="w100">
              <el-input
                  maxlength="500"
                  show-word-limit
                  type="textarea"
                  :rows="2"
                  v-model.trim="rights.rightsDesc"
                  placeholder="权益描述"
                  clearable
                  class="w100">
              </el-input>
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="最小充值金额（元）" prop="amountMin">
                  <ext-input-number class="wd200" v-model="rights.amountMin" :ratio="100"
                                    placeholder="最小充值金额"></ext-input-number>
                  <!--                  <el-input-number
                                        controls-position="right"
                                        v-model.trim="rights.amountMin"
                                        placeholder="最小充值金额"
                                        clearable
                                        :min="0"
                                        class="wd200">
                                    </el-input-number>-->
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最大充值金额（元）" prop="amountMin">
                  <ext-input-number class="wd200" v-model="rights.amountMax" :ratio="100"
                                    placeholder="最大充值金额"></ext-input-number>
                  <!--                  <el-input-number
                                        controls-position="right"
                                        v-model.trim="rights.amountMax"
                                        placeholder="最大充值金额"
                                        clearable
                                        :min="0"
                                        class="wd200">
                                    </el-input-number>-->
                </el-form-item>

              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="折扣率（%）" prop="discount">
                  <el-input-number class="wd200"
                                    v-model="rights.discount"
                                    placeholder="例：75代表75折"
                                   :step="1"
                                    precision="0"  >
                  </el-input-number>
                  <!--                  <el-input-number-->
                  <!--                      :controls="false"-->
                  <!--                      v-model.trim="rights.discount"-->
                  <!--                      placeholder="例：75代表75折"-->
                  <!--                      clearable-->
                  <!--                      class="wd200">-->
                  <!--                  </el-input-number>-->
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="有效期（天）" prop="validity">
                  <el-input-number
                      :min="1"
                      :controls="false"
                      v-model.trim="rights.validity"
                      placeholder="有效天数"
                      clearable
                      class="wd200">
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>

          </el-card>
        </template>

        <!--        优惠券-->
        <template v-else>
          <el-card v-for="(coupon,idx) in state.ruleForm.couponList" :key="idx" class="mt10" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-button class="button" plain type="danger" size="small" v-auth="'activity.modify'"
                           @click="handleRemoveCouponItem(idx)">删除
                </el-button>
              </div>
            </template>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="名称" prop="coupon.name">
                  <el-input
                      v-model="coupon.name"
                      placeholder="名称"
                      clearable
                      class="wd200 ">
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="权益描述" prop="rightsDesc" class="w100">
              <el-input
                  maxlength="500"
                  show-word-limit
                  type="textarea"
                  :rows="2"
                  v-model.trim="coupon.couponDesc"
                  placeholder="权益描述"
                  clearable
                  class="w100">
              </el-input>

            </el-form-item>


            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="折扣类型" prop="couponType">
                  <ext-d-select
                      v-model="coupon.couponType"
                      placeholder="折扣类型"
                      type="Activity.couponType"
                      clearable
                      class="wd200 ">
                  </ext-d-select>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item v-if="coupon.couponType==='Discount'" label="折扣率（%）" prop="discount">
                  <el-input-number
                      :controls="false"
                      v-model.trim="coupon.discount"
                      placeholder="请输入折扣率"
                      clearable
                      class="wd200">
                  </el-input-number>
                </el-form-item>

              <el-form-item v-if="coupon.couponType==='FullDiscount'" label="折扣金额（元）" prop="discount">
                  <ext-input-number
                      :ratio="100"
                      :controls="false"
                      v-model.trim="coupon.discount"
                      placeholder="请输入折扣金额"
                      clearable
                      class="wd200">
                  </ext-input-number>
                </el-form-item>
              </el-col>
            </el-row>


            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="服务费门槛（元）" prop="minServiceMoney">
                  <ext-input-number class="wd200 " v-model="coupon.minServiceMoney" :ratio="100"
                                    placeholder="服务费门槛"></ext-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="数量(0为不限)" prop="quantity">
                  <el-input-number
                      :min=0
                      :controls="false"
                      v-model.trim="coupon.quantity"
                      placeholder="数量"
                      clearable
                      class="wd200">
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="领取方式" prop="receiveType">
                  <ext-d-select
                      v-model="coupon.receiveType"
                      placeholder="优惠券领取方式"
                      type="Coupon.receiveType"
                      clearable
                      class="wd200 ">
                  </ext-d-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="有效期（天）" prop="validity">
                  <el-input-number
                      :min="1"
                      :controls="false"
                      v-model.trim="coupon.validity"
                      placeholder="有效天数"
                      clearable
                      class="wd200">
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>

          </el-card>
        </template>


      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="onCancel" size="default">取 消</el-button>
          <el-button v-if="state.action==='add'||state.action==='edit'" :loading="state.btnLoading" type="primary"
                     @click="onSubmit" size="default">{{ state.dialog.submitTxt }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="ActivityDialog">
import {reactive, ref} from 'vue';
import {Msg} from "/@/utils/message";
import {$body, $get} from "/@/utils/request";
import u from '/@/utils/u'
import ExtDatePicker from "/@/components/form/ExtDatePicker.vue";
import ExtDSelect from "/@/components/form/ExtDSelect.vue";
import ExtSelect from "/@/components/form/ExtSelect.vue";
import ExtInputNumber from "/@/components/form/ExtInputNumber.vue";
import ExtEditor from "/@/components/form/ExtEditor.vue";

// 定义子组件向父组件传值/事件
const emit = defineEmits(['refresh']);
const formRef = ref();
const ext_editor_ref = ref();
//定义初始变量，重置使用
const initState = () => ({
  action: '',
  ruleForm: {
    id: 0,
    rechargeRightsList: [] as Array<any>,
    couponList: [] as Array<any>,
  },
  btnLoading: false,
  dialog: {
    isShowDialog: false,
    type: '',
    title: '',
    submitTxt: '',
  },
  rules: {
    name: [u.validator.required],
    startTime: [u.validator.required],
    endTime: [u.validator.required],
    discountType: [u.validator.required],
    targetUsers: [u.validator.required],
    applyStation: [u.validator.required],
    // allowStacke: [u.validator.required],
    // quantity: [u.validator.required],
    status: [u.validator.required],
  },
})

// 定义变量内容
const state = reactive(initState());


// 打开弹窗
const open = (action: string = 'add', row: any) => {
  state.dialog.title = u.dialog.actions[action].title + "『活动』"
  state.dialog.submitTxt = u.dialog.actions[action].btn + "『活动』"
  state.dialog.isShowDialog = true;
  state.action = action;
  if (action !== 'add') {
    loadData(row.id);
  } else {
    state.ruleForm = Object.assign(state.ruleForm, row);
    ext_editor_ref.value?.initText(state.ruleForm.activityDesc || '');
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
  console.log(state.ruleForm)
  let rechargeRightsList = state.ruleForm.rechargeRightsList;
  if (!u.isEmptyOrNull(rechargeRightsList)) {
    for (let i = 0; i < rechargeRightsList.length; i++) {
      const item: any = rechargeRightsList[i];
      if (item.amountMax <= 0) {
        Msg.message(`权益最大充值金额不能小于0`, 'error')
        return false;
      }

      if (item.amountMin > item.amountMax) {
        Msg.message(`权益最小充值金额不能大于最大充值金额`, 'error')
        return false;
      }
    }

    /*    state.ruleForm.rechargeRightsList.forEach((rights: any) => {
          rights.amountMin = rights.amountMin * 100;
          rights.amountMax = rights.amountMax * 100;
        })*/
  }

  formRef.value.validate((valid: boolean) => {
    if (valid) {
      state.btnLoading = true;
      const url = state.ruleForm.id > 0 ? "activity/modify" : "activity/add"
      $body(url, state.ruleForm).then(() => {
        state.btnLoading = false;
        Msg.message('操作成功');
        console.log('submit!')
        onClose();
        emit('refresh');
      })
    }
  }).catch(() => {
    state.btnLoading = false;
    Msg.message('请先完整填写表单', 'error');
  })
};

const handleFormChange = (formData: any) => {
  console.log(formData)
}

// 初始化表格数据
const loadData = (id: number) => {
  $get(`activity/${id}`).then((res: any) => {
    if (res && res.rechargeRightsList) {
      /*      res.rechargeRightsList.forEach((rights: any) => {
              rights.amountMin = rights.amountMin / 100;
              rights.amountMax = rights.amountMax / 100;
            })*/
    }
    state.ruleForm = res;
    state.ruleForm.rechargeRightsList = res.rechargeRightsList || []

    ext_editor_ref.value?.initText(res.activityDesc || '');
  })
}

const handleStationChange = (applyStation: number) => {
  console.log(applyStation)
  if (applyStation == 0) {
    // state.ruleForm.stationIds = [];
  }

}

const handleAddRightsItem = () => {
  if (state.ruleForm.discountType === 'RechargeRights') {
    state.ruleForm.rechargeRightsList.unshift({
      rightsDesc: '',
      amountMin: 0,
      amountMax: 0,
      validity: 1
    })
  } else if (state.ruleForm.discountType === 'Coupon') {
    state.ruleForm.couponList.unshift({
      couponDesc: '',
      couponType: 'Discount',
      minServiceMoney: 0,
      validity: 1,
      quantity: 0,
    })
  }

}

const handleRemoveRightsItem = (idx: number) => {
  state.ruleForm.rechargeRightsList.splice(idx, 1)
}
const handleRemoveCouponItem = (idx: number) => {
  state.ruleForm.couponList.splice(idx, 1)
}

// 暴露变量
defineExpose({
  open
});


</script>