<style scoped lang="scss">

</style>
<template>
  <div class="system-dialog-container">
    <el-dialog
        title="文件预览"
        v-model="state.dialog.isShowDialog"
        width="820px"
        fullscreen
        draggable
        destroy-on-close
        :close-on-click-modal="false"
        @close="onClose"
        align-center>
<!--            <iframe :src="state.pdfUrl"
                    ref="pdfViewer"
                    height="100%"
                    width="100%"
                    style="position: absolute"></iframe>-->
<!--
      <VuePdfApp
          file-name="文件名"
          style="width:100%;height:100%"
          :pdf="state.pdfData">

      </VuePdfApp>
-->

<!--      <pdf-viewer :src="state.pdfData" :scale="1" :autoresize="true"></pdf-viewer>-->

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="onCancel" size="default"> 关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="InvoiceDialog">
import {defineAsyncComponent, reactive, onMounted, ref} from 'vue';
import u from "/@/utils/u"
// import pdfjsLib from "pdfjs-dist";
// import "pdfjs-dist/pdf.worker.min.js";
// import { pdfViewer } from "pdfjs-dist";


// 定义子组件向父组件传值/事件
const formRef = ref();
//定义初始变量，重置使用
const initState = () => ({
  pdfUrl: '',
  pdfData:null,
  dialog: {
    isShowDialog: false,
    type: '',
    title: '',
    submitTxt: '',
  },
})

// 定义变量内容
const state = reactive(initState());


// 打开弹窗
const open = (url: string) => {
  // loadPdf(url);
  const serverUrl = import.meta.env.VITE_API_URL
  state.pdfUrl = `${serverUrl}/file/preview`
  state.dialog.isShowDialog = true;
  console.log("openxxx")
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


// 暴露变量
defineExpose({
  open
});


</script>