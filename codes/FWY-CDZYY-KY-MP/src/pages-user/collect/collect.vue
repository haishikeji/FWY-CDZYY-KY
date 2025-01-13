<template>
  <view class="pl-20 pr-20" v-if="list && list.length">
    <view class="mt-20" v-for="(item, index) in list" :key="index">
      <charge-station
        :title="item.stationName"
        :tag="item.construction"
        :price="item.totalFee"
        :fast="item.fastEquipmentInfos"
        :slow="item.slowEquipmentInfos"
        :sId="item.StationID"
        :address="item.address"
        :latitude="item.location.stationLat"
        :longitude="item.location.stationLng"
      ></charge-station>
    </view>
  </view>

  <view
    class="pt-40 flex-center fs-30"
    style="color: rgba(0, 0, 0, 0.6)"
    v-if="list && list.length <= 0"
    >暂无数据</view
  >

  <view class="pt-40 flex-center fs-28" style="opacity: 0.6" v-if="!list"
    >加载中</view
  >
</template>

<script setup lang="ts">
import { fetchStationByIds } from "../../api/charge";
import { fetchCollectList } from "../../api/user";
import { onLoad } from "@dcloudio/uni-app";
import { ref } from "vue";
const list = ref<any[]>();
onLoad(() => {
  fetchCollectList().then((collectIds) => {
    fetchStationByIds(collectIds || []).then((res: any) => {
      list.value = res;
    });
  });
});
</script>

<style lang="scss">
page {
  background-color: #f5f5f5;
}
</style>
