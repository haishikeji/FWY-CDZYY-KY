<template>
  <style-dialog
      v-if="visible"
      confirm
      @ok="handleConfirm"
      @close="handleClose"
      title="选择优惠">

    <view class="page pt-20 pr-40 pl-40">
      <view class="tab-list flex-align-end">
        <view class="fs-28 color-666 mt-20 w50" :class="mode===0?'active':''" @click="changeTab(0)">权益卡</view>
        <view class="fs-28 color-666 mt-20 w50" :class="mode===1?'active':''" @click="changeTab(1)">优惠券</view>
      </view>
      <view v-if="mode===0">
        <view
            class="item mt-20 "
            v-for="(item, index) in rightsList"
            :key="index">

          <image class="full-percent" src="/pages-charge/static/discount-bg.png"></image>
          <view class="absolute-full flex pt-8 pb-8">
            <view class="left  flex-justify-center pl-48">

              <view class="item-content_left">
                <view class="flex-align-end">
                  <view class="item-header__left">
                    <image class="width-84" src="/pages-charge/static/discount-rights-card.png" mode="widthFix"></image>
                    <view class="fs-48 lh-58 color-000 fw-500 ml-16">
                      {{ (item.discount / 10).toFixed(1) }}折
                    </view>
                    <view class="fs-26 color-999 ml-16" v-if="mode === 0"
                    >抵扣充电服务费
                    </view>
                  </view>
                </view>


                <block>
                  <view class="fs-26 color-999 mt-8"
                        @click.stop="to(`/pages-common/activity/activity?id=${item.activityId}`)">
                    <view class="fs-26 color-999">到期时间：{{ item.endTime }}</view>
                    <view class="flex-align-center" v-if="mode === 0">
                      <view class="fs-26 color-999">权益余额：{{ ((item.rightsBalance || 0) / 100).toFixed(2) }}元
                        <text class="color-primary">详细规则</text>
                        <text class="fs-24 ml-4 color-primary">>></text>
                      </view>
                    </view>
                  </view>
                </block>
              </view>
              <view class="item-content_right">
                <style-checkbox
                    @on-checkChange="handleCheckRights($event,item.rightsId,item.id)"
                    :size="48"
                    :checked="item.rightsId === rightsId"
                    :iconsize="17"
                ></style-checkbox>
              </view>

            </view>
          </view>
        </view>
        <view style="width: 100%;text-align: center;">
          <image v-if="!rightsList||rightsList.length==0" style="width: 240rpx;" mode="widthFix" src="/static/images/search-empty.png"></image>
        </view>

      </view>


      <view v-else-if="mode===1">
        <view
            class="item mt-20"
            v-for="(item, index) in couponList"
            :key="index">

          <!--            :class="item.usageStatus==1||item.status==0?'inactive':''"-->
          <view class="absolute-full flex pt-8 pb-8">
            <view class="left  flex-justify-center pl-48">
              <view class="item-content_left">
                <view class="flex-align-end">
                  <view class="item-header__left">

                    <image
                        class="width-84"
                        src="/pages-charge/static/discount-coupon.png"
                        mode="widthFix"
                    ></image>
                    <view></view>
                    <view class="fs-48 lh-58 color-000 fw-500 ml-16" v-if="item.couponType=='FullDiscount'">
                      {{ ((item.discount || 0) / 100).toFixed(2) }}元
                    </view>

                    <view class="fs-48 lh-58 color-000 fw-500 ml-16" v-else>
                      {{ (item.discount / 10).toFixed(1) }}折
                    </view>

                    <view class="fs-26 color-999 ml-16">
                      充电服务费：{{ item.couponType == 'FullDiscount' ? '满减券' : '折扣券' }}
                    </view>
                  </view>

                </view>

                <block v-if="mode === 1">
                  <view class="fs-26 color-999 mt-8"
                        @click.stop="to(`/pages-common/activity/activity?id=${item.activityId}`)">
                    <view class="fs-26 color-999">到期时间：{{ item.endTime }}</view>
                    <view class="flex-align-center">
                      <view class="fs-26 color-999">服务费门槛：{{ ((item.minServiceMoney || 0) / 100).toFixed(2) }}元
                        <text class="color-primary">详细规则</text>
                        <text class="fs-24 ml-4 color-primary">>></text>
                      </view>
                    </view>
                  </view>
                </block>
              </view>
              <view class="item-content_right">
                <view class="item-content_right">
                  <!--                  <radio-group @change="handleCheckCoupon($event,item.couponId)">
                                      <radio  :checked="item.couponId === couponId"/>
                                    </radio-group>-->
                  <style-checkbox
                      @on-checkChange="handleCheckCoupon($event,item.couponId,item.id)"
                      :size="48"
                      :checked="item.couponId === couponId"
                      :iconsize="17"
                  ></style-checkbox>
                </view>
              </view>


            </view>
          </view>
        </view>

        <view style="width: 100%;text-align: center;">
          <image v-if="!couponList||couponList.length==0" style="width: 240rpx;" mode="widthFix" src="/static/images/search-empty.png"></image>
        </view>
      </view>

    </view>

  </style-dialog>

</template>

