<template>
  <ul class="select">
    <li v-for="(item, index) of selectData" :key="index" class="select-item">
      <ul>
        <label class="select-item-label">{{ item.subTitle }}</label>
        <slot name="select-custom-content">
          <div v-if="!item.options || item.options.length == 0">
            <slot name="empty-text">
              <div class="select-item-empty">
                {{ item.emptyOptionsText || '暂无选项' }}
              </div>
            </slot>
          </div>
          <template v-else>
            <li
              v-for="(option, idx) of item.options"
              :key="idx"
              class="select-item-option"
              @click="onSelect(option)"
            >
              <span>
                {{ option.name }}
              </span>
              <CloseOutlined
                v-show="item.hasDel"
                :key="option"
                class="select-item-del"
                @click.stop="onRemove(option)"
              />
            </li>
          </template>
        </slot>
      </ul>
    </li>
  </ul>
</template>

<script setup lang="ts">
  import { CloseOutlined } from '@ant-design/icons-vue'
  import { defineProps, toRefs } from 'vue'

  export type Option = {
    action: string
    name: string
  } & {
    [key: string]: any
  }

  export interface SelectData {
    subTitle?: string
    action?: string
    hasDel?: boolean
    emptyOptionsText?: string
    options?: Option[]
  }
  interface SelectBoxProps {
    selectData?: SelectData[]
  }

  interface SelectBoxEmits {
    (event: 'select', option: Option): void
    (event: 'remove', option: Option): void
  }

  const props = defineProps<SelectBoxProps>()
  const emits = defineEmits<SelectBoxEmits>()
  const { selectData } = toRefs(props)

  const onRemove = (option: Option) => {
    emits('remove', option)
  }
  const onSelect = (option: Option) => {
    emits('select', option)
  }
</script>

<style lang="scss" scoped>
  ul {
    list-style: none;
    margin: 0;
    padding: 0;
    box-sizing: border-box;

    li {
      padding: 4px 6px;
      margin-bottom: 6px;
      border-radius: 6px;
    }
  }

  .select {
    width: 100%;
    height: 100%;

    &-item {
      margin-bottom: 20px;

      &-label {
        display: block;
        margin-bottom: 10px;
        font-weight: 500;
      }

      &-empty {
        padding: 10px;
        text-align: center;
        color: #a9a9a9;
      }

      &-option {
        display: flex;
        align-items: center;
        justify-content: space-between;
        border: 1px solid #c9c9c9;
        cursor: pointer;

        span {
          flex: 1;
          text-align: center;
        }

        .select-item-del {
          transition: opacity 0.08s ease-in-out;
          opacity: 0;
          flex: 0 0 14px;
        }

        &:hover {
          background-color: rgb(0, 0, 0, 0.06);

          .select-item-del {
            display: block;
            opacity: 1;
          }
        }
      }
    }
  }
</style>
