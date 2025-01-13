<template>
  <style-dialog
      v-if="visible"
      @close="handleClose"
      title="优惠明细">

    <view class="page pt-20 pr-40 pl-40">

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
                    <image class="width-84" src="/static/images/discount-rights-card.png" mode="widthFix"></image>
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
                      <view class="fs-24 color-999">享受{{ (item.discount / 10).toFixed(1) }}折服务费权益，本次充电优惠：{{ ((item.orderDiscountAmount || 0) / 100).toFixed(2) }}元
                                                <text class="color-primary">详细规则</text>
                                                <text class="fs-24 ml-4 color-primary">>></text>
                      </view>
                    </view>
                  </view>
                </block>
              </view>
            </view>
          </view>
        </view>

      </view>

      <view v-if="mode===1">
        <view
            class="item mt-20 "
            v-for="(item, index) in couponList"
            :class="item.collected?'inactive':''"
            :key="index">
          <image class="full-percent" src="/static/images/discount-bg.png"></image>
          <view class="absolute-full flex pt-8 pb-8">
            <view class="left  flex-justify-center pl-48">
              <view class="item-content_left">
                <view class="flex-align-end">
                  <view class="item-header__left">

                    <image
                        class="width-84"
                        src="/static/images/discount-coupon.png"
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

                <block>
                  <view class="fs-26 color-999 mt-8"
                        @click.stop="to(`/pages-common/activity/activity?id=${item.activityId}`)">
                    <view class="fs-26 color-999">到期时间：{{ item.endTime }}</view>
                    <view class="flex-align-center">
                      <view class="fs-26 color-999">本次充电优惠：{{ ((item.orderDiscountAmount || 0) / 100).toFixed(2) }}元
                        <text class="color-primary">详细规则</text>
                        <text class="fs-24 ml-4 color-primary">>></text>
                      </view>
                    </view>
                  </view>
                </block>
              </view>
            </view>
          </view>
        </view>
      </view>


    </view>
    >
  </style-dialog>

</template>

<script setup lang="ts">
import {ref} from "vue";
import {fetchOrderDiscount} from "@/api/user";
import {to} from "@/utils/navigate";

const mode = ref(0);
const rightsList = ref<any[]>([]);
const couponList = ref<any[]>([]);
const visible = ref(false);

const handleClose = () => {
  visible.value = false;
}


const open = (startChargeSeq: string, discountType: string) => {
  visible.value = true;
  fetchOrderDiscount(startChargeSeq, discountType).then((res) => {
    console.log(res)
    if (res) {
      if (discountType == "RechargeRights") {
        rightsList.value = [res]
        mode.value = 0;
      } else {
        couponList.value = [res]
        mode.value = 1;
      }
    }
  })
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