<script setup lang="ts">
import {onPullDownRefresh} from "@dcloudio/uni-app";
import {ref} from "vue";
import {listStationAvailableRightsAndCoupons} from "@/api/user";
import {to} from "@/utils/navigate";

const mode = ref(0);
const couponList = ref<any[]>([]);
const rightsList = ref<any[]>([]);

const stationId = ref(null);
const rightsId = ref(null);
const userRechargeRightsId = ref(null);
const couponId = ref(null);
const userCouponId = ref(null);
const visible = ref(false);

const emits = defineEmits(['on-change'])

const handleClose = () => {
  visible.value = false;
  rightsId.value = null;
  userRechargeRightsId.value = null;
  couponId.value = null;
  userCouponId.value = null;
  stationId.value = null;
  mode.value = 0;
}

const handleConfirm = () => {
  let emitData = {
    rightsId: rightsId.value,
    userRechargeRightsId: userRechargeRightsId.value,
    couponId: couponId.value,
    userCouponId: userCouponId.value,
    stationId: stationId.value,
    userRights: rightsList.value?.find((k: any) => k.rightsId == rightsId.value ),
    userCoupon: couponList.value?.find((k: any) => k.couponId == couponId.value),
  }
   console.log(emitData)
  emits('on-change', emitData)
  handleClose();
}

onPullDownRefresh(() => {
  loadData();
});

const changeTab = (tab: number) => {
  mode.value = tab;
}

/**
 *
 * @param val
 * @param rId rechargeRightsId
 * @param urId userRechargeRightsId
 */
const handleCheckRights = (val: boolean, rId: any,urId: any) => {
  // console.log("handleCheckRights", userRightsId, val)
  if (rightsId.value == rId) {
    rightsId.value = null;
    userRechargeRightsId.value = null;
  } else {
    rightsId.value = rId;
    userRechargeRightsId.value = urId;
    couponId.value = null;
  }
}

/**
 *
 * @param e
 * @param cid couponId
 * @param ucid userCouponId
 */
const handleCheckCoupon = (e: any, cid: any,ucid: any) => {
  // console.log("handleCheckCoupon", userCouponId, e)
  if (couponId.value == cid) {
    couponId.value = null;
    userCouponId.value = null;
  } else {
    couponId.value = cid;
    userCouponId.value = ucid;
    rightsId.value = null;
  }
}


const loadData = () => {
  uni.showLoading({
    title: "加载中",
  });
  listStationAvailableRightsAndCoupons(stationId.value).then((res) => {
    let {userRechargeRightsList, userCouponList} = res;
    couponList.value = (userCouponList || []).sort((a: any, b: any) => new Date(a.endTime).getTime() - new Date(b.endTime).getTime());
    rightsList.value = (userRechargeRightsList || []).sort((a: any, b: any) => new Date(a.endTime).getTime() - new Date(b.endTime).getTime());
    if (!rightsId.value && !couponId.value) {
      if (rightsList.value && rightsList.value.length > 0) {
        rightsId.value = rightsList.value[0].rightsId;
        mode.value = 0;
      } else if (couponList.value && couponList.value.length > 0) {
        couponId.value = couponList.value[0].couponId;
        mode.value = 1;
      }
    } else if (rightsId.value) {
      mode.value = 0;
    } else if (couponId.value) {
      mode.value = 1;
    }

    // console.log(res)
    uni.hideLoading();
  });
}
const open = (sid: any, userRightsId: any, userCouponId: any) => {
  visible.value = true;
  stationId.value = sid;
  rightsId.value = userRightsId;
  couponId.value = userCouponId;
  loadData()
}


defineExpose({
  open
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  width: 100%;
  background-color: #f6f7fa;

  .item {
    position: relative;
    min-height: 220rpx;
    border-radius: 20rpx;
    overflow: hidden;
    background: #fff;

    .left {
      flex-grow: 1;
    }

    .right {
      flex-shrink: 0;

      .btn {
        border-radius: 8rpx;
        border: 1px solid var(--color-primary);
        color: var(--color-primary);
        width: 144rpx;
        height: 56rpx;
      }
    }
  }

  .tab-list {
    width: 100%;

    .w50 {
      width: 50%;
      text-align: center;
    }

    .active {
      color: var(--color-primary);
    }
  }
}

.item-status {
  font-size: 24rpx;
  padding: 0rpx 6rpx;
  color: var(--color-primary);
  border: 2rpx solid var(--color-primary);
  border-radius: 6rpx;

}

.item-header__left {
  display: inline-flex;
  align-items: flex-end;
}

.item-header__right {
  padding: 5rpx;
  display: flex;
  //justify-content: flex-end;
}

.inactive {
  cursor: not-allowed;
  pointer-events: none;

  .item-status {
    font-size: 24rpx;
    padding: 0rpx 6rpx;
    color: #ccc !important;
    border: 2rpx solid #ccc;
    border-radius: 6rpx;

  }

  .color-primary {
    color: #ccc !important;
  }
}

.item-content_left {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-around;

}

.item-content_right {
  width: 120rpx;
  display: flex;
  align-items: center;
  align-content: center;
}
</style>
