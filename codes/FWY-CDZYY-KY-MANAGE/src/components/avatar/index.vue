<template>
  <el-avatar v-for="(avatar,idx) in state.avatarList" :key="idx"
             class="cursor-pointer avatar-item"
             :title="avatar.name"
             :size="size" :src="u.fmt.fmtImg(avatar.avatar)">
    <el-avatar :style="getStyle(avatar)">{{ getFirst(avatar.name) }}</el-avatar>
  </el-avatar>
</template>

<script setup name="Avatar" lang="ts">
import {reactive, watch} from 'vue';
import u from "/@/utils/u";

const props = defineProps({
  size: {
    type: Number,
    default: 39
  },
  option: {
    type: [Object, Array],
    require: true,
  }
})

type Avatar = {
  id: number;
  avatar: string;
  name: string;
}


const state = reactive({
  avatarList: [] as Array<Avatar>
})

watch(() => props.option, (val, oldVal) => {
  console.log("watch>>>>>>>", props.option)
  if (val == oldVal) {
    return;
  }
  if (props.option) {
    if (props.option instanceof Array) {
      state.avatarList = props.option.map((k: any) => {
        let {id, avatar, name,} = k;
        return {id, name, avatar}
      })
    } else {
      let {id, avatar, name} = props.option;
      state.avatarList = {id, name, avatar}
    }
  } else {
    state.avatarList = [];
  }
}, {deep: true, immediate: true})


const getFirst = (name: string) => {
  if (name) {
    return name.substring(0, 1);
  }
  return '';
};


const getStyle = (avatar: Avatar) => {
  console.log(avatar)
  return {
    backgroundColor: colorById(avatar.id || 0),
    color: '#fff'
  }
};

const colorById = (i: number) => {
  if (i < 10) i = i * 302.3;
  if (i < 100) i = i * 31.2;
  for (; i > 255; i *= 0.98) ;
  let temp = i.toString().substring(i.toString().length - 3);
  i += parseInt(temp);
  for (; i > 255; i -= 255) ;
  i = parseInt(i);
  if (i < 10) i += 10;

  let R: number = i * (i / 100);
  for (; R > 255; R -= 255) ;
  if (R < 50) R += 60;
  R = parseInt(R).toString(16);

  var G = i * (i % 100);
  for (; G > 255; G -= 255) ;
  if (G < 50) G += 60;
  G = parseInt(G).toString(16);

  var B = i * (i % 10);
  for (; B > 255; B -= 255) ;
  if (B < 50) B += 60;
  B = parseInt(B).toString(16);

  // console.log(i + ":" + R + ":" + G + ":" + B);
  return "#" + R + G + B;
}

// const firstLetter = computed(() => {
//   return props?.option?.name?.substring(0, 1) || '';
// });
</script>

<style scoped lang="scss">
.avatar-item {
  border: 2px solid #fff;
}

</style>