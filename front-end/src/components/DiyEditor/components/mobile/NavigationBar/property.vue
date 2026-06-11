<template>
  <el-form label-width="100px" :model="formData" :rules="rules">
    <el-form-item :label="t('diy.navigationBar.style')" prop="styleType">
      <el-radio-group v-model="formData!.styleType">
        <el-radio value="normal">{{
          t('diy.navigationBar.standard')
        }}</el-radio>
        <el-tooltip
          :content="t('diy.navigationBar.innerTooltip')"
          placement="top"
        >
          <el-radio value="inner">{{
            t('diy.navigationBar.immersive')
          }}</el-radio>
        </el-tooltip>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      :label="t('diy.navigationBar.alwaysShow')"
      prop="alwaysShow"
      v-if="formData.styleType === 'inner'"
    >
      <el-radio-group v-model="formData!.alwaysShow">
        <el-radio :value="false">{{ t('common.off') }}</el-radio>
        <el-tooltip
          :content="t('diy.navigationBar.alwaysShowTooltip')"
          placement="top"
        >
          <el-radio :value="true">{{ t('common.on') }}</el-radio>
        </el-tooltip>
      </el-radio-group>
    </el-form-item>
    <el-form-item :label="t('diy.navigationBar.bgType')" prop="bgType">
      <el-radio-group v-model="formData.bgType">
        <el-radio value="color">{{
          t('diy.navigationBar.solidColor')
        }}</el-radio>
        <el-radio value="img">{{ t('diy.navigationBar.image') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      :label="t('diy.navigationBar.bgColor')"
      prop="bgColor"
      v-if="formData.bgType === 'color'"
    >
      <ColorInput v-model="formData.bgColor" />
    </el-form-item>
    <el-form-item :label="t('diy.navigationBar.bgImg')" prop="bgImg" v-else>
      <div class="flex items-center">
        <UploadImg
          v-model="formData.bgImg"
          :limit="1"
          width="56px"
          height="56px"
        />
        <span class="text-xs text-gray-400 ml-2 mb-2">{{
          t('diy.navigationBar.suggestWidth')
        }}</span>
      </div>
    </el-form-item>
    <el-card class="property-group" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ t('diy.navigationBar.contentMp') }}</span>
          <el-form-item prop="_local.previewMp" class="m-b-0!">
            <el-checkbox
              v-model="formData._local.previewMp"
              @change="
                formData._local.previewOther = !formData._local.previewMp
              "
            >
              {{ t('diy.navigationBar.preview') }}
            </el-checkbox>
          </el-form-item>
        </div>
      </template>
      <NavigationBarCellProperty v-model="formData.mpCells" is-mp />
    </el-card>
    <el-card class="property-group" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span>{{ t('diy.navigationBar.contentOther') }}</span>
          <el-form-item prop="_local.previewOther" class="m-b-0!">
            <el-checkbox
              v-model="formData._local.previewOther"
              @change="
                formData._local.previewMp = !formData._local.previewOther
              "
            >
              {{ t('diy.navigationBar.preview') }}
            </el-checkbox>
          </el-form-item>
        </div>
      </template>
      <NavigationBarCellProperty v-model="formData.otherCells" :is-mp="false" />
    </el-card>
  </el-form>
</template>

<script setup lang="ts">
  import { NavigationBarProperty } from './config'
  import { useVModel } from '@vueuse/core'
  import NavigationBarCellProperty from '@/components/DiyEditor/components/mobile/NavigationBar/components/CellProperty.vue'
  // 导航栏属性面板
  defineOptions({ name: 'NavigationBarProperty' })
  const { t } = useI18n()
  // 表单校验
  const rules = computed(() => ({
    name: [
      {
        required: true,
        message: t('diy.navigationBar.nameRequired'),
        trigger: 'blur'
      }
    ]
  }))

  const props = defineProps<{ modelValue: NavigationBarProperty }>()
  const emit = defineEmits(['update:modelValue'])
  const formData = useVModel(props, 'modelValue', emit)
  if (!formData.value._local) {
    formData.value._local = { previewMp: true, previewOther: false }
  }
</script>

<style scoped lang="scss"></style>
